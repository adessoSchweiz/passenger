package ch.adesso.teleport.routes.controller;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import ch.adesso.teleport.kafka.config.Topics;
import ch.adesso.teleport.routes.entity.Route;
import ch.adesso.teleport.routes.entity.RouteStatus;

@Stateless
public class AcceptRouteTimer {

	private static final Logger LOG = Logger.getLogger(AcceptRouteTimer.class.getName());

	@Resource
	private TimerService timerService;

	@Inject
	private RouteEventPublisherProvider routeEventPublisherProvider;

	@Inject
	private RouteLocalStoreProvider routesLocalStoreProvider;

	/**
	 * simulate driver
	 * 
	 */
	public void triggerRouteAcceptedEvent(String routeId) {
		TimerConfig timerConfig = new TimerConfig(routeId, false);

		// 10 - 30 sec
		int rnd = (new Random().nextInt(2) + 10) * 1000;
		timerService.createSingleActionTimer(rnd, timerConfig);

		LOG.log(Level.INFO, "Trigger acceptRoute in: " + rnd + " sec.");
	}

	/**
	 * this usually would be triggered by the driver app
	 */
	@Timeout
	private void acceptRoute(Timer timer) {
		String routeId = (String) timer.getInfo();

		Route route = findRouteById(routeId);
		if (route == null) {
			throw new EntityNotFoundException(String.format("Route [id = %s] not found.", routeId));
		}

		if (!route.hasStatus(RouteStatus.CANCELLED)) {
			route.acceptRoute();
			save(route);

			LOG.log(Level.INFO, "Route accepted.");
		}
	}

	private Route findRouteById(String routeId) {
		return routesLocalStoreProvider.getKafkaLocalStore().findById(Topics.ROUTE_AGGREGATE_STORE.toString(), routeId);
	}

	private void save(Route route) {
		routeEventPublisherProvider.getEventPublisher().save(Topics.ROUTE_EVENT_TOPIC.toString(), route);
	}
}
