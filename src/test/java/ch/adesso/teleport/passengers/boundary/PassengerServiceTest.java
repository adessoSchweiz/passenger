package ch.adesso.teleport.passengers.boundary;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import ch.adesso.teleport.passengers.entity.CreditCard;
import ch.adesso.teleport.passengers.entity.CreditCardTypeEnum;
import ch.adesso.teleport.passengers.entity.Passenger;
import ch.adesso.teleport.persons.entity.Person;

@RunWith(MockitoJUnitRunner.class)
public class PassengerServiceTest {

	/**
	 * given a person (with an personId) we create passenger
	 * 
	 */
	@Test
	public void testCreatePassenger() {

		Person person = mock(Person.class);
		when(person.getId()).thenReturn(UUID.randomUUID().toString());

		Passenger input = new Passenger();
		input.updateCreditCard(new CreditCard("1234", CreditCardTypeEnum.VISA, "name", 1, 2020, 123));
		input.setVersion(0);
		input.clearEvents();

		PassengerService passengerService = spy(PassengerService.class);

		// ignore wait
		doNothing().when(passengerService).waitForLastStoredEvent(Mockito.any(Passenger.class));

		// return input passenger
		doAnswer(invocation -> invocation.getArgument(0)).when(passengerService).find(Mockito.any(Passenger.class));

		// return input person
		doAnswer(invocation -> person).when(passengerService).findPersonById(Mockito.any());

		// we expect new passenger with created event (passenger with new ID) and
		// created credit card event
		Passenger passenger = passengerService.createPassenger(input);
		assertThat(passenger.getId(), is(person.getId()));
		assertThat(passenger.getUncommitedEvents().size(), is(2));
	}
}
