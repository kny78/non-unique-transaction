# non-unique-transaction Project

This project is made to illustrate the "Connection" issue with unsynchronized transactions.


## Testing the project
The project can be ran against test contaiers or a local PostgreSQL database, dependening on the `application.properties`.

## Rest calls:

### List Persons:

```
curl -s -XGET  -H "Content-Type: application/json" http://localhost:8080/persons |jq ''
```

### Post Person (OK)

```
curl -s -XPOST  -H "Content-Type: application/json" http://localhost:8080/persons -d '{ "name": "Anton"}' | jq ''  
```


### Post Person FAILING

This Rest call fails because it starts transactions in a Kotlin co-routine, and then it is closed in the "main method".


With exception in the log:
```
curl -s -XPOST  -H "Content-Type: application/json" "http://localhost:8080/persons/fails?wait-for-job=true" -d '{ "name": "Anton"}' | jq ''
```


With just message that the commit is not rolled back
```
curl -s -XPOST  -H "Content-Type: application/json" "http://localhost:8080/persons/fails?wait-for-job=true" -d '{ "name": "Anton"}' | jq ''
```

