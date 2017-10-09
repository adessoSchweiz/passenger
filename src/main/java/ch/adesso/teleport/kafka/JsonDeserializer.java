package ch.adesso.teleport.kafka;

import java.util.Map;

import org.apache.kafka.common.serialization.Deserializer;

import ch.adesso.teleport.JsonConverter;

public class JsonDeserializer<T> implements Deserializer<T> {

	private Class<T> clazz;

	public JsonDeserializer(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public void configure(Map<String, ?> configs, boolean isKey) {
	}

	@Override
	public T deserialize(String topic, byte[] data) {
		if (data == null) {
			return null;
		}
		return JsonConverter.fromByteArray(data, clazz);
	}

	@Override
	public void close() {
	}

}
