package ch.adesso.teleport.kafka.config;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import ch.adesso.teleport.kafka.serializer.CoreEventAvroDeserializer;
import ch.adesso.teleport.kafka.serializer.KafkaAvroReflectSerializer;

public class KafkaConfiguration {

	private final AtomicBoolean closed = new AtomicBoolean(false);

	public enum Producer {
		BOOTSTRAP_SERVERS("bootstrap.servers"), SCHEMA_REGISTRY_URL("schema.registry.url"), KEY_SERIALIZER(
				"key.serializer"), VALUE_SERIALIZER("value.serializer"), LINGER_MS(
						"linger.ms"), ENABLE_IDEMPOTENCE("enable.idempotence"), TRANSACTIONAL_ID("transactional.id");

		String propertyName;

		Producer(String propName) {
			this.propertyName = propName;
		}

		@Override
		public String toString() {
			return propertyName;
		}
	}

	public enum Consumer {
		BOOTSTRAP_SERVERS("bootstrap.servers"), SCHEMA_REGISTRY_URL("schema.registry.url"), GROUP_ID(
				"group.id"), AUTO_OFFSET_RESET("auto.offset.reset"), COMMIT_INTERVAL_MS(
						"commit.interval.ms"), ISOLATION_LEVEL("isolation.level"), KEY_DESERIALIZER(
								"key.deserializer"), VALUE_DESERIALIZER(
										"value.deserializer"), ENABLE_AUTO_COMMIT("enable.auto.commit");

		String propertyName;

		Consumer(String propName) {
			this.propertyName = propName;
		}

		@Override
		public String toString() {
			return propertyName;
		}
	}

	public enum Streams {
		BOOTSTRAP_SERVERS("bootstrap.servers"), SCHEMA_REGISTRY_URL("schema.registry.url"), GROUP_ID(
				"group.id"), AUTO_OFFSET_RESET("auto.offset.reset"), COMMIT_INTERVAL_MS("commit.interval.ms"),

		APPLICATION_ID("application.id"), APPLICATION_SERVER("application.server"), STATE_DIR(
				"state.dir"), PROCESSING_GUARANTEE("processing.guarantee"), NUM_STREAM_THREADS("num.stream.threads");

		String propertyName;

		Streams(String propName) {
			this.propertyName = propName;
		}

		@Override
		public String toString() {
			return propertyName;
		}
	}

	public static Properties producerDefaultProperties() {
		Properties properties = new Properties();
		properties.put(Producer.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Producer.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Producer.KEY_SERIALIZER.toString(), StringSerializer.class.getName());
		properties.put(Producer.VALUE_SERIALIZER.toString(), KafkaAvroReflectSerializer.class.getName());
		properties.put(Producer.LINGER_MS.toString(), 10);
		properties.put(Producer.ENABLE_IDEMPOTENCE.toString(), true);
		properties.put(Producer.TRANSACTIONAL_ID.toString(), "transactionalId-1");

		return properties;
	}

	public static Properties consumerDefaultProperties() {
		Properties properties = new Properties();
		properties.put(Consumer.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Consumer.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Consumer.KEY_DESERIALIZER.toString(), StringDeserializer.class);
		properties.put(Consumer.VALUE_DESERIALIZER.toString(), CoreEventAvroDeserializer.class);
		properties.put(Consumer.ISOLATION_LEVEL.toString(), "read_committed");
		properties.put(Consumer.ENABLE_AUTO_COMMIT.toString(), "false");
		properties.put(Consumer.GROUP_ID.toString(), "consumer-group" + UUID.randomUUID());
		properties.put(Streams.AUTO_OFFSET_RESET.toString(), "earliest");

		return properties;
	}

	public static Properties streamsDefaultProperties() {
		Properties properties = new Properties();
		// application.id is used to set client.id, group.id and state.dir, see Kafka
		// Docs.
		properties.put(Streams.APPLICATION_ID.toString(), "streams-app-" + UUID.randomUUID());
		properties.put(Streams.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Streams.APPLICATION_SERVER.toString(), System.getenv("APPLICATION_SERVER")); // localhost:8093
		properties.put(Streams.STATE_DIR.toString(), "/tmp/kafka-streams");
		properties.put(Streams.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Streams.AUTO_OFFSET_RESET.toString(), "earliest");
		properties.put(Streams.PROCESSING_GUARANTEE.toString(), "exactly_once");
		properties.put("num.stream.threads", 4);

		return properties;
	}
}
