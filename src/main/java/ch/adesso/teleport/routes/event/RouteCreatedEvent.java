package ch.adesso.teleport.routes.event;

import org.apache.avro.reflect.AvroDefault;
import org.apache.avro.reflect.Nullable;

import ch.adesso.teleport.routes.entity.CarType;
import ch.adesso.teleport.routes.entity.LatitudeLongitude;
import ch.adesso.teleport.routes.entity.RouteStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class RouteCreatedEvent extends RouteEvent {

	private String passengerId;
	private LatitudeLongitude from;
	private LatitudeLongitude to;

	@Nullable
	@AvroDefault("1")
	private int noOfPersons;

	private String carType;

	@Nullable
	private String passengerComment;

	@Nullable
	private String estimatedTime;

	@Nullable
	private String estimatedDistance;

	private String status;

	public RouteCreatedEvent(String aggregateId, long sequence, String passengerId, LatitudeLongitude from,
			LatitudeLongitude to, int noOfPersons, CarType carType, String passengerComments, String estimatedTime,
			String estimatedDistance, RouteStatus status) {
		super(RouteCreatedEvent.class, aggregateId, sequence);

		this.passengerId = passengerId;
		this.from = from;
		this.to = to;
		this.noOfPersons = noOfPersons;
		this.carType = carType != null ? carType.toString() : null;
		this.passengerComment = passengerComments;
		this.estimatedTime = estimatedTime;
		this.estimatedDistance = estimatedDistance;
		this.setStatus(status != null ? status.toString() : null);
	}
}
