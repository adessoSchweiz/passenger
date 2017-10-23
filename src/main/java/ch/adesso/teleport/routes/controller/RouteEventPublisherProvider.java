package ch.adesso.teleport.routes.controller;

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
public class RouteEventPublisherProvider {

	private KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer;

	private KafkaEventPublisher routeEventPublisher;

	public KafkaEventPublisher getEventPublisher() {
		return routeEventPublisher;
	}

	@PostConstruct
	public void init() {
		producer = new KafkaProducer<>(KafkaConfiguration.producerDefaultProperties());
		routeEventPublisher = new KafkaEventPublisher(producer);
	}

	@PreDestroy
	public void close() {
		producer.close();
	}
}
