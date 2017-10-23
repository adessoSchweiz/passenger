package ch.adesso.teleport.persons.controller;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;

import org.apache.kafka.clients.producer.KafkaProducer;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import ch.adesso.teleport.kafka.producer.KafkaEventPublisher;

@Startup
@Singleton
public class PersonEventPublisherProvider {

	private KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer;

	private KafkaEventPublisher personEventPublisher;

	public KafkaEventPublisher getEventPublisher() {
		return personEventPublisher;
	}

	@PostConstruct
	public void init() {
		producer = new KafkaProducer<>(KafkaConfiguration.producerDefaultProperties());
		personEventPublisher = new KafkaEventPublisher(producer);
	}

	@PreDestroy
	public void close() {
		producer.close();
	}
}
