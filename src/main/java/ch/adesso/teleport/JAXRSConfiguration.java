package ch.adesso.teleport;

import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * Configures a JAX-RS endpoint. Delete this class, if you are not exposing
 * JAX-RS resources in your application.
 *
 * @author airhacks.com
 */
@ApplicationPath("resources")
public class JAXRSConfiguration extends Application {

	@Override
	public Set<Object> getSingletons() {
		Set<Object> objects = super.getSingletons();
		objects.stream().forEach(o -> System.out.println("XXXXXXXX:> " + o));
		return objects;
	}
}
