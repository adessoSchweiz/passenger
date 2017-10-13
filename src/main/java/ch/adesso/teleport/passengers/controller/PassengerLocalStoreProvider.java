package ch.adesso.teleport.passengers.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;

import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.serializer.KafkaJsonDeserializer;
import ch.adesso.teleport.kafka.serializer.KafkaJsonSerializer;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.kafka.store.KafkaStoreBuilder;
import ch.adesso.teleport.kafka.store.KafkaStoreProcessor;
import ch.adesso.teleport.kafka.store.ProcessedEvent;
import ch.adesso.teleport.passengers.entity.Passenger;
import io.reactivex.subjects.PublishSubject;

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

		kafkaStreams = new KafkaStoreBuilder<Passenger>().withStoreSerializer(new KafkaJsonSerializer<>())
				.withStoreDerializer(new KafkaJsonDeserializer<>(Passenger.class))
				.withSourceTopicName(Topics.PASSENGER_EVENT_TOPIC.toString())
				.withStateStoreName(Topics.PASSENGER_AGGREGATE_STORE.toString()).withProcessorSupplier(
						() -> new KafkaStoreProcessor<Passenger>(Topics.PASSENGER_AGGREGATE_STORE.toString(),
								Passenger::new, rxPublishSubject::onNext))
				.build();

		kafkaLocalStore = new KafkaEventStore(kafkaStreams);
	}

	@PreDestroy
	public void close() {
		rxPublishSubject.onComplete();
		this.kafkaStreams.close();
	}

	@PassengerQualifier
	@Produces
	public KafkaEventStore getKafkaLocalStore() {
		return kafkaLocalStore;
	}

	@PassengerQualifier
	@Produces
	public PublishSubject<ProcessedEvent> getRxPublishSubject() {
		return rxPublishSubject;
	}
}
