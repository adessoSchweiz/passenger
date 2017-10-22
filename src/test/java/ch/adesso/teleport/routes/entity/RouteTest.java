package ch.adesso.teleport.routes.entity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

import ch.adesso.teleport.JsonConverter;

public class RouteTest {

	@Test
	public void testJsonConverter() throws IOException {
		Route route = new Route();
		route.newRoute("123", new LatitudeLongitude(111.1, 222.2), new LatitudeLongitude(333.1, 222.2), 2,
				CarType.ECONOMIC, "comments", "time", "distance");

		String json = JsonConverter.toJson(route);

		InputStream is = new ByteArrayInputStream(json.getBytes());
		Route r = JsonConverter.fromInputStream(is, Route.class);

		is.close();
		
		json = "{\"passengerId\" : \"111\", "+
			   "\"from\": { " +
			   " \"latitude\": 111.2, "+
			   " \"longitude\": 2222.11 "+
			   " },\"to\": { "+
			   " \"latitude\": 212.3, "+
			   " \"longitude\": 2222.11 "+
			   " }, "+
	           " \"noOfPersons\": 2,\"passengerComments\": \"ecke bahnhof strasse\"} ";

		is = new ByteArrayInputStream(json.getBytes());
		r = JsonConverter.fromInputStream(is, Route.class);
		
		is.close();
	}

}
