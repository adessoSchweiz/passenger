package ch.adesso.teleport.routes.controller;

import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

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
import ch.adesso.teleport.routes.entity.Route;
import ch.adesso.teleport.routes.event.RouteEventEnvelope;

@Startup
@Singleton
public class RouteLocalStoreProvider {

	private KafkaStreams kafkaStreams;
	private KafkaEventStore kafkaLocalStore;

	@PostConstruct
	public void init() {
		Properties props = KafkaConfiguration.streamsDefaultProperties();
		props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "streams-routes");
		props.setProperty(StreamsConfig.STATE_DIR_CONFIG, "/tmp/kafka-streams/routes");

		kafkaStreams = new KafkaStreams(createKafkaBuilder(), new StreamsConfig(props));
		kafkaStreams.cleanUp();
		kafkaStreams.start();

		kafkaLocalStore = new KafkaEventStore(kafkaStreams);
	}

	@PreDestroy
	public void close() {
		this.kafkaStreams.close();
	}

	public KafkaEventStore getKafkaLocalStore() {
		return kafkaLocalStore;
	}

	private TopologyBuilder createKafkaBuilder() {
		Serializer<RouteEventEnvelope> eventEnvelopeSerializer = new KafkaAvroReflectSerializer<>();
		Deserializer<RouteEventEnvelope> eventEnvelopeDeserializer = new KafkaAvroReflectDeserializer<>(
				RouteEventEnvelope.class);

		Serde<RouteEventEnvelope> eventEnvelopeSerde = Serdes.serdeFrom(eventEnvelopeSerializer,
				eventEnvelopeDeserializer);

		Serializer<Route> routeSerializer = new KafkaJsonSerializer<>();
		Deserializer<Route> routeDeserializer = new KafkaJsonDeserializer<>(Route.class);
		Serde<Route> routeSerde = Serdes.serdeFrom(routeSerializer, routeDeserializer);

		String sourceTopic = Topics.ROUTE_EVENT_TOPIC.toString();
		String storeName = Topics.ROUTE_AGGREGATE_STORE.toString();

		String sourceName = sourceTopic + "-source";
		String processorName = storeName + "-processor";

		// local state store
		@SuppressWarnings("unchecked")
		StateStoreSupplier<KeyValueStore<String, Route>> stateStore = Stores.create(storeName).withKeys(Serdes.String())
				.withValues(routeSerde).persistent().build();

		// aggregate route events -> route
		return new TopologyBuilder().addStateStore(stateStore)
				.addSource(sourceName, Serdes.String().deserializer(), eventEnvelopeSerde.deserializer(), sourceTopic)
				.addProcessor(processorName, () -> new KafkaStoreProcessor<>(storeName, Route::new, null), sourceName)
				.connectProcessorAndStateStores(processorName, storeName);

	}

}
