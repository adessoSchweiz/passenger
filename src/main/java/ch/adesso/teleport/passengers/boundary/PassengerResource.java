package ch.adesso.teleport.passengers.boundary;

import static ch.adesso.teleport.JsonConverter.fromInputStream;
import static ch.adesso.teleport.JsonConverter.toJson;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
	private ExecutorService passengerResourcePool;

	@Inject
	private PassengerService passengerService;

	@Path("/{passengerId}")
	@GET
	public Response getPassenger(@PathParam("passengerId") String passengerId) {
		return Response.ok().entity(toJson(passengerService.findPassengerById(passengerId))).build();
	}

	@POST
	public void createPassenger(InputStream passenger, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> Response.status(Response.Status.CREATED)
				.entity(exec(passenger, passengerService::createPassenger)).build(), passengerResourcePool)
						.thenApply(asyncResponse::resume);
	}

	@Path("/{passengerId}")
	@PUT
	public void updatePassenger(@PathParam("passengerId") String passengerId, InputStream passenger,
			@Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> Response.ok().entity(exec(passenger, passengerService::updatePassenger)).build(),
				passengerResourcePool).thenApply(asyncResponse::resume);
	}

	private String exec(InputStream is, Function<Passenger, Passenger> service) {
		return toJson(service.apply(fromInputStream(is, Passenger.class)));
	}
}
