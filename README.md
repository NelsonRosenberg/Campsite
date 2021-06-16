# Volcano Campsite booking #

An underwater volcano formed a new small island in the Pacific Ocean last month. All the conditions on the island seems perfect and it was
decided to open it up for the general public to experience the pristine uncharted territory.

The island is big enough to host a single campsite so everybody is very excited to visit. In order to regulate the number of people on the island, it
was decided to come up with an online web application to manage the reservations. You are responsible for design and development of a REST
API service that will manage the campsite reservations.

To streamline the reservations a few constraints need to be in place -

- The campsite will be free for all.
- The campsite can be reserved for max 3 days.
- The campsite can be reserved minimum 1 day(s) ahead of arrival and up to 1 month in advance.
- Reservations can be cancelled anytime.
- For sake of simplicity assume the check-in & check-out time is 12:00 AM

## System Requirements ##
- The users will need to find out when the campsite is available. So the system should expose an API to provide information of the
availability of the campsite for a given date range with the default being 1 month.
- Provide an end point for reserving the campsite. The user will provide his/her email & full name at the time of reserving the campsite
along with intended arrival date and departure date. Return a unique booking identifier back to the caller if the reservation is successful.
- The unique booking identifier can be used to modify or cancel the reservation later on. Provide appropriate end point(s) to allow
modification/cancellation of an existing reservation.
- Due to the popularity of the island, there is a high likelihood of multiple users attempting to reserve the campsite for the same/overlapping
date(s). Demonstrate with appropriate test cases that the system can gracefully handle concurrent requests to reserve the campsite.
Provide appropriate error messages to the caller to indicate the error cases.
- Provide appropriate error messages to the caller to indicate the error cases.
- In general, the system should be able to handle large volume of requests for getting the campsite availability.
- There are no restrictions on how reservations are stored as as long as system constraints are not violated.


## How To Build/Run ##

The app uses Gradle as dependency management, which also facilitates the build, test, run and even deployment of the system. 
There is no need to install gradle, it is already included. The only requirement to run this app is JDK 11.

To build the app, while also running all the unit tests, use `./gradlew clean build`. 
To run, use `./gradlew bootRun` after building. 

Spring runs in an embedded Tomcat server. 
The system will be available on `locahost:8080`.

If you wish to run in a docker container, you can create an image using `./gradlew docker`. 
The Dockerfile provided is just a basic example which can be improved for production environments.
Be mindful to update the timezone in the Dockerfile to your current one.

## Documentation And Metrics ##

The system is annotated to generate swagger docs automatically. The resulting json is saved to the folder `/docs/swagger.json`, but you can also access a webpage version on `localhost:8080/api/swagger`.

Likewise, the system includes the following metrics endpoints: `localhost:8080/health`, `localhost:8080/info` and `localhost:8080/metrics`. 

The last one gives a list of acessible metrics. Each can be accessed by calling an endpoit with the same name, like so: `localhost:8080/metrics/http.server.requests` (which will return general performance metrics).

The docs folder also includes a postman collection that has a simple example for each of the available endpoints, to make testing easier.

## Database And Cache ##
The system was coded using an in-memory DB (H2) to make it more portable for the examiners, since they won't need to initialize an outside DB.
It is possible to access a console for the H2 DB using `http://localhost:8080/h2-console` with the following credentials:

```
jdbc.url = jdbc:h2:mem:campsite
username = sa
password = password
```

The cache also uses an embedded Redis that is started along with the app. 
If this is to be a real app to be run on a server, we can easily switch the DB and Cache to any other external one just using the `application.properties` file.

WARNING: Unfortunately, the embedded Redis is sometimes not closed after the app is stopped, which may cause an error `Could not start redis server, port is in use or server already started.` the next time the app is run, as the redis server is already up. 
This is a problem specifically with the embedded Redis server library and would not affect the app if it was a real external Redis server. 
It also does not affect the functionality of the app.

## Design And Assumptions ##

Considering the requirements made the point that the booking starts and ends at midnight, this means that there will be no overlap between the dates.
This requirement allows us to save to the DB a collection of unique dates for each booking and make the `booking_dates` table have only unique values. 
This way we sidestep any possible concurrency problems when saving bookings, using the DB constraints to garanteee that the first one will be successfull and the others will fail (and we can then handle the exceptions gracefully).

Since the system should handle a large quantity of requests, we added a cache to prevent repeated travels to the DB. 
This cache will contain all the future bookings, which will not grow by much since the system won't allow bookings further than a month in the advance.
Every 30 minutes, a quartz scheduled task will run to reset the cache using data from the DB, which will clear the past bookings from the cache as well as resolve any possible caching inconsistencies.
Quartz was used in this case to make sure the cache reset is only run once per cluster for every cron trigger, considering that this system will probably run in a multi-server environment. 

Overall, the system should be able to handle multiple requests at once, but if the performance is insuficient it can be easily packaged into a docker and run on a cloud server like AWS EC2 or Fargate.



