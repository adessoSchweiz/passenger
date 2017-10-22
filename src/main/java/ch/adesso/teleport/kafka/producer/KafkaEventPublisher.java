package ch.adesso.teleport.kafka.producer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import ch.adesso.teleport.AggregateRoot;
import ch.adesso.teleport.CoreEvent;
import ch.adesso.teleport.EventEnvelope;

public class KafkaEventPublisher {

	private KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer;

	private Consumer<PublishedEvent> producerEvents;

	public KafkaEventPublisher(KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer) {
		this.producer = producer;
	}

	public KafkaEventPublisher(KafkaProducer<String, EventEnvelope<? extends CoreEvent>> producer,
			Consumer<PublishedEvent> producerEvents) {
		this(producer);
		this.producerEvents = producerEvents;
	}

	public <T extends AggregateRoot> void save(String topicName, T aggregateRoot) {
		Collection<EventEnvelope<? extends CoreEvent>> events = aggregateRoot.getUncommitedEvents();
		publishEvents(topicName, events);
		if (producerEvents != null) {
			events.forEach(e -> producerEvents.accept(new PublishedEvent(e.getEvent())));
		}
		aggregateRoot.clearEvents();
	}

	private void publishEvents(String topicName, Collection<EventEnvelope<? extends CoreEvent>> events) {
		List<CompletableFuture<RecordMetadata>> futures = new ArrayList<>();
		events.stream().forEach(e -> futures.add(publishEvent(topicName, e)));
		waitForAll(futures);
		producer.flush();
	}

	private CompletableFuture<RecordMetadata> publishEvent(String topicName, EventEnvelope<? extends CoreEvent> event) {
		String aggregateId = event.getEvent().getAggregateId();
		ProducerRecord<String, EventEnvelope<? extends CoreEvent>> record = new ProducerRecord<>(topicName, aggregateId,
				event);

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
