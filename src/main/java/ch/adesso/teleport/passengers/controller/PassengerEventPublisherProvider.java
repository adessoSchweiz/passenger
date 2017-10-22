package ch.adesso.teleport.passengers.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Produces;

import org.apache.kafka.clients.producer.KafkaProducer;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;

@Startup
@Singleton
public class PassengerEventPublisherProvider {

	private KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer;

	private KafkaEventPublisher passengerEventPublisher;

	@PassengerQualifier
	@Produces
	public KafkaEventPublisher getEvetnPublisher() {
		return passengerEventPublisher;
	}

	@PostConstruct
	public void init() {
		producer = new KafkaProducer<>(KafkaConfiguration.producerDefaultProperties());
		passengerEventPublisher = new KafkaEventPublisher(producer);
	}

	@PreDestroy
	public void close() {
		producer.close();
	}
}
