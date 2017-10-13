package ch.adesso.teleport.kafka.store;

import static org.apache.kafka.streams.state.QueryableStoreTypes.keyValueStore;

import javax.persistence.EntityNotFoundException;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;

import ch.adesso.teleport.AggregateRoot;
import kafka.common.KafkaException;

public class KafkaEventStore {

	private KafkaStreams kafkaStreams;

	public KafkaEventStore(KafkaStreams kafkaStreams) {
		this.kafkaStreams = kafkaStreams;
	}

	public <T extends AggregateRoot> T findById(String storeName, String id) {
		T root = loadAggregateFromLocalStore(storeName, id);
		if (root == null) {
			throw new EntityNotFoundException("Could not find Entity for ID: " + id);
		}

		return (T) root;
	}

	public <T extends AggregateRoot> T findByIdAndVersion(String storeName, String aggregateId, long aggregateVersion) {
		AggregateRoot root = loadAggregateFromLocalStore(storeName, aggregateId);
		if (root == null || (root.getVersion() != aggregateVersion)) {
			throw new EntityNotFoundException(
					"Could not find Entity for ID: " + aggregateId + " and version: " + aggregateVersion);
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

	public <T extends AggregateRoot> T loadAggregateFromLocalStore(String storeName, String aggregateId) {
		ReadOnlyKeyValueStore<String, T> store = null;
		try {
			store = QueryableStoreUtils.waitUntilStoreIsQueryable(storeName, keyValueStore(), kafkaStreams);

		} catch (InterruptedException e) {
			throw new KafkaException("KeyValueStore can not read current data.", e);
		}

		return store.get(aggregateId);
	}

}
