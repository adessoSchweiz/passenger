package ch.adesso.teleport.passengers.entity;

import javax.json.bind.annotation.JsonbProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CreditCard {

	@JsonbProperty("card_number")
	private String cardNumber;
	@JsonbProperty("card_type")
	private CreditCardTypeEnum cardType;
	@JsonbProperty("name_on_card")
	private String nameOnCard;
	@JsonbProperty("valid_to_month")
	private Integer validToMonth;
	@JsonbProperty("valid_to_year")
	private Integer validToYear;
	@JsonbProperty("secret_number")
	private Integer secretNumber;

	enum EventType {
		CREDITCARD_CREATED("creditcard_created"), CARD_NUMBER_CHANGED("card_number_changed"), CARD_TYPE_CHANGED(
				"card_type_changed"), NAME_ON_CARD_CHANGED("name_on_card_changed"), VALID_TO_MONTH_CHANGED(
						"valid_to_month_changed"), VALID_TO_YEAR_CHANGED(
								"valid_to_year_changed"), SECRET_NUMBER_CHANGED("secret_number_changed");

		private String type;

		private EventType(String type) {
			this.type = type;
		}

		@Override
		public String toString() {
			return type;
		}
	}
}
