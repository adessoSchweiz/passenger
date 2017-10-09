package ch.adesso.teleport.persons.boundary;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;

import org.apache.kafka.streams.KafkaStreams;

import ch.adesso.teleport.kafka.KafkaConfiguration;
import ch.adesso.teleport.kafka.KafkaStore;
import ch.adesso.teleport.kafka.KafkaStoreBuilder;
import ch.adesso.teleport.kafka.StoreProcessor;
import ch.adesso.teleport.passengers.controller.Topics;
import ch.adesso.teleport.persons.entity.Person;

@Stateless
public class PersonService {

	private KafkaStore kafkaStore;

	private KafkaStreams kafkaStreams;

	@PostConstruct
	public void init() {
		kafkaStreams = new KafkaStoreBuilder<Person>(Person.class, KafkaConfiguration.streamsProperties())
				.withSourceTopicName(Topics.PERSON_EVENT_TOPIC.toString())
				.withStateStoreName(Topics.PERSON_AGGREGATE_STORE.toString())
				.withProcessorSupplier(
						() -> new StoreProcessor<Person>(Topics.PERSON_AGGREGATE_STORE.toString(), Person::new, null))
				.build();

		// we need only the query functionality
		kafkaStore = new KafkaStore(kafkaStreams, null, null);
	}

	@PreDestroy
	public void close() {
		kafkaStreams.close();
	}

	public Person find(String aggregateId) {
		return kafkaStore.findById(Topics.PERSON_AGGREGATE_STORE.toString(), aggregateId);
	}
}