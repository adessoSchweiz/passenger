package ch.adesso.teleport.kafka;

import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import kafka.common.KafkaException;

public class KafkaStore {

	private KafkaProducer<String, CoreEvent> producer;
	private KafkaStreams kafkaStreams;
	private Consumer<CoreEvent> eventConsumer;

	public KafkaStore(KafkaStreams kafkaStreams, KafkaProducer<String, CoreEvent> producer,
			Consumer<CoreEvent> eventConsumer) {
		this.kafkaStreams = kafkaStreams;
		this.producer = producer;
		this.eventConsumer = eventConsumer;
	}

	public <T extends AggregateRoot> void save(String topicName, T aggregateRoot) {
		Collection<CoreEvent> events = aggregateRoot.getUncommitedEvents();
		publishEvents(topicName, events);
		if (eventConsumer != null) {
			events.forEach(eventConsumer::accept);
		}
		aggregateRoot.clearEvents();
	}

	public <T extends AggregateRoot> T findById(String storeName, String id) {
		T root = loadAggregateFromLocalStore(storeName, id);
		if (root == null) {
			throw new EntityNotFoundException("Could not find Entity for ID: " + id);
		}

		return (T) root;
	}

	public <T extends AggregateRoot> T findByIdAndVersionWaitForResult(String storeName, T aggregate) {
		int loop = 0;
		String id = aggregate.getId();
		long version = aggregate.getVersion();
		while (true) {
			AggregateRoot root = loadAggregateFromLocalStore(storeName, id);
			if (root == null || (root.getVersion() != version)) {
				loop++;
				if (loop > 20) {
					break;
				}
				try {
					Thread.sleep(50L);
					continue;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					break;
				}

			}

			return (T) root;
		}

		throw new EntityNotFoundException("Could not find Entity for ID: " + id + " and version: " + version);
	}

	private void publishEvents(String topicName, Collection<CoreEvent> events) {
		List<CompletableFuture<RecordMetadata>> futures = new ArrayList<>();
		producer.beginTransaction();
		try {
			events.stream().forEach(e -> futures.add(publishEvent(topicName, e)));
			waitForAll(futures);
			producer.commitTransaction();

		} catch (ProducerFencedException fEx) {
			producer.abortTransaction();
		} catch (KafkaException kEx) {
			producer.close();
		}
	}

	private CompletableFuture<RecordMetadata> publishEvent(String topicName, CoreEvent event) {
		ProducerRecord<String, CoreEvent> record = new ProducerRecord<>(topicName, event.getAggregateId(), event);

		CompletableFuture<RecordMetadata> f = new CompletableFuture<>();
		producer.send(record, (metadata, exception) -> {
			if (exception != null) {
				f.completeExceptionally(exception);
			} else {
				f.complete(metadata);
			}
		});

		return f;
	}

	private <T extends AggregateRoot> T loadAggregateFromLocalStore(String storeName, String aggregateId) {
		ReadOnlyKeyValueStore<String, T> store = null;
		try {
			store = QueryableStoreUtils.waitUntilStoreIsQueryable(storeName, keyValueStore(), kafkaStreams);

		} catch (InterruptedException e) {
			throw new KafkaException("KeyValueStore can not read current data.", e);
		}

		return store.get(aggregateId);
	}

	private static <T> CompletableFuture<List<T>> waitForAll(List<CompletableFuture<T>> futures) {
		CompletableFuture<Void> allDoneFuture = CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return allDoneFuture
				.thenApply(v -> futures.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}
}
