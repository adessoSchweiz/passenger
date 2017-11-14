package ch.adesso.teleport.passengers.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.kafka.clients.producer.KafkaProducer;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;
import ch.adesso.teleport.passengers.event.PassengerEvent;
import ch.adesso.teleport.passengers.event.PassengerEventEnvelope;

@Startup
@Singleton
public class PassengerEventPublisherProvider {

	private KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer;

	private KafkaEventPublisher passengerEventPublisher;

	public KafkaEventPublisher getEvetnPublisher() {
		return passengerEventPublisher;
	}

	@PostConstruct
	public void init() {
		producer = new KafkaProducer<>(KafkaConfiguration.producerDefaultProperties());
		passengerEventPublisher = new KafkaEventPublisher(producer,
				e -> new PassengerEventEnvelope((PassengerEvent) e));
	}

	@PreDestroy
	public void close() {
		producer.close();
	}
}
