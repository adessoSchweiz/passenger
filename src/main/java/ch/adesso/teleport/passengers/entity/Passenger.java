package ch.adesso.teleport.passengers.entity;

import javax.json.bind.annotation.JsonbProperty;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;
import ch.adesso.teleport.passengers.event.CreditCardChangedEvent;
import ch.adesso.teleport.passengers.event.CreditCardCreatedEvent;
import ch.adesso.teleport.passengers.event.PassengerCreatedEvent;
import ch.adesso.teleport.passengers.event.PassengerEvent;
import ch.adesso.teleport.passengers.event.PassengerEventEnvelope;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Passenger extends AggregateRoot {

	@JsonbProperty(nillable = true)
	private CreditCard creditCard;

	public Passenger() {
		super();
	}

	public Passenger(String id) {
		super();
		applyChange(new PassengerCreatedEvent(id));
	}

	public void updateFrom(Passenger passenger) {
		if (wasChanged(getCreditCard(), passenger.getCreditCard())) {
			updateCreditCard(passenger.getCreditCard());
		}
	}

	// ---------- business commands ---------------//
	public void updateCreditCard(CreditCard creditCard) {
		if (creditCard == null) {
			return;
		}

		if (getCreditCard() == null) {
			applyChange(new CreditCardCreatedEvent(getId(), getNextVersion(), creditCard.getCardNumber(),
					creditCard.getCardType(), creditCard.getNameOnCard(), creditCard.getValidToMonth(),
					creditCard.getValidToYear(), creditCard.getSecretNumber()));
		} else {
			applyChange(new CreditCardChangedEvent(getId(), getNextVersion(), creditCard.getCardNumber(),
					creditCard.getCardType(), creditCard.getNameOnCard(), creditCard.getValidToMonth(),
					creditCard.getValidToYear(), creditCard.getSecretNumber()));
		}
	}

	// -------------------- modifications ----------- //
	private void on(PassengerCreatedEvent event) {
		setId(event.getAggregateId());
	}

	private void on(CreditCardCreatedEvent event) {
		CreditCard creditCard = new CreditCard(event.getCardNumber(), CreditCardTypeEnum.valueOf(event.getCardType()),
				event.getNameOnCard(), event.getValidToMonth(), event.getValidToMonth(), event.getSecretNumber());
		setCreditCard(creditCard);
	}

	private void on(CreditCardChangedEvent event) {
		CreditCard creditCard = new CreditCard(event.getCardNumber(), CreditCardTypeEnum.valueOf(event.getCardType()),
				event.getNameOnCard(), event.getValidToMonth(), event.getValidToMonth(), event.getSecretNumber());
		setCreditCard(creditCard);
	}

	@Override
	protected EventEnvelope<? extends CoreEvent> wrapEventIntoEnvelope(CoreEvent event) {
		return new PassengerEventEnvelope((PassengerEvent) event);
	}
}
