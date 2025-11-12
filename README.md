# Description
This is a part of my full stack web application Social Network. This part is API Server using Spring Boot framework with many library such as Lombok, Mapstruct, Apache Tika,... Not only having Spring Security authentication with JSON Web Token but also authorizing with 2 roles ADMIN and USER.

## Technologies Used
- **Java 17:** The programming language used for backend development.
- **Spring Boot:** A framework that simplifies the creation of RESTful APIs and microservices.
- **Spring Data JPA:** Provides functionality for data persistence and database operations.
- **MariaDB:** A relational database management system used for data storage.
- **Spring Security:** Handles authentication and authorization, including support for JWT-based security.
- **JSON Web Token (JWT):** Used for secure information exchange.
- **MapStruct:** A library for mapping between different object models.
- **Lombok:** A tool to reduce boilerplate code by generating common methods such as getters and setters.
- **Spring Boot Validation:** Provides validation support for request data.
- **Apache Tika:** Used for extracting metadata and text from various types of files.
- **Spring Boot WebSocket:** Enables WebSocket support for real-time communication such as Chat system, Notification system.

## Getting Started

### Prerequisites
- Java 17 or higher
- Gradle 7.0 or higher
- A running instance of a database (e.g., MySQL, PostgreSQL)
### Installation
1. Clone the repository:
```bash
git clone https://github.com/nhoclahola/SocialNetwork-SpringBoot-API
````

2. Navigate to the project directory:
```bash
cd social-network-api
````


3. Build the project using Gradle:
```bash
./gradlew build
````

4. Configure the application.properties file to point to your database. You may need to set the database URL, username, and password in this file.

### Running the Application
Run the Spring Boot application using Gradle:
```bash
./gradlew bootRun
```
The server will start on http://localhost:8080.

# Social Media API Documentation

## Overview
This API allows users to interact with a social media platform, providing functionalities like creating account, posting, liking, commenting, and following other users.

## Authentication
All endpoints start with "/api" require a Bearer token to be included in the `Authorization` header.
Example: 

Authorization: Bearer your_token_here

### HTTP Status Codes

- **200 OK:** The request was successful.
- **201 Created:** A new resource was successfully created.
- **400 Bad Request:** The request could not be understood or was missing required parameters.
- **401 Unauthorized:** Authentication failed or user does not have permissions.
- **403 Forbidden:** Authentication succeeded but authenticated user does not have access to the resource.
- **404 Not Found:** The requested resource could not be found.
- **500 Internal Server Error:** An error occurred on the server.

### Endpoints
#### 1. User Registration

- **URL:** `/auth/register`
- **Method:** `POST`
- **Description:** Register a new user.

##### Request Body:
```json
{
  "username": "youusername",
  "email": "youremail@example.com",
  "password": "yourpassword",
  "firstName": "yourfirstname",
  "lastName": "yourlastname",
}
````

##### Response:
```json
{
  "token": "your_jwt_token_here"
}
````

#### 2. Login

- **URL:** `/auth/login`
- **Method:** `POST`
- **Description:** Authenticate a user and return a JWT token.

##### Request Body:
```json
{
  "email": "youremail@example.com",
  "password": "yourpassword"
}
````

##### Response:
```json
{
  "token": "your_jwt_token_here"
}
````


# API Error Codes Documentation

## Overview
This document provides a list of all custom error codes used in the API, along with their corresponding HTTP status codes and descriptions.

## Error Codes

### 1. Authentication and Validation Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1100       | Unauthenticated                                 | 401 Unauthorized     | The user is not authenticated.                             |
| 1101       | You do not have permission to do this           | 403 Forbidden        | The user is authenticated but not authorized to perform the action. |
| 1102       | Invalid Token                                   | 400 Bad Request      | The provided token is invalid.                             |
| 1103       | Invalid request                                 | 400 Bad Request      | The request is invalid or malformed.                       |
| 1104       | This email has been used                        | 409 Conflict         | The email is already in use during registration.           |
| 1105       | This username has been used                     | 409 Conflict         | The username is already in use during registration.        |

### 2. User Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1200       | The user does not exist                         | 404 Not Found        | The specified user cannot be found.                        |
| 1201       | You can't follow yourself                       | 400 Bad Request      | A user cannot follow their own account.                    |

### 3. Post Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1300       | The post does not exist                         | 404 Not Found        | The specified post cannot be found.                        |

### 4. Comment Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1400       | The comment does not exist                      | 400 Bad Request      | The specified comment cannot be found.                     |

### 5. Video Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1500       | The video does not exist                        | 400 Bad Request      | The specified video cannot be found.                       |

### 6. Chat Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1600       | The chat does not exist                         | 400 Bad Request      | The specified chat cannot be found.                        |
| 1601       | User does not exist in the chat                 | 400 Bad Request      | The user is not a participant in the specified chat.       |

### 7. Message Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1700       | The message does not exist                      | 400 Bad Request      | The specified message cannot be found.                     |

### 8. File Upload Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1801       | The file you uploaded is empty                  | 400 Bad Request      | The uploaded file is empty.                                |
| 1801       | The image you uploaded is empty                 | 400 Bad Request      | The uploaded image is empty.                               |
| 1802       | The video you uploaded is empty                 | 400 Bad Request      | The uploaded video is empty.                               |
| 1803       | The image you uploaded is either not valid or not supported | 400 Bad Request | The uploaded image format is not supported or invalid.     |
| 1804       | The video you uploaded is either not valid or not supported | 400 Bad Request | The uploaded video format is not supported or invalid.     |
| 1805       | There is an error during the I/O process        | 500 Internal Server Error | An error occurred during file I/O operations.            |

### 9. Notification Errors

| Error Code | Message                                        | HTTP Status         | Description                                                |
|------------|------------------------------------------------|---------------------|------------------------------------------------------------|
| 1901       | The notification does not exist                 | 400 Bad Request      | The specified notification cannot be found.               |
| 1902       | That is not your notification                   | 400 Bad Request      | The notification does not belong to the user.             |

---

## Error Code Conventions
- **1XXX**: Authentication and validation related errors.
- **12XX**: User-related errors.
- **13XX**: Post-related errors.
- **14XX**: Comment-related errors.
- **15XX**: Video-related errors.
- **16XX**: Chat-related errors.
- **17XX**: Message-related errors.
- **18XX**: File upload errors.
- **19XX**: Notification-related errors.
