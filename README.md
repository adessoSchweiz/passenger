# passenger app

The passenger app contains services for creating/updating a passenger and requesting or cancel a route.
In the business context of passenger we have tree aggregate roots: passenger, person and route. The person must be registered 
before the user can create a new passenger (this is a role the user play in the passenger context).

All changes are published into kafka, 

* passenger events into passenger-events-topic
* person events into person-events-topic and
* route events into route-events-topic

The person events are produced by another service (person registration). In passenger app we only feed them from kafka (person-events-topic) and aggregate into person aggreagate root and store locally (using kafka streams state store). This way, 
there is no need to call other REST services in order to get additional personal info.

A route aggregate (see Domain Driven Design DDD) is different in the context of passenger and driver. A passenger initializes a route, a driver usually can accept and finalizes the route.
 

## run Kafka Cluster

clone local-kafka-setup project and run ./startKafka.sh

## run passenger app

` ./build-run.sh`


The person creation is not needed if already done in registration service - here only for testing

1. create a person http://localhost:8091/passenger/resources/persons

		{
			"firstname": "firstname",
			"lastname" : "lastname",
			"mobil"    : "079-222-11-11"
		}


2. create passenger http://localhost:8091/passenger/resources/passengers 

		{
		 	"id": "personId from person create request",
		 	"credit_card": {
	        "card_number": "2B-2222",
	        "card_type": "VISA",
	        "name_on_card": "me",
	        "secret_number": 171,
	        "valid_to_month": 12,
	        "valid_to_year": 2020
	    }

3. create route http://localhost:8091/passenger/resources/routes

		{
			"passenger_id" : "passenger id created in step 2",
			"from": {
			    "latitude": 111.2,
			    "longitude": 2222.11
			  	},
			"to": {
			    "latitude": 212.3,
			    "longitude": 2222.11
			  	},		
			"no_of_persons": 2,
			"passenger_comment": "some important info"
		}
	
	
4. cancel route http://localhost:8091/passenger/resources/routes/{routeId}

	
Or easy install "restlet client plugin" in chrom and import create_route_workflow.json file, switch to scenarios and run. 
	