package ch.adesso.teleport.kafka.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.errors.ProducerFencedException;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.kafka.config.KafkaConfiguration;
import kafka.common.KafkaException;

@Startup
@Singleton
public class KafkaEventPublisher {

	private KafkaProducer<String, CoreEvent> producer;

	@Inject
	Event<PublishedEvent> producerEvents;

	@PostConstruct
	public void init() {
		producer = new KafkaProducer<String, CoreEvent>(KafkaConfiguration.producerDefaultProperties());
		producer.initTransactions();
	}

	@PreDestroy
	public void close() {
		producer.close();
	}

	public <T extends AggregateRoot> void save(String topicName, T aggregateRoot) {
		Collection<CoreEvent> events = aggregateRoot.getUncommitedEvents();
		publishEvents(topicName, events);
		events.forEach(e -> producerEvents.fire(new PublishedEvent(e)));
		aggregateRoot.clearEvents();
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

	private static <T> CompletableFuture<List<T>> waitForAll(List<CompletableFuture<T>> futures) {
		CompletableFuture<Void> allDoneFuture = CompletableFuture
				.allOf(futures.toArray(new CompletableFuture[futures.size()]));
		return allDoneFuture
				.thenApply(v -> futures.stream().map(future -> future.join()).collect(Collectors.<T>toList()));
	}
}
