package ch.adesso.teleport.passengers.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.AdditionalAnswers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.adesso.teleport.kafka.KafkaStore;
import ch.adesso.teleport.passengers.entity.CreditCard;
import ch.adesso.teleport.passengers.entity.CreditCardTypeEnum;
import ch.adesso.teleport.passengers.entity.Passenger;

@RunWith(MockitoJUnitRunner.class)
public class PassengerServiceTest {

	@Mock
	private KafkaStore kafkaStore;

	@InjectMocks
	private PassengerService passengerService;

	@Test
	public void testCreatePassenger() {

		when(kafkaStore.findByIdAndVersionWaitForResult(any(String.class), any(Passenger.class)))
				.then(AdditionalAnswers.returnsSecondArg());

		// fire created event
		Passenger passenger = new Passenger("111");
		assertThat(passenger.getId(), notNullValue());
		assertThat(passenger.getUncommitedEvents().size(), is(1));
		passenger.clearEvents();

		// fire 7 events (credit card creates + all properties of credit card)
		passenger.creditCard(new CreditCard("1234", CreditCardTypeEnum.VISA, "name", 1, 2020, 123));
		assertThat(passenger.getUncommitedEvents().size(), is(7));

		Passenger p = passengerService.createPassenger(passenger);
		assertThat(p.getId(), notNullValue());
		assertThat(p.getUncommitedEvents().size(), is(8));
	}

}
