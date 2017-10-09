package ch.adesso.teleport.passengers.controller;

public enum Topics {

    PASSENGER_EVENT_TOPIC("passenger-events-topic"),
    PASSENGER_AGGREGATE_STORE("passenger-aggregate-store"),
    PERSON_EVENT_TOPIC("person-events-topic"),
    PERSON_AGGREGATE_STORE("person-aggregate-store");


    private String topic;

    Topics(String topic) {
        this.topic = topic;
    }

    public String toString() {
        return topic;
    }
}
