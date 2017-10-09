package ch.adesso.teleport.kafka;

import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;

public class StoreProcessor<T extends AggregateRoot> implements Processor<String, CoreEvent> {

	private String passengerStoreName;
	private ProcessorContext context;
	private KeyValueStore<String, T> kvPassengerStore;
	private Consumer<CoreEvent> eventConsumer;
	private Function<String, T> factory;

	public StoreProcessor(String passengerStoreName, Function<String, T> factory,
			Consumer<CoreEvent> eventConsumer) {
		this.passengerStoreName = passengerStoreName;
		this.eventConsumer = eventConsumer;
		this.factory = factory;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void init(ProcessorContext context) {
		this.context = context;
		kvPassengerStore = (KeyValueStore<String, T>) context.getStateStore(passengerStoreName);
	}

	@Override
	public void process(String key, CoreEvent event) {
		T aggregate = kvPassengerStore.get(key);
		if (aggregate == null) {
			aggregate = factory.apply(key);
		}

		aggregate.applyEvent(event);

		kvPassengerStore.put(key, aggregate);
		if (eventConsumer != null) {
			eventConsumer.accept(event);
		}
		context.commit();
	}

	@Override
	public void punctuate(long timestamp) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		kvPassengerStore.close();
	}

}
