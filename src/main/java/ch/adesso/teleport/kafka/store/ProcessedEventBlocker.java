package ch.adesso.teleport.kafka.store;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;

/**
 * We use rx-java to dynamically subscribe for processed events (see
 * {@link KafkaStoreProcessor}). When the condition (usually aggregateId &&
 * aggregateVersion must match the event data) is true, then the future
 * completes and waitFor returns.
 * 
 * As alternative, it's possible to poll the local state store see
 * {@link KafkaEventStore#findByIdAndVersionWaitForResult}
 * 
 */
public abstract class ProcessedEventBlocker {

	private PublishSubject<ProcessedEvent> rxPublishSubject;

	public ProcessedEventBlocker(PublishSubject<ProcessedEvent> rxPublishSubject) {
		this.rxPublishSubject = rxPublishSubject;
	}

	/**
	 * condition to complete the future
	 */
	public abstract boolean whenCondition(ProcessedEvent event);

	/**
	 * here we publish data to kafka, usually save events
	 */
	public abstract void execute();

	/**
	 * wait for last processed event, so we can start to query local store for the
	 * actual aggregated data
	 * 
	 * @throws TimeoutException
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	public void waitFor(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		CompletableFuture<ProcessedEvent> wait = new CompletableFuture<>();

		Disposable disposable = null;

		try {
			disposable = rxPublishSubject.subscribe(event -> {
				if (whenCondition(event)) {
					wait.complete(event);
				}
			});

			execute();

			wait.get(timeout, unit);
		} finally {
			if (disposable != null && !disposable.isDisposed()) {
				disposable.dispose();
			}
		}
	}

}
