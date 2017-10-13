package ch.adesso.teleport.passengers.controller;

import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.Topics;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.consumer.KafkaConsumerRunner;

//@Startup
//@Singleton
public class PassengerConsumer {

	@Inject
	private Event<CoreEvent> coreEvents;

	// @Inject
	// @Dedicated
	// ExecutorService passengerConsumerPool;

	private KafkaConsumerRunner<CoreEvent> consumer;

	@PostConstruct
	public void init() {
		consumer = new KafkaConsumerRunner<CoreEvent>(KafkaConfiguration.consumerDefaultProperties(), coreEvents::fire,
				Topics.PASSENGER_EVENT_TOPIC.toString());

		CompletableFuture.runAsync(consumer);
		// CompletableFuture.runAsync(consumer, passengerConsumerPool);
	}

	@PreDestroy
	public void close() {
		consumer.shutdown();
	}

}
