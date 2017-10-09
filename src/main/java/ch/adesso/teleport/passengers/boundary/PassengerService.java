package ch.adesso.teleport.passengers.boundary;

import java.util.Properties;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.streams.KafkaStreams;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.kafka.KafkaConfiguration;
import ch.adesso.teleport.kafka.KafkaConfiguration.KeyValue;
import ch.adesso.teleport.kafka.KafkaConfiguration.Streams;
import ch.adesso.teleport.kafka.KafkaStore;
import ch.adesso.teleport.kafka.KafkaStoreBuilder;
import ch.adesso.teleport.kafka.StoreProcessor;
import ch.adesso.teleport.passengers.controller.Topics;
import ch.adesso.teleport.passengers.entity.Passenger;

@Startup
@Singleton
public class PassengerService {

	private static final Logger LOG = Logger.getLogger(PassengerService.class.getName());

	@Inject
	Event<CoreEvent> events;

	private KafkaStore kafkaStore;

	private KafkaProducer<String, CoreEvent> producer;

	private KafkaStreams kafkaStreams;

	@PostConstruct
	public void init() {

		producer = new KafkaProducer<String, CoreEvent>(KafkaConfiguration.producerProperties());
		producer.initTransactions();

		Properties props = KafkaConfiguration
				.streamsProperties(new KeyValue(Streams.STATE_DIR.toString(), "/tmp/kafka-streams/" + "passenger"));

		kafkaStreams = new KafkaStoreBuilder<Passenger>(Passenger.class, props)
				.withSourceTopicName(Topics.PASSENGER_EVENT_TOPIC.toString())
				.withStateStoreName(Topics.PASSENGER_AGGREGATE_STORE.toString())
				.withProcessorSupplier(() -> new StoreProcessor<Passenger>(Topics.PASSENGER_AGGREGATE_STORE.toString(),
						Passenger::new, events::fire))
				.build();

		kafkaStore = new KafkaStore(kafkaStreams, producer, null);
	}

	@PreDestroy
	public void close() {
		producer.close();
		kafkaStreams.close();
	}

	public Passenger createPassenger(Passenger passenger) {
		System.out.println("create passenger: " + passenger);
		LOG.info("create passenger: " + passenger);
		String passengerId = UUID.randomUUID().toString();
		Passenger newPassenger = new Passenger(passengerId);
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

	public <T extends AggregateRoot> void save(T aggregate) {
		kafkaStore.save(Topics.PASSENGER_EVENT_TOPIC.toString(), aggregate);
	}

	public <T extends AggregateRoot> T find(T aggregate) {
		return kafkaStore.findByIdAndVersionWaitForResult(Topics.PASSENGER_AGGREGATE_STORE.toString(), aggregate);
	}
}