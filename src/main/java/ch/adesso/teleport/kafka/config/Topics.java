package ch.adesso.teleport.kafka.config;

public enum Topics {

	PASSENGER_EVENT_TOPIC("passenger-events-topic"), PASSENGER_AGGREGATE_STORE(
			"passenger-aggregate-store"), PERSON_EVENT_TOPIC("person-events-topic"), PERSON_AGGREGATE_STORE(
					"person-aggregate-store"), ROUTE_EVENT_TOPIC(
							"route-events-topic"), ROUTE_AGGREGATE_STORE("route-aggregate-store");

	private String topic;

	Topics(String topic) {
		this.topic = topic;
	}

	public String toString() {
		return topic;
	}
}
