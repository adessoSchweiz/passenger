package ch.adesso.teleport.passengers.boundary;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.kafka.store.ProcessedEvent;
import ch.adesso.teleport.kafka.store.ProcessedEventBlocker;
import ch.adesso.teleport.passengers.controller.PassengerQualifier;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.persons.boundary.PersonService;
import ch.adesso.teleport.persons.entity.Person;
import io.reactivex.subjects.PublishSubject;

@Stateless
public class PassengerService {

	private static final Logger LOG = Logger.getLogger(PassengerService.class.getName());

	@PassengerQualifier
	@Inject
	private KafkaEventPublisher passengerEventPublisher;

	@PassengerQualifier
	@Inject
	private KafkaEventStore passengersLocalStore;

	@PassengerQualifier
	@Inject
	private PublishSubject<ProcessedEvent> rxPublishSubject;

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

		Passenger newPassenger = new Passenger(person.getId());

		newPassenger.updateFrom(passenger);

		// wait till last event were stored in the local store
		waitForLastStoredEvent(newPassenger);

		return find(newPassenger);

	}

	public Passenger updatePassenger(Passenger passenger) {
		Passenger storedPassenger = find(passenger);
		storedPassenger.updateFrom(passenger);
		save(storedPassenger);
		return storedPassenger;
	}

	public void waitForLastStoredEvent(AggregateRoot aggregateRoot) {
		// wait till last event were stored in the local store
		ProcessedEventBlocker blocker = new ProcessedEventBlocker(rxPublishSubject) {

			@Override
			public boolean whenCondition(ProcessedEvent event) {
				return event.getEvent().getAggregateId().equals(aggregateRoot.getId())
						&& event.getEvent().getSequence() == aggregateRoot.getVersion();
			}

			@Override
			public void execute() {
				save(aggregateRoot);
			}
		};

		try { // 5 sec. must be OK to complete
			blocker.waitFor(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// store is not yet initialized ?)
		}
	}

	public void save(AggregateRoot passenger) {
		passengerEventPublisher.save(Topics.PASSENGER_EVENT_TOPIC.toString(), passenger);
	}

	public Passenger find(Passenger passenger) {
		return passengersLocalStore.findByIdAndVersion(Topics.PASSENGER_AGGREGATE_STORE.toString(), passenger);
	}

	public Passenger findPassengerById(String passengerId) {
		return passengersLocalStore.loadAggregateFromLocalStore(Topics.PASSENGER_AGGREGATE_STORE.toString(),
				passengerId);
	}

	public Person findPersonById(String personId) {
		return personService.findPersonById(personId);
	}
}