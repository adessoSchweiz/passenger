package ch.adesso.teleport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.json.bind.annotation.JsonbTransient;

import lombok.Data;

@Data
public abstract class AggregateRoot {

	private String id;

	private long version = 0;

	@JsonbTransient
	private Collection<CoreEvent> uncommitedEvents;

	@JsonbTransient
	private Map<String, Consumer<CoreEvent>> eventHandlers;

	public AggregateRoot() {
		init();
	}

	public void applyEvent(final CoreEvent event) {
		// deserialization
		if (eventHandlers.size() == 0) {
			initHandlers();
		}
		setVersion(event.getSequence());
		eventHandlers.get(event.getEventType()).accept(event);
	}

	protected void applyChange(Object eventType) {
		CoreEvent event = new CoreEvent(getId(), getNextVersion(), eventType.toString(), null);
		applyEvent(event);
		synchronized (uncommitedEvents) {
			uncommitedEvents.add(event);
		}
	}

	protected void applyChange(Object eventType, Object newValue, Object oldValue) {
		if (wasChanged(newValue, oldValue)) {
			CoreEvent event = new CoreEvent(getId(), getNextVersion(), eventType.toString(), newValue);
			applyEvent(event);
			synchronized (uncommitedEvents) {
				uncommitedEvents.add(event);
			}
		}
	}

	protected void applyChange(CoreEvent event) {
		applyEvent(event);
		synchronized (uncommitedEvents) {
			uncommitedEvents.add(event);
		}
	}

	private void init() {
		uncommitedEvents = new ArrayList<CoreEvent>();
		eventHandlers = new HashMap<String, Consumer<CoreEvent>>();
		initHandlers();
	}

	protected abstract void initHandlers();

	protected void addHandler(Object eventType, Consumer<CoreEvent> consumer) {
		eventHandlers.put(eventType.toString(), consumer);
	}

	public Collection<CoreEvent> getUncommitedEvents() {
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
