package ch.adesso.teleport.passengers.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class CreditCard {

	private String cardNumber;
	private CreditCardType cardType;
	private String nameOnCard;
	private Integer validToMonth;
	private Integer validToYear;
	private Integer secretNumber;
}
