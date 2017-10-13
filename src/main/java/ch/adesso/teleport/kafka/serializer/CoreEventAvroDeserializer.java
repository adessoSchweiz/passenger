package ch.adesso.teleport.kafka.serializer;

import ch.adesso.teleport.CoreEvent;

public class CoreEventAvroDeserializer extends KafkaAvroReflectDeserializer<CoreEvent> {
	public CoreEventAvroDeserializer() {
		super(CoreEvent.class);
	}

}
