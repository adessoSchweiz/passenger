package ch.adesso.teleport.passengers.entity;

import javax.json.bind.annotation.JsonbProperty;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Passenger extends AggregateRoot {

	enum EventType {
		PASSENGER_CREATED("passenger_created");

		private String type;

		private EventType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}

	@JsonbProperty(nillable = true)
	private CreditCard creditCard;

	public Passenger() {
		super();
	}

	public Passenger(String id) {
		super();
		applyChange(new CoreEvent(id, getNextVersion(), EventType.PASSENGER_CREATED.toString(), id));
	}

	public void updateFrom(Passenger passenger) {
		creditCard(passenger.getCreditCard());
	}

	public void newCreditCard() {
		applyChange(CreditCard.EventType.CREDITCARD_CREATED);
	}

	public void creditCard(CreditCard creditCard) {
		System.out.println("update from: " + creditCard);
		if (creditCard == null) {
			return;
		}

		if (getCreditCard() == null) {
			newCreditCard();
		}

		creditCardNumber(creditCard.getCardNumber());
		creditCardType(creditCard.getCardType());
		creditCardOwner(creditCard.getNameOnCard());
		creditCardSecretNumber(creditCard.getSecretNumber());
		creditCardValidToMonth(creditCard.getValidToMonth());
		creditCardValidToYear(creditCard.getValidToYear());
	}

	public void creditCardNumber(String creditCardNumber) {
		applyChange(CreditCard.EventType.CARD_NUMBER_CHANGED, creditCardNumber, getCreditCard().getCardNumber());
	}

	public void creditCardType(CreditCardTypeEnum cardType) {
		applyChange(CreditCard.EventType.CARD_TYPE_CHANGED, cardType != null ? cardType.toString() : null,
				getCreditCard().getCardType() != null ? getCreditCard().getCardType().toString() : null);
	}

	public void creditCardOwner(String cardOwner) {
		applyChange(CreditCard.EventType.NAME_ON_CARD_CHANGED, cardOwner, getCreditCard().getNameOnCard());
	}

	public void creditCardValidToMonth(int month) {
		applyChange(CreditCard.EventType.VALID_TO_MONTH_CHANGED, Integer.valueOf(month),
				getCreditCard().getValidToMonth());
	}

	public void creditCardValidToYear(int year) {
		applyChange(CreditCard.EventType.VALID_TO_YEAR_CHANGED, Integer.valueOf(year),
				getCreditCard().getValidToYear());
	}

	public void creditCardSecretNumber(int secretNumber) {
		applyChange(CreditCard.EventType.SECRET_NUMBER_CHANGED, Integer.valueOf(secretNumber),
				getCreditCard().getSecretNumber());
	}

	@Override
	protected void initHandlers() {
		addHandler(EventType.PASSENGER_CREATED, e -> this.setId(e.getValue()));
		addHandler(CreditCard.EventType.CREDITCARD_CREATED, e -> this.setCreditCard(new CreditCard()));
		addHandler(CreditCard.EventType.CARD_NUMBER_CHANGED, e -> this.getCreditCard().setCardNumber(e.getValue()));
		addHandler(CreditCard.EventType.CARD_TYPE_CHANGED,
				e -> this.getCreditCard().setCardType(e.toEnum(CreditCardTypeEnum.class)));
		addHandler(CreditCard.EventType.NAME_ON_CARD_CHANGED, e -> this.getCreditCard().setNameOnCard(e.getValue()));
		addHandler(CreditCard.EventType.VALID_TO_MONTH_CHANGED, e -> this.getCreditCard().setValidToMonth(e.toInt()));
		addHandler(CreditCard.EventType.VALID_TO_YEAR_CHANGED, e -> this.getCreditCard().setValidToYear(e.toInt()));
		addHandler(CreditCard.EventType.SECRET_NUMBER_CHANGED, e -> this.getCreditCard().setSecretNumber(e.toInt()));
	}

}
