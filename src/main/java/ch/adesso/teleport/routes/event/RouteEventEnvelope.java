package ch.adesso.teleport.routes.event;

import ch.adesso.teleport.EventEnvelope;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
@Data
public class RouteEventEnvelope implements EventEnvelope<RouteEvent> {

	@Getter
	private RouteEvent event;

	public RouteEventEnvelope(RouteEvent event) {
		this.event = event;
	}

}
