package ch.adesso.teleport.passengers.event;

import ch.adesso.teleport.EventEnvelope;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString(callSuper = true)
@Data
public class PassengerEventEnvelope implements EventEnvelope<PassengerEvent> {

	@Getter
	private PassengerEvent event;

	public PassengerEventEnvelope(PassengerEvent event) {
		this.event = event;
	}

}
