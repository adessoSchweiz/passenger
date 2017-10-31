package ch.adesso.teleport.passengers.boundary;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.kafka.config.Topics;
import ch.adesso.teleport.passengers.controller.PassengerEventPublisherProvider;
import ch.adesso.teleport.passengers.controller.PassengerLocalStoreProvider;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.persons.boundary.PersonService;
import ch.adesso.teleport.persons.entity.Person;

@Stateless
public class PassengerService {

	private static final Logger LOG = Logger.getLogger(PassengerService.class.getName());

	@Inject
	private PassengerEventPublisherProvider passengerEventPublisherProvider;

	@Inject
	private PassengerLocalStoreProvider passengersLocalStoreProvider;

	@Inject
	private PersonService personService;

	/**
	 * passenger.id is same as person id and should be provided.
	 * 
	 * TODO: add validation for person existence
	 * 
	 */
	public Passenger createPassenger(Passenger passenger) {

		Person person = findPersonById(passenger.getId());
		if (person == null) {
			throw new EntityNotFoundException(String.format("Person [id = %s] not registered yet.", passenger.getId()));
		}

		Passenger storedPassenger = findPassengerById(passenger.getId());

		if (storedPassenger != null) {
			throw new RuntimeException(
					String.format("Person [id = %s] has already a passenger role.", passenger.getId()));
		}

		Passenger newPassenger = new Passenger(person.getId());

		newPassenger.updateFrom(passenger);

		save(newPassenger);
		return find(newPassenger);
	}

	public Passenger updatePassenger(Passenger passenger) {
		Passenger storedPassenger = find(passenger);
		storedPassenger.updateFrom(passenger);
		save(storedPassenger);
		return storedPassenger;
	}

	public void save(AggregateRoot passenger) {
		passengerEventPublisherProvider.getEvetnPublisher().save(Topics.PASSENGER_EVENT_TOPIC.toString(), passenger);
	}

	public Passenger find(Passenger passenger) {
		return passengersLocalStoreProvider.getKafkaLocalStore()
				.findByIdAndVersion(Topics.PASSENGER_AGGREGATE_STORE.toString(), passenger);
	}

	public Passenger findPassengerById(String passengerId) {
		return passengersLocalStoreProvider.getKafkaLocalStore()
				.loadAggregateFromLocalStore(Topics.PASSENGER_AGGREGATE_STORE.toString(), passengerId);
	}

	public Person findPersonById(String personId) {
		return personService.findPersonById(personId);
	}
}