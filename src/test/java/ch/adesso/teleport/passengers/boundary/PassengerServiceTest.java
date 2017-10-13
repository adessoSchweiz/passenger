package ch.adesso.teleport.passengers.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.adesso.teleport.passengers.entity.CreditCard;
import ch.adesso.teleport.passengers.entity.CreditCardTypeEnum;
import ch.adesso.teleport.passengers.entity.Passenger;

@RunWith(MockitoJUnitRunner.class)
public class PassengerServiceTest {

	@Test
	public void testCreatePassenger() {

		Passenger input = new Passenger();
		input.creditCard(new CreditCard("1234", CreditCardTypeEnum.VISA, "name", 1, 2020, 123));
		input.setVersion(0);
		input.clearEvents();

		PassengerService passengerService = spy(PassengerService.class);

		// ignore wait
		doNothing().when(passengerService).waitForLastStoredEvent(Mockito.any(Passenger.class));

		// return input passenger
		doAnswer(invocation -> invocation.getArgument(0)).when(passengerService).find(Mockito.any(Passenger.class));

		// we expect new passenger with created event (passenger with new ID), created
		// credit card event and 6 events for credit card attributes
		Passenger passenger = passengerService.createPassenger(input);
		assertThat(passenger.getId(), notNullValue());
		assertThat(passenger.getUncommitedEvents().size(), is(8));
	}
}
