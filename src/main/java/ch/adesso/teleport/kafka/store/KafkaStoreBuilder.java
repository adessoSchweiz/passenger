package ch.adesso.teleport.kafka.store;

import static org.apache.kafka.streams.state.Stores.create;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.ProcessorSupplier;
import org.apache.kafka.streams.processor.StateStoreSupplier;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.state.KeyValueStore;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.serializer.KafkaAvroReflectDeserializer;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;

public class KafkaStoreBuilder<T> {

	public static final String SCHEMA_REGISTRY_URL = System.getenv("SCHEMA_REGISTRY_URL");

	private Properties streamsProperties;

	private String sourceTopicName;
	private String stateStoreName;

	private Serializer<T> storeSerializer;
	private Deserializer<T> storeDeserializer;

	private ProcessorSupplier<String, CoreEvent> processorSupplier;

	public KafkaStoreBuilder() {
		this.streamsProperties = KafkaConfiguration.streamsDefaultProperties();
	}

	public KafkaStoreBuilder<T> withProperty(String name, Object value) {
		this.streamsProperties.put(name, value);
		return this;
	}

	public KafkaStoreBuilder<T> withSourceTopicName(String sourceTopicName) {
		this.sourceTopicName = sourceTopicName;
		return this;
	}

	public KafkaStoreBuilder<T> withStateStoreName(String stateStoreName) {
		this.stateStoreName = stateStoreName;
		return this;
	}

	public KafkaStoreBuilder<T> withStoreSerializer(Serializer<T> storeSerializer) {
		this.storeSerializer = storeSerializer;
		return this;
	}

	public KafkaStoreBuilder<T> withStoreDerializer(Deserializer<T> storeDeserializer) {
		this.storeDeserializer = storeDeserializer;
		return this;
	}

	public KafkaStoreBuilder<T> withProcessorSupplier(ProcessorSupplier<String, CoreEvent> processorSupplier) {
		this.processorSupplier = processorSupplier;
		return this;
	}

	public KafkaStreams build() {
		KafkaStreams streams = new KafkaStreams(createStreamBuilder(), new StreamsConfig(streamsProperties));
		streams.cleanUp();
		streams.start();
		return streams;
	}

	private TopologyBuilder createStreamBuilder() {
		String sourceName = sourceTopicName + "-source";
		String processorName = stateStoreName + "-processor";

		@SuppressWarnings("rawtypes")
		StateStoreSupplier<KeyValueStore> stateStore = createStateStore(stateStoreName);

		Deserializer<CoreEvent> coreEventDeserializer = new KafkaAvroReflectDeserializer<>(CoreEvent.class);
		coreEventDeserializer.configure(Collections.singletonMap(
				AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG, System.getenv("SCHEMA_REGISTRY_URL")), false);

		return new KStreamBuilder().addStateStore(stateStore)
				.addSource(sourceName, new StringDeserializer(), coreEventDeserializer, sourceTopicName)
				.addProcessor(processorName, processorSupplier, sourceName)
				.connectProcessorAndStateStores(processorName, stateStore.name());
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private StateStoreSupplier<KeyValueStore> createStateStore(String storeName) {
		Serde<T> serde = Serdes.serdeFrom(this.storeSerializer, this.storeDeserializer);
		return create(storeName).withKeys(Serdes.String()).withValues(serde).persistent().enableCaching().build();
	}

}
