
## Solution Description

The project uses the following technologies:

* Java 15
* Spring Boot
* MongoDB
* Maven

## The architecture

* The service uses Controller - Service - Repository pattern:
    * Controller: contains application logic and passing user input data to service
    * Service: The middleware between controller and repository. Gather data from controller, 
      performs validation and business logic, and calling repositories for data manipulation.
    * Repository: layer for interaction with models and performing DB operations.
  
* The main components of the service are: 
    * KeyGenerationScheduler: It generates random unique 8 letters strings, and store them in DB.
      Whenever we want to generate a tinyUrl, we will use one of the pre-generated key from the DB.
    * AliasService: It is responsible for fetching one of the keys, and making sure that there is no 
      other user trying to use the same key at the same time. Whenever the key is used, it marked as used in the DB.
    * TinyUrlService: It is responsible for manipulating the URLS. Provides a curd operations for the URL. 
      
Since we anticipate storing billions of rows and relations between objects are not needed, so, NoSql is a better choice
since it is easier to scale. I used MongoDB in our case, however, I think Cassandra would be a better fit but didn't 
want to complicate things too much for the task.

## More about the KeyGenerationSchedule

Currently, the KeyGenerationScheduler runs every 100ms and generate a key, make sure it is unique and store it in the DB.
The keys are marked as not used, and if there are 500 not used keys, the scheduler will not generate any more keys,
until the number is below 500 (of course this number can be adjusted depends on the load).

It uses a shedLock to make sure that if there are more than one instance running, only one schedule task will run.

It generates an 8 letters key from characters ([A-Z, a-z, 0-9]), this gives us the possibility to generate 
62 ^ 8 = 281 trillion unique keys, which should be enough for our service.

AliasService also takes care of the concurrency issues, and makes sure that there are no two users trying to use the 
same key at the same time. It uses Optimistic Locking, each key has a version that is updated with each update, 
if the current thread has an outdated object, it will find another key.

## Caching
I would use a distributed caching to store the generated keys for the fast access. Also, I would 
cache the mapping between key and the original url.

## DB sharding 
Since we anticipate storing billions of records, one DB server won't be enough.
We will need to shard the data based on the hash of the generated Key.

## Random Notes
 * I created a User entity but I didn't use it due to time constraint.
 * The generated urls expires after 7 days (the keys also could return to not used state).
 * Every time a redirection occurs, the TinyUrlService updates the redirection counter async, so it won't impact 
the redirection performance, and also it handles the expected concurrency issues.
   
## The endpoints

* Create a url
  * Request:
    
    ``` curl -X POST -H 'Content-Type: application/json' -i http://localhost:8080/v1/urls --data 'https://www.google.com/' ```
  * Response:
    
    ``` http://localhost:8080/HOYp3PMG ```
    
* Find all
  * Request:
  
    ``` curl -X GET -i 'http://localhost:8080/v1/urls?page=0&size=2' ```
  * Response:
  
    ``` 
      {
          "page": 0,
          "size": 2,
          "totalElements": 1,
          "totalPages": 1,
          "last": true,
          "content": [{
            "id": "6033058802ec5e1ac0538898",
            "version": 1,
            "alias": "HOYp3PMG",
            "originalUrl": "https://www.google.com/",
            "userId": null,
            "redirectionCount": 0,
            "createdAt": "2021-02-22T01:14:48.838",
            "expirationDate": "2021-03-01T01:14:48.819"
          }]
       }
    
* Find a URL by alias
  * Request:
      
    ``` curl -X GET -i http://localhost:8080/v1/urls/HOYp3PMG ```
  * Response:
    ``` 
      {
        "id": "6033058802ec5e1ac0538898",
        "version": 1,
        "alias": "HOYp3PMG",
        "originalUrl": "https://www.google.com/",
        "userId": null,
        "redirectionCount": 0,
        "createdAt": "2021-02-22T01:14:48.838",
        "expirationDate": "2021-03-01T01:14:48.819"
      }
    ```
  
## Run Instructions
  To the application 

    Navigate to {app_folder}
    docker-compose up 
