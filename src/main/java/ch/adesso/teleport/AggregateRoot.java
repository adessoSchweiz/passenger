package ch.adesso.teleport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import javax.json.bind.annotation.JsonbTransient;

import avro.shaded.com.google.common.collect.Lists;
import lombok.Data;

@Data
public abstract class AggregateRoot {

	private String id;

	private long version = 0;

	@JsonbTransient
	private Collection<EventEnvelope<? extends CoreEvent>> uncommitedEvents = Lists.newArrayList();

	public void applyEvent(final EventEnvelope<? extends CoreEvent> eventEnv) {
		CoreEvent event = eventEnv.getEvent();
		setVersion(event.getSequence());
		try {
			Method m = getClass().getDeclaredMethod("on", event.getClass());
			m.setAccessible(true);
			m.invoke(this, event);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			throw new RuntimeException(e);
		}
	}

	protected void applyChange(CoreEvent event) {
		EventEnvelope<? extends CoreEvent> env = wrapEventIntoEnvelope(event);
		applyEvent(env);
		synchronized (uncommitedEvents) {
			uncommitedEvents.add(env);
		}
	}

	protected abstract EventEnvelope<? extends CoreEvent> wrapEventIntoEnvelope(CoreEvent event);

	public Collection<EventEnvelope<? extends CoreEvent>> getUncommitedEvents() {
		return Collections.unmodifiableCollection(uncommitedEvents);
	}

	public void clearEvents() {
		uncommitedEvents.clear();
	}

	protected boolean wasChanged(Object o1, Object o2) {
		return o1 == null ? o2 != null : !o1.equals(o2);
	}

	protected long getNextVersion() {
		return ++version;
	}
}
