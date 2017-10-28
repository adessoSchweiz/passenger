package ch.adesso.teleport.passengers.controller;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;

import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.config.Topics;
import ch.adesso.teleport.kafka.serializer.KafkaAvroReflectDeserializer;
import ch.adesso.teleport.kafka.serializer.KafkaAvroReflectSerializer;
import ch.adesso.teleport.kafka.serializer.KafkaJsonDeserializer;
import ch.adesso.teleport.kafka.serializer.KafkaJsonSerializer;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.kafka.store.KafkaStoreProcessor;
import ch.adesso.teleport.kafka.store.ProcessedEvent;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.passengers.event.PassengerEventEnvelope;
import io.reactivex.subjects.PublishSubject;

/**
 * 
 * local passenger store with default event subscription for processed events.
 * 
 */

@Startup
@Singleton
public class PassengerLocalStoreProvider {

	@Inject
	Event<ProcessedEvent> processedEvents;

	private KafkaStreams kafkaStreams;
	private KafkaEventStore kafkaLocalStore;

	// allows dynamic subscriptions for processed events
	private PublishSubject<ProcessedEvent> rxPublishSubject;

	@PostConstruct
	public void init() {
		rxPublishSubject = PublishSubject.create();

		// default subscriber
		rxPublishSubject.subscribe(processedEvents::fire);

		Properties props = KafkaConfiguration.streamsDefaultProperties();
		props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streams-passengers");
		props.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/passengers");

		kafkaStreams = new KafkaStreams(createKafkaBuilder(), new StreamsConfig(props));
		kafkaStreams.cleanUp();
		kafkaStreams.start();

		kafkaLocalStore = new KafkaEventStore(kafkaStreams);
	}

	@PreDestroy
	public void close() {
		rxPublishSubject.onComplete();
		this.kafkaStreams.close();
	}

	public KafkaEventStore getKafkaLocalStore() {
		return kafkaLocalStore;
	}

	public PublishSubject<ProcessedEvent> getRxPublishSubject() {
		return rxPublishSubject;
	}

	private TopologyBuilder createKafkaBuilder() {
		Serializer<PassengerEventEnvelope> eventEnvelopeSerializer = new KafkaAvroReflectSerializer<>();
		Deserializer<PassengerEventEnvelope> eventEnvelopeDeserializer = new KafkaAvroReflectDeserializer<>(
				PassengerEventEnvelope.class);

		Serde<PassengerEventEnvelope> eventEnvelopeSerde = Serdes.serdeFrom(eventEnvelopeSerializer,
				eventEnvelopeDeserializer);

		Serializer<Passenger> passengerSerializer = new KafkaJsonSerializer<>();
		Deserializer<Passenger> passengerDeserializer = new KafkaJsonDeserializer<>(Passenger.class);
		Serde<Passenger> passengerSerde = Serdes.serdeFrom(passengerSerializer, passengerDeserializer);

		String sourceTopic = Topics.PASSENGER_EVENT_TOPIC.toString();
		String storeName = Topics.PASSENGER_AGGREGATE_STORE.toString();

		String sourceName = sourceTopic + "-source";
		String processorName = storeName + "-processor";

		// local state store
		@SuppressWarnings("unchecked")
		StateStoreSupplier<KeyValueStore<String, Passenger>> stateStore = Stores.create(storeName)
				.withKeys(Serdes.String()).withValues(passengerSerde).persistent().build();

		// aggregate passenger events -> passenger
		return new TopologyBuilder().addStateStore(stateStore)
				.addSource(sourceName, Serdes.String().deserializer(), eventEnvelopeSerde.deserializer(), sourceTopic)
				.addProcessor(processorName,
						() -> new KafkaStoreProcessor<>(storeName, Passenger::new, rxPublishSubject::onNext),
						sourceName)
				.connectProcessorAndStateStores(processorName, storeName);

	}

}
