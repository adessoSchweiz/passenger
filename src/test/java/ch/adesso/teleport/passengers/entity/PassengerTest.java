package ch.adesso.teleport.passengers.entity;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class PassengerTest {

	@Test
	public void createPassenger() {
		Passenger passenger = new Passenger("111");
		assertThat(passenger.getId(), notNullValue());

		// expect created event
		assertThat(passenger.getUncommitedEvents().size(), is(1));

		// and version = 0
		assertThat(passenger.getVersion(), is(0l));
	}

	@Test
	public void updateFrom() {
		Passenger input = new Passenger("id");
		input.setCreditCard(new CreditCard("111-222", CreditCardTypeEnum.VISA, "card_owner", 11, 2020, 333));

		// we get passenger from DB with version 7
		Passenger savedPassenger = new Passenger();
		savedPassenger.setId(input.getId());
		savedPassenger.setVersion(7l);

		// and then update the data (will generate events)
		savedPassenger.updateFrom(input);

		// created credit card
		assertThat(savedPassenger.getUncommitedEvents().size(), is(1));

		// we start with saved version = 7 and add 1 events from credit card
		assertThat(savedPassenger.getVersion(), is(8l));

	}

}
