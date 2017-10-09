package ch.adesso.teleport;

import org.apache.avro.reflect.AvroDefault;
import org.apache.avro.reflect.AvroName;
import org.apache.avro.reflect.Nullable;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@ToString
public class CoreEvent {

	@AvroName("event_type")
	private String eventType;
	@AvroName("aggregate_id")
	private String aggregateId;
	private long sequence;
	private long timestamp;

	@Nullable
	@AvroDefault("null")
	private String value = null;

	public CoreEvent(String aggregateId, long sequence, String eventType, Object value) {
		this.timestamp = System.nanoTime();
		this.eventType = eventType;
		this.aggregateId = aggregateId;
		this.sequence = sequence;
		if (value != null) {
			this.value = value.toString();
		}
	}

	public Integer toInt() {
		return Integer.valueOf(value);
	}

	public <T extends Enum<T>> T toEnum(Class<T> clazz) {
		return Enum.valueOf(clazz, value);
	}
}