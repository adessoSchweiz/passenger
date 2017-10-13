package ch.adesso.teleport.persons.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.apache.kafka.streams.KafkaStreams;

import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.serializer.KafkaJsonDeserializer;
import ch.adesso.teleport.kafka.serializer.KafkaJsonSerializer;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.kafka.store.KafkaStoreProcessor;
import ch.adesso.teleport.kafka.store.KafkaStoreBuilder;
import ch.adesso.teleport.kafka.store.ProcessedEvent;
import ch.adesso.teleport.persons.entity.Person;

//@Singleton
public class PersonLocalStoreProvider {

	@Inject
	Event<ProcessedEvent> stateStoreProcessedEvents;

	private KafkaStreams kafkaStreams;
	private KafkaEventStore kafkaLocalStore;

	@PostConstruct
	public void init() {

		kafkaStreams = new KafkaStoreBuilder<Person>().withStoreSerializer(new KafkaJsonSerializer<>())
				.withStoreDerializer(new KafkaJsonDeserializer<>(Person.class))
				.withSourceTopicName(Topics.PERSON_EVENT_TOPIC.toString())
				.withStateStoreName(Topics.PERSON_AGGREGATE_STORE.toString())
				.withProcessorSupplier(
						() -> new KafkaStoreProcessor<Person>(Topics.PERSON_AGGREGATE_STORE.toString(),
								Person::new, stateStoreProcessedEvents::fire))
				.build();

		kafkaLocalStore = new KafkaEventStore(kafkaStreams);
	}

	@PreDestroy
	public void close() {
		this.kafkaStreams.close();
	}

	@PersonQualifier
	@Produces
	public KafkaEventStore getKafkaLocalStore() {
		return kafkaLocalStore;
	}
}
