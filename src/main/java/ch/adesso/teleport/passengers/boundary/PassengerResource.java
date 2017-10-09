package ch.adesso.teleport.passengers.boundary;

import static ch.adesso.teleport.JsonConverter.fromInputStream;
import static ch.adesso.teleport.JsonConverter.toJson;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.airhacks.porcupine.execution.boundary.Dedicated;

import ch.adesso.teleport.passengers.entity.Passenger;

@Path("passengers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class PassengerResource {

	@Inject
	@Dedicated
	private ExecutorService passengerPool;

	@Inject
	private PassengerService passengerService;

	@POST
	public void createPassenger(InputStream passenger, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> Response.status(Response.Status.CREATED)
				.entity(toJson(passengerService.createPassenger(fromInputStream(passenger, Passenger.class)))).build(),
				passengerPool).thenApply(asyncResponse::resume);
	}

	@PUT
	public void updatePassenger(Passenger passenger, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> Response.ok().entity(passengerService.updatePassenger(passenger)).build(), passengerPool)
				.thenApply(asyncResponse::resume);
	}

}
