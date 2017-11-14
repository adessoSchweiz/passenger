package ch.adesso.teleport.routes.entity;

import java.util.UUID;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.routes.event.RouteCreatedEvent;
import ch.adesso.teleport.routes.event.RouteStatusChangedEvent;
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

	public boolean hasStatus(RouteStatus status) {
		return this.status == status;
	}

	public void cancelRoute() {
		applyChange(new RouteStatusChangedEvent(getId(), getNextVersion(), RouteStatus.CANCELLED));
	}

	public void acceptRoute() {
		applyChange(new RouteStatusChangedEvent(getId(), getNextVersion(), RouteStatus.ACCEPTED));
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

	private void on(RouteStatusChangedEvent event) {
		this.setStatus(RouteStatus.valueOf(event.getStatus()));
	}

}
