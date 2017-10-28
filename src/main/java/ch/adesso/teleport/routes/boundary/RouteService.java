package ch.adesso.teleport.routes.boundary;

import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import ch.adesso.teleport.kafka.config.Topics;
import ch.adesso.teleport.passengers.boundary.PassengerService;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.routes.controller.AcceptRouteTimer;
import ch.adesso.teleport.routes.controller.RouteEventPublisherProvider;
import ch.adesso.teleport.routes.controller.RouteLocalStoreProvider;
import ch.adesso.teleport.routes.entity.Route;
import ch.adesso.teleport.routes.entity.RouteStatus;

@Stateless
public class RouteService {

	private static final Logger LOG = Logger.getLogger(RouteService.class.getName());

	@Inject
	private PassengerService passengerService;

	@Inject
	private RouteEventPublisherProvider routeEventPublisherProvider;

	@Inject
	private RouteLocalStoreProvider routesLocalStoreProvider;

	@Inject
	private AcceptRouteTimer acceptRouteTimer;

	public Route createRoute(Route route) {

		Passenger passenger = findPassengerById(route.getPassengerId());

		if (passenger == null) {
			throw new EntityNotFoundException("Passenger not found.");
		}

		Route newRoute = Route.newRoute(route.getPassengerId(), route.getFrom(), route.getTo(), route.getNoOfPersons(),
				route.getCarType(), route.getPassengerComment(), route.getEstimatedTime(),
				route.getEstimatedDistance());

		save(newRoute);

		// trigger accept route
		acceptRouteTimer.triggerRouteAcceptedEvent(newRoute.getId());

		return newRoute;
	}

	public void cancelRoute(String routeId) {

		Route route = findRouteById(routeId);
		if (route == null) {
			throw new EntityNotFoundException(String.format("Route [id = %s] not found.", routeId));
		}
		if (route.hasStatus(RouteStatus.ACCEPTED)) {
			throw new RuntimeException("Route already accepted.");
		}

		route.cancelRoute();
		save(route);
	}

	public void save(Route route) {
		routeEventPublisherProvider.getEventPublisher().save(Topics.ROUTE_EVENT_TOPIC.toString(), route);
	}

	public Passenger findPassengerById(String passengerId) {
		if (passengerId == null) {
			return null;
		}
		return passengerService.findPassengerById(passengerId);
	}

	public Route findRouteById(String routeId) {
		return routesLocalStoreProvider.getKafkaLocalStore().findById(Topics.ROUTE_AGGREGATE_STORE.toString(), routeId);
	}
}