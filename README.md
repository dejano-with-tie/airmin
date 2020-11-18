# AirMin

Find cheapest flights between two cities. Used [datasets](https://openflights.org/data.html) 

# Getting Started

### Run application

```
mvn spring-boot:run -Dspring-boot.run.arguments=--initCities=true,--initUsers=true
```

- `initCities=true` will save all cities from dataset to database
- `initUsers=true` will create two users with credentials admin:admin and user:user

----
### Endpoints

#### Authenticate
```
curl -i --location --request POST 'http://localhost:8080/auth' \
--header 'Content-Type: application/json' \
--data-raw '{

    "username": "admin",
    "password": "admin"
}'
```

#### Signup
```
curl --location --request POST 'http://localhost:8080/signup' \
--header 'Content-Type: application/json' \
--data-raw '{
    "firstName": "John",
    "lastName": "Doe",
    "username": "john",
    "password": "john"
}'
```

#### Import airports

```
curl --location --request POST 'http://localhost:8080/airports' \
--form 'file=@absolute-path'
```

#### Import routes

```
curl --location --request POST 'http://localhost:8080/routes' \
--form 'file=@absolute-path'
```

#### Search cities

```
curl --location --request GET 'http://localhost:8080/cities?page=0&comments-length=2&limit=10&name=belgrade' \
--header 'Authorization: Bearer token'
```

#### Create City
```
curl --location --request POST 'http://localhost:8080/cities' \
--header 'Authorization: Bearer token' \
--header 'Content-Type: application/json' \
--data-raw '{
    "name": "Belgrade",
    "country": "Serbia",
    "description": "Capital"
}'
```

#### Comment
```
curl --location --request POST 'http://localhost:8080/cities/${city-id}/comments' \
--header 'Authorization: Bearer token' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "hello"
}'
```

#### Update comment
```
curl --location --request PUT 'http://localhost:8080/cities/${city-id}/comments/${comment-id}' \
--header 'Authorization: Bearer token' \
--header 'Content-Type: application/json' \
--data-raw '{
    "content": "hello1"
}'
```

#### Delete comment
```
curl --location --request DELETE 'http://localhost:8080/cities/${city-id}/comments/${comment-id}' \
--header 'Authorization: Bearer token' \
--header 'Content-Type: application/json'
```

#### Find cheapest route
```
curl --location --request GET 'http://localhost:8080/routes/cheapest?source=${city-id}&destination=${city-id}' \
--header 'Authorization: Bearer token'
```