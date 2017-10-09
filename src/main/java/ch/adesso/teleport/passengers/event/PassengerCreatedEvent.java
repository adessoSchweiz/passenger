package ch.adesso.teleport.passengers.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@NoArgsConstructor
public class PassengerCreatedEvent extends PassengerEvent {

	public PassengerCreatedEvent(String aggregateId) {
		super(PassengerCreatedEvent.class, aggregateId, 0);
	}
}
