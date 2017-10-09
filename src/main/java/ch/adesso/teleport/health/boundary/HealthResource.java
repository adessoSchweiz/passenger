package ch.adesso.teleport.health.boundary;

import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.util.concurrent.ExecutorService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.streams.KafkaStreams;

import com.airhacks.porcupine.execution.boundary.Dedicated;

import ch.adesso.teleport.JsonConverter;
import ch.adesso.teleport.kafka.KafkaConfiguration;
import ch.adesso.teleport.kafka.KafkaStore;
import ch.adesso.teleport.kafka.KafkaStoreBuilder;
import ch.adesso.teleport.kafka.StoreProcessor;
import ch.adesso.teleport.passengers.controller.Topics;
import ch.adesso.teleport.passengers.entity.Passenger;

@Path("health")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class HealthResource {

	@Inject
	@Dedicated
	private ExecutorService healthPool;

	private KafkaStore kafkaStore;

	private KafkaStreams kafkaStreams;

	@PostConstruct
	public void init() {

		kafkaStreams = new KafkaStoreBuilder<Passenger>(Passenger.class, KafkaConfiguration.streamsProperties())
				.withSourceTopicName(Topics.PASSENGER_EVENT_TOPIC.toString())
				.withStateStoreName(Topics.PASSENGER_AGGREGATE_STORE.toString())
				.withProcessorSupplier(() -> new StoreProcessor<Passenger>(Topics.PASSENGER_AGGREGATE_STORE.toString(),
						Passenger::new, null))
				.build();

		kafkaStore = new KafkaStore(kafkaStreams, null, null);
	}

	@PreDestroy
	public void close() {
		kafkaStreams.close();
	}

	@GET
	public void testPassenger(@QueryParam("personId") String personId, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(
				() -> JsonConverter.toJson(kafkaStore.findById(Topics.PASSENGER_AGGREGATE_STORE.toString(), personId)),
				healthPool).thenApply(asyncResponse::resume);
	}
}
