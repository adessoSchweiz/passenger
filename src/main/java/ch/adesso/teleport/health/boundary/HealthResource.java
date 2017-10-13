package ch.adesso.teleport.health.boundary;

import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

@Path("health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class HealthResource {

	// @Inject
	// @Dedicated
	// private ExecutorService healthPool;

	// @PassengerQualifier
	// @Inject
	// private KafkaEventStore kafkaLocalStore;

	@GET
	public void testPassenger(@QueryParam("personId") String personId, @Suspended final AsyncResponse asyncResponse) {
		// supplyAsync(
		// () -> JsonConverter
		// .toJson(kafkaLocalStore.findById(Topics.PASSENGER_AGGREGATE_STORE.toString(),
		// personId)),
		// healthPool).thenApply(asyncResponse::resume);
	}
}
