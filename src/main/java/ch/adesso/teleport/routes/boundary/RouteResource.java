package ch.adesso.teleport.routes.boundary;

import static ch.adesso.teleport.JsonConverter.fromInputStream;
import static ch.adesso.teleport.JsonConverter.toJson;
import static java.util.concurrent.CompletableFuture.supplyAsync;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.airhacks.porcupine.execution.boundary.Dedicated;

import ch.adesso.teleport.routes.entity.Route;

@Path("routes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class RouteResource {

	@Inject
	@Dedicated
	private ExecutorService routesResourcePool;

	@Inject
	private RouteService routesService;

	@POST
	public void createRoute(InputStream route, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(
				() -> Response.status(Response.Status.CREATED)
						.entity(toJson(routesService.createRoute(fromInputStream(route, Route.class)))).build(),
				routesResourcePool).thenApply(asyncResponse::resume);
	}

	@Path("/{routeId}")
	@POST
	public void cancelRoute(@PathParam("routeId") String routeId, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> {
			try {
				routesService.cancelRoute(routeId);
				return Response.ok().build();
			} catch (Throwable t) {
				while (t.getCause() != null) {
					t = t.getCause();
				}
				return Response.status(Response.Status.BAD_REQUEST).entity(toJson(t.getMessage())).build();
			}
		}, routesResourcePool).thenApply(asyncResponse::resume);
	}

	@Path("/{routeId}")
	@GET
	public void getRoute(@PathParam("routeId") String routeId, @Suspended final AsyncResponse asyncResponse) {
		supplyAsync(() -> Response.ok().entity(toJson(routesService.findRouteById(routeId))).build(),
				routesResourcePool).thenApply(asyncResponse::resume);
	}
}
