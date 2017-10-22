package ch.adesso.teleport.routes.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class RouteCancelledEvent extends RouteEvent {

	public RouteCancelledEvent(String aggregateId, long sequence) {
		super(RouteCreatedEvent.class, aggregateId, sequence);
	}
}
