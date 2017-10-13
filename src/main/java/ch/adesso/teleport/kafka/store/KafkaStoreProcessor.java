package ch.adesso.teleport.kafka.store;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;

public class KafkaStoreProcessor<T extends AggregateRoot> implements Processor<String, CoreEvent> {

	private String passengerStoreName;
	private ProcessorContext context;
	private KeyValueStore<String, T> kvPassengerStore;
	private Consumer<ProcessedEvent> eventConsumer;
	private Supplier<T> aggregateFactory;

	public KafkaStoreProcessor(String passengerStoreName, Supplier<T> aggregateFactory,
			Consumer<ProcessedEvent> eventConsumer) {
		this.passengerStoreName = passengerStoreName;
		this.eventConsumer = eventConsumer;
		this.aggregateFactory = aggregateFactory;
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
			aggregate = aggregateFactory.get();
		}

		aggregate.applyEvent(event);

		kvPassengerStore.put(key, aggregate);
		if (eventConsumer != null) {
			eventConsumer.accept(new ProcessedEvent(event));
		}
		// context.commit();
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
