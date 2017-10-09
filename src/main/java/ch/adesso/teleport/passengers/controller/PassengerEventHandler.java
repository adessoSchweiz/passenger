package ch.adesso.teleport.passengers.controller;

import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Observes;

import ch.adesso.teleport.CoreEvent;

@Startup
@Singleton
public class PassengerEventHandler {

	private static final Logger LOG = Logger.getLogger(PassengerEventHandler.class.getName());

	public void onEvent(@Observes CoreEvent event) {
		LOG.info("Event: " + event + " sucessfully processed.");
	}
}
