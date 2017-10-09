package ch.adesso.teleport.passengers.event;

import org.apache.avro.reflect.Union;

import ch.adesso.teleport.CoreEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@ToString(callSuper = true)
@Union({ PassengerCreatedEvent.class, PassengerChangedEvent.class, CreditCardCreatedEvent.class,
		CreditCardChangedEvent.class })
public class PassengerEvent extends CoreEvent {

	public PassengerEvent(Class<?> eventType, String aggregateId, long sequence) {
		super(aggregateId, sequence, eventType.getSimpleName(), null);
	}
}
