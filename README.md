# User Management API

A Spring Boot REST API for user generation, batch import, JWT authentication, and role-based access control.

---

## Features

- Generate realistic user data in JSON format
- Download generated users as a JSON file
- Import users from a JSON file
- Prevent duplicate usernames and email addresses
- Encrypt passwords using BCrypt
- Authenticate users using username or email
- Generate JWT access tokens
- Secure endpoints with Spring Security
- Role-based authorization (USER / ADMIN)
- H2 in-memory database
- Swagger/OpenAPI documentation
- Unit testing support

---

## Technologies

- Java 21
- Spring Boot
- Spring Security
- Spring Data JPA
- JWT
- H2 Database
- Maven
- Swagger/OpenAPI
- JUnit 5
- Mockito
- Java Faker

---

## Project Structure

src/main/java
├── config
├── controller
├── dto
├── entity
├── repository
├── security
├── service
└── exception

---

## API Endpoints

### Generate Users

GET /api/users/generate?count=100

Generates a JSON file containing realistic user data and triggers download.

---

### Import Users

POST /api/users/batch

Upload file:

multipart/form-data
file = users.json

Response:

{
  "total": 100,
  "imported": 95,
  "rejected": 5
}

---

### Authentication

POST /api/auth

Request:

{
  "username": "john",
  "password": "password"
}

Response:

{
  "accessToken": "jwt-token"
}

You can login using username or email.

---

### My Profile

GET /api/users/me

Requires JWT authentication.

---

### User Profile

GET /api/users/{username}

Rules:
- ADMIN → can access any user
- USER → only their own profile

---

## Running the Project

git clone https://github.com/badrcherhoune/jwt-user-management-api.git
cd user-management-api
mvn spring-boot:run

App runs on:
http://localhost:9090

---

## Swagger

http://localhost:9090/swagger-ui.html

OpenAPI:
http://localhost:9090/v3/api-docs

---

## H2 Database

JDBC URL: jdbc:h2:mem:testdb  
Username: sa  
Password: (empty)

H2 Console:
http://localhost:9090/h2-console

---

## Tests

mvn test

---

## Security

- BCrypt password encryption
- JWT authentication
- Stateless security
- Role-based access control

---

## Author

Badr Cherhoune
Java Full Stack Developer
Spring Boot | Angular | Docker | Kubernetes
