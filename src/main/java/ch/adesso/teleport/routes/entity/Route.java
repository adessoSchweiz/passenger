package ch.adesso.teleport.routes.entity;

import java.util.UUID;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;
import ch.adesso.teleport.routes.event.RouteCancelledEvent;
import ch.adesso.teleport.routes.event.RouteCreatedEvent;
import ch.adesso.teleport.routes.event.RouteEvent;
import ch.adesso.teleport.routes.event.RouteEventEnvelope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Route extends AggregateRoot {

	private String passengerId;

	private LatitudeLongitude from;
	private LatitudeLongitude to;
	private int noOfPersons;
	private CarType carType;
	private String passengerComment;
	private String estimatedTime;
	private String estimatedDistance;

	private RouteStatus status;

	public Route() {
		super();
	}

	public static Route newRoute(String passengerId, LatitudeLongitude from, LatitudeLongitude to, int noOfPersons,
			CarType carType, String passengerComments, String estimatedTime, String estimatedDistance) {

		String routeId = UUID.randomUUID().toString();
		if (carType == null) {
			carType = CarType.STANDARD;
		}

		Route route = new Route();
		route.applyChange(new RouteCreatedEvent(routeId, 0, passengerId, from, to, noOfPersons, carType,
				passengerComments, estimatedTime, estimatedDistance, RouteStatus.CREATED));

		return route;
	}

	public void cancelRoute() {
		applyChange(new RouteCancelledEvent(getId(), getNextVersion()));
	}

	private void on(RouteCreatedEvent event) {
		this.setId(event.getAggregateId());
		this.setVersion(0);
		this.setPassengerId(event.getPassengerId());
		this.setPassengerComment(event.getPassengerComment());
		this.setCarType(event.getCarType() != null ? CarType.valueOf(event.getCarType()) : null);
		this.setNoOfPersons(event.getNoOfPersons());
		this.setFrom(event.getFrom());
		this.setTo(event.getTo());
		this.setEstimatedTime(event.getEstimatedTime());
		this.setEstimatedDistance(event.getEstimatedDistance());
		this.setStatus(event.getStatus() != null ? RouteStatus.valueOf(event.getStatus()) : null);
	}

	private void on(RouteCancelledEvent event) {
		this.setStatus(RouteStatus.CANCELLED);
	}

	@Override
	protected EventEnvelope<? extends CoreEvent> wrapEventIntoEnvelope(CoreEvent event) {
		return new RouteEventEnvelope((RouteEvent) event);
	}

}
