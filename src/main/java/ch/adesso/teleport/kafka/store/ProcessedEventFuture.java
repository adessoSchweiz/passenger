package ch.adesso.teleport.kafka.store;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * We use rx-java to dynamically subscribe for processed events (see
 * {@link KafkaStoreProcessor}). When the condition (usually aggregateId &&
 * aggregateVersion) match the event data, then the future completes.
 * 
 * As alternative, it's possible to poll the local state store see
 * {@link KafkaEventStore#findByIdAndVersionWaitForResult}
 * 
 */
public class ProcessedEventFuture {

	private PublishSubject<ProcessedEvent> rxPublishSubject;

	public ProcessedEventFuture(PublishSubject<ProcessedEvent> rxPublishSubject) {
		this.rxPublishSubject = rxPublishSubject;
	}

	public CompletableFuture<ProcessedEvent> getCompletableFuture(Function<ProcessedEvent, Boolean> condition) {
		CompletableFuture<ProcessedEvent> f = new CompletableFuture<>();

		Disposable disposable = null;

		try {
			disposable = rxPublishSubject.subscribe(event -> {
				if (condition.apply(event)) {
					f.complete(event);
				}
			});

			return f;
		} finally {
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
	}

}
