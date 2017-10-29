package ch.adesso.teleport.passengers.boundary;

import static ch.adesso.teleport.JsonConverter.toJson;
import static com.airhacks.rulz.jaxrsclient.JAXRSClientProvider.buildWithURI;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;

import com.airhacks.rulz.jaxrsclient.JAXRSClientProvider;

import ch.adesso.teleport.passengers.entity.CreditCard;
import ch.adesso.teleport.passengers.entity.CreditCardType;
import ch.adesso.teleport.passengers.entity.Passenger;

public class PassengerResourceIT {

	public static final String BASE_PATH = System.getenv("BASE_PATH");

	@Rule
	public JAXRSClientProvider healthProvider = buildWithURI(BASE_PATH + "/health");

	@Rule
	public JAXRSClientProvider personProvider = buildWithURI(BASE_PATH + "/passengers");

	private static final String CARD_NUMBER = "0098 8765 5432 9876";
	private static final String CARD_TYPE = "VISA";
	private static final String NAME_ON_CARD = "Robert Brem";
	private static final int VALID_TO_MONTH = 2;
	private static final int VALID_TO_YEAR = 2020;
	private static final int SECRET_NUMBER = 123;

	private static String ID;

	@Test
	public void a01_shouldCreatePassenger() {
		Passenger passengerToCreate = new Passenger();
		CreditCard cc = new CreditCard(CARD_NUMBER, CreditCardType.valueOf(CARD_TYPE), NAME_ON_CARD, VALID_TO_MONTH,
				VALID_TO_YEAR, SECRET_NUMBER);
		passengerToCreate.setCreditCard(cc);

		Response postResponse = this.personProvider.target().request().post(Entity.json(toJson(passengerToCreate)));

		assertThat(postResponse.getStatus(), is(201));

		ID = postResponse.readEntity(Passenger.class).getId();

		assertThat(ID, notNullValue());
	}

	@Test
	public void a02_shouldReturnPassengerForHealthCheck() throws InterruptedException {
		Passenger passenger = this.healthProvider.target().queryParam("personId", ID)
				.request(MediaType.APPLICATION_JSON).get(Passenger.class);

		CreditCard cc = passenger.getCreditCard();

		assertThat(cc.getCardNumber(), is(CARD_NUMBER));
		assertThat(cc.getCardType().toString(), is(CARD_TYPE));
		assertThat(cc.getNameOnCard(), is(NAME_ON_CARD));
		assertThat(cc.getValidToMonth(), is(VALID_TO_MONTH));
		assertThat(cc.getValidToYear(), is(VALID_TO_YEAR));
		assertThat(cc.getSecretNumber(), is(SECRET_NUMBER));
	}

}
