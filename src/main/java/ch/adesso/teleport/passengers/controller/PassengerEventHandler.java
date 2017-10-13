package ch.adesso.teleport.passengers.controller;

import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;

import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.kafka.producer.PublishedEvent;
import ch.adesso.teleport.kafka.store.ProcessedEvent;

@Startup
@Singleton
public class PassengerEventHandler {

	private static final Logger LOG = Logger.getLogger(PassengerEventHandler.class.getName());

	public void onEvent(@Observes CoreEvent event) {
		LOG.info("CoreEvent: " + event + " sucessfully processed.");
	}

	public void onEvent(@Observes PublishedEvent event) {
		LOG.info("PublishedEvent: " + event + " sucessfully processed.");
	}

	public void onEvent(@Observes ProcessedEvent event) {
		LOG.info("StoredEvent: " + event + " sucessfully processed.");
	}
}
