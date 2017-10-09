package ch.adesso.teleport.kafka;

import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import lombok.AllArgsConstructor;
import lombok.Data;

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

	@Data
	@AllArgsConstructor
	public static class KeyValue {
		private String name;
		private Object value;
	}

	public static Properties producerProperties(KeyValue... kv) {
		Properties properties = new Properties();
		properties.put(Producer.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Producer.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Producer.KEY_SERIALIZER.toString(), StringSerializer.class.getName());
		properties.put(Producer.VALUE_SERIALIZER.toString(), KafkaAvroReflectSerializer.class.getName());
		properties.put(Producer.LINGER_MS.toString(), 10);
		properties.put(Producer.ENABLE_IDEMPOTENCE.toString(), true);
		properties.put(Producer.TRANSACTIONAL_ID.toString(), "transactionalId-1");

		for (KeyValue keyValue : kv) {
			properties.put(keyValue.getName(), keyValue.getValue());
		}
		return properties;
	}

	public static Properties consumerProperties(KeyValue... kv) {
		Properties properties = new Properties();
		properties.put(Consumer.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Consumer.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Consumer.KEY_DESERIALIZER.toString(), StringDeserializer.class.getName());
		properties.put(Consumer.VALUE_DESERIALIZER.toString(), CoreEventAvroDeserializer.class.getName());
		properties.put(Consumer.ISOLATION_LEVEL.toString(), "read_commited");
		properties.put(Consumer.ENABLE_AUTO_COMMIT.toString(), "false");
		properties.put(Consumer.GROUP_ID.toString(), "consumer-group" + UUID.randomUUID());

		for (KeyValue keyValue : kv) {
			properties.put(keyValue.getName(), keyValue.getValue());
		}

		return properties;
	}

	public static Properties streamsProperties(KeyValue... kv) {
		Properties properties = new Properties();
		properties.put(Streams.APPLICATION_ID.toString(), "streams-app");
		properties.put(Streams.BOOTSTRAP_SERVERS.toString(), System.getenv("BOOTSTRAP_SERVERS")); // kafka:9092
		properties.put(Streams.APPLICATION_SERVER.toString(), System.getenv("APPLICATION_SERVER")); // localhost:8093
		properties.put(Streams.STATE_DIR.toString(), "/tmp/kafka-streams");
		properties.put(Streams.SCHEMA_REGISTRY_URL.toString(), System.getenv("SCHEMA_REGISTRY_URL")); // http://avro-schema-registry:8081
		properties.put(Streams.GROUP_ID.toString(), "streams-group" + UUID.randomUUID());
		properties.put(Streams.AUTO_OFFSET_RESET.toString(), "earliest");
		properties.put(Streams.COMMIT_INTERVAL_MS.toString(), 20);
		properties.put(Streams.PROCESSING_GUARANTEE.toString(), "exactly_once");

		for (KeyValue keyValue : kv) {
			properties.put(keyValue.getName(), keyValue.getValue());
		}
		return properties;
	}
}
