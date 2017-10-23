package ch.adesso.teleport.routes.event;

import ch.adesso.teleport.routes.entity.RouteStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class RouteStatusChangedEvent extends RouteEvent {

	private String status;

	public RouteStatusChangedEvent(String aggregateId, long sequence, RouteStatus status) {
		super(RouteCreatedEvent.class, aggregateId, sequence);
		this.status = status.toString();
	}
}
