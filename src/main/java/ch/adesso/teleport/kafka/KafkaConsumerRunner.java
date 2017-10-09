package ch.adesso.teleport.kafka;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;

import ch.adesso.teleport.CoreEvent;

/**
 * 
 * example taken from
 * https://kafka.apache.org/0110/javadoc/index.html?org/apache/kafka/clients/consumer/KafkaConsumer.html
 * 
 */
public class KafkaConsumerRunner implements Runnable {

	private final AtomicBoolean closed = new AtomicBoolean(false);
	private final KafkaConsumer<String, CoreEvent> consumer;

	private List<String> topics;
	private Consumer<CoreEvent> eventConsumer;

	public KafkaConsumerRunner(Properties consumerProperties, Consumer<CoreEvent> eventConsumer, String... topic) {
		this.topics = Arrays.asList(topic);
		this.eventConsumer = eventConsumer;

		consumer = new KafkaConsumer<>(consumerProperties);
		consumer.subscribe(topics);
	}

	public void run() {
		try {
			while (!closed.get()) {
				ConsumerRecords<String, CoreEvent> records = consumer.poll(1000);
				records.forEach(record -> eventConsumer.accept(record.value()));
				consumer.commitSync();
			}
		} catch (WakeupException e) {
			// Ignore exception if closing
			if (!closed.get())
				throw e;
		} finally {
			consumer.close();
		}
	}

	// Shutdown hook which can be called from a separate thread
	public void shutdown() {
		closed.set(true);
		consumer.wakeup();
	}
}
