package ch.adesso.teleport.persons.boundary;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.adesso.teleport.kafka.config.Topics;
import ch.adesso.teleport.persons.controller.PersonLocalStoreProvider;
import ch.adesso.teleport.persons.entity.Person;

@Stateless
public class PersonService {

	@Inject
	private PersonLocalStoreProvider personsLocalStoreProvider;

	public Person findPersonById(String personId) {
		return personsLocalStoreProvider.getKafkaLocalStore().findById(Topics.PERSON_AGGREGATE_STORE.toString(),
				personId);
	}
}