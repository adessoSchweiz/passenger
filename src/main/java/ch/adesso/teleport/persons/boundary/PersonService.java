package ch.adesso.teleport.persons.boundary;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.persons.controller.PersonQualifier;
import ch.adesso.teleport.persons.entity.Person;

@Stateless
public class PersonService {

	@PersonQualifier
	@Inject
	private KafkaEventPublisher personEventPublisher;

	@PersonQualifier
	@Inject
	private KafkaEventStore personsKafkaLocalStore;

	public Person createPerson(Person person) {
		String personId = UUID.randomUUID().toString();
		Person newPerson = new Person(personId);

		newPerson.updatefrom(person);

		personEventPublisher.save(Topics.PERSON_EVENT_TOPIC.toString(), newPerson);
		return newPerson;
	}

	public Person updatePerson(String personId, Person person) {
		return person;
	}

	public Person changePersonStatus(String personId, String status) {
		return new Person();
	}

	public Person findPersonById(String personId) {
		return personsKafkaLocalStore.findById(Topics.PERSON_AGGREGATE_STORE.toString(), personId);
	}
}