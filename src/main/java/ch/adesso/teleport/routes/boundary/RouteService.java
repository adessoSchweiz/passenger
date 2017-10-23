package ch.adesso.teleport.routes.boundary;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityNotFoundException;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.Topics;
import ch.adesso.teleport.passengers.boundary.PassengerService;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.routes.controller.RouteEventPublisherProvider;
import ch.adesso.teleport.routes.controller.RouteLocalStoreProvider;
import ch.adesso.teleport.routes.entity.Route;

@Stateless
public class RouteService {

	@Inject
	private PassengerService passengerService;

	@Inject
	private RouteEventPublisherProvider routeEventPublisherProvider;

	@Inject
	private RouteLocalStoreProvider routesLocalStoreProvider;

	public Route createRoute(Route route) {

		Passenger passenger = findPassengerById(route.getPassengerId());

		if (passenger == null) {
			throw new EntityNotFoundException("Passenger not found.");
		}

		Route newRoute = Route.newRoute(route.getPassengerId(), route.getFrom(), route.getTo(), route.getNoOfPersons(),
				route.getCarType(), route.getPassengerComment(), route.getEstimatedTime(),
				route.getEstimatedDistance());

		save(newRoute);
		return newRoute;
	}

	public void cancelRoute(String routeId) {

		Route route = findRouteById(routeId);
		if (route == null) {
			throw new EntityNotFoundException("Route not found.");
		}
		route.cancelRoute();
		save(route);
	}

	public <T extends AggregateRoot> void save(T aggregate) {
		routeEventPublisherProvider.getEventPublisher().save(Topics.ROUTE_EVENT_TOPIC.toString(), aggregate);
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