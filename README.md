# passenger app

The passenger app contains services for creating/updating a passenger and requesting or cancel a route.
In the business context of passenger we have tree aggregate roots: passenger, person and route. The person must be registered (party-service)
before the user can create a new passenger (this is a role the user play in the passenger context).

All changes are published into kafka, 

* passenger events into passenger-events-topic
* person events into person-events-topic and
* route events into route-events-topic

The person events are produced by another service (person registration, party-service). In passenger app we only feed them from kafka (person-events-topic) and aggregate into person aggreagate root and store locally (using kafka streams state store). This way, 
there is no need to call other REST services in order to get additional personal info.

A route aggregate (see Domain Driven Design DDD) is different in the context of passenger and driver. A passenger initializes a route, a driver usually can accept and finalizes the route.
 

## run Kafka Cluster

clone local-kafka-setup project and run `./startKafka.sh `

## run MSSQL

clone mssql-centos project and run `./startContainer.sh `

and `create-database.sh  to create a "demo" database

## run passenger app

` ./build-run.sh `


## run query-service
query-service was implemented as a showcase for relational database view of the events produces by different components. 
This model can be used for ad hoc queries. 

clone query-service and run `./build-run.sh `



Registration of person
1. create a person http://localhost:8092/party-service/resources/persons

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


5. Start SQuirreL (or any other DB Viewer) and connect to the demo database:

connection string: `jdbc:sqlserver://localhost:1433 `
login : sa
password: adesso@OpenShift	

for login/passwd see in mssql-centos project ./startContainer.sh