package ch.adesso.teleport.passengers.boundary;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import com.airhacks.porcupine.execution.boundary.Dedicated;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.consumer.KafkaConsumerRunner;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;
import ch.adesso.teleport.kafka.store.KafkaEventStore;
import ch.adesso.teleport.kafka.store.ProcessedEvent;
import ch.adesso.teleport.kafka.store.ProcessedEventBlocker;
import ch.adesso.teleport.passengers.controller.PassengerQualifier;
import ch.adesso.teleport.passengers.entity.Passenger;
import io.reactivex.subjects.PublishSubject;

@Stateless
public class PassengerService {

	private static final Logger LOG = Logger.getLogger(PassengerService.class.getName());

	@Inject
	KafkaEventPublisher eventPublisher;

	@PassengerQualifier
	@Inject
	private KafkaEventStore kafkaLocalStore;

	@PassengerQualifier
	@Inject
	private PublishSubject<ProcessedEvent> rxPublishSubject;

	@Inject
	private Event<CoreEvent> coreEvents;

	@Inject
	@Dedicated
	ExecutorService passengerConsumerPool;

	private KafkaConsumerRunner<CoreEvent> consumer;

	@PostConstruct
	public void init() {
		consumer = new KafkaConsumerRunner<CoreEvent>(KafkaConfiguration.consumerDefaultProperties(), coreEvents::fire,
				Topics.PASSENGER_EVENT_TOPIC.toString());

		// CompletableFuture.runAsync(consumer);
		CompletableFuture.runAsync(consumer, passengerConsumerPool);
	}

	@PreDestroy
	public void close() {
		consumer.shutdown();
	}

	/**
	 * passenger.id is same as person id and should be provided.
	 * 
	 * TODO: add validation for person existence
	 * 
	 */
	public Passenger createPassenger(Passenger passenger) {
		String passengerId = UUID.randomUUID().toString();
		Passenger newPassenger = new Passenger(passengerId);
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

	public void waitForLastStoredEvent(Passenger newPassenger) {
		// wait till last event were stored in the local store
		ProcessedEventBlocker blocker = new ProcessedEventBlocker(rxPublishSubject) {

			@Override
			public boolean whenCondition(ProcessedEvent event) {
				return event.getEvent().getAggregateId().equals(newPassenger.getId())
						&& event.getEvent().getSequence() == newPassenger.getVersion();
			}

			@Override
			public void execute() {
				save(newPassenger);
			}
		};

		try { // 5 sec. must be OK to complete
			blocker.waitFor(5000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			// as fallback, lets try to poll the store (probably this is the first call and
			// store is not yet initialized ?)
			kafkaLocalStore.findByIdAndVersionWaitForResult(Topics.PASSENGER_AGGREGATE_STORE.toString(), newPassenger);
		}
	}

	public <T extends AggregateRoot> void save(T aggregate) {
		eventPublisher.save(Topics.PASSENGER_EVENT_TOPIC.toString(), aggregate);
	}

	public <T extends AggregateRoot> T find(T aggregate) {
		return kafkaLocalStore.findByIdAndVersion(Topics.PASSENGER_AGGREGATE_STORE.toString(), aggregate.getId(),
				aggregate.getVersion());
	}

	public Passenger find(String aggregateId) {
		return kafkaLocalStore.loadAggregateFromLocalStore(Topics.PASSENGER_AGGREGATE_STORE.toString(), aggregateId);
	}

}