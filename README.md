HMCTS Task Manager

A full-stack task management application built for the HMCTS DTS Developer Technical Test.

The application allows caseworkers to create, view, update and delete tasks through a simple, responsive interface. Tasks are saved in a database so they remain available after the application is restarted.

Features

* Create tasks with a title, optional description and due date/time.
* View all tasks in one place.
* Update task statuses between Pending, In Progress and Completed.
* Delete tasks with a confirmation prompt.
* Filter tasks by status.
* View task counts for each status.
* Highlight overdue tasks.
* Display helpful error messages when something goes wrong.
* Responsive layout for desktop and smaller screens.

Technologies Used

Backend

* Java
* Spring Boot
* Spring Data JPA
* H2 Database
* Maven

Frontend

* React
* JavaScript
* Vite
* CSS

Testing

* JUnit 5
* Mockito
* Spring Boot MockMvc

Getting Started

Prerequisites

Before running the application, make sure you have:

* Java (compatible with the version configured in pom.xml)
* Node.js and npm

1. Clone the repository

git clone <YOUR_GITHUB_REPOSITORY_URL>
cd taskmanager

Replace the placeholder with your GitHub repository URL after uploading the project.

2. Start the backend

From the main project folder, run:

./mvnw spring-boot:run

On Windows:

.\mvnw.cmd spring-boot:run

The backend runs at:

http://localhost:8080

3. Start the frontend

Open a second terminal and run:

cd frontend
npm install
npm run dev

The frontend runs at:

http://localhost:5173

Open this address in your browser to use the application.

Both servers need to be running for the application to work correctly.

API Endpoints

The backend provides the following REST API endpoints:

Method	Endpoint	Description
POST	/api/tasks	Create a task
GET	/api/tasks	Retrieve all tasks
GET	/api/tasks/{id}	Retrieve a task by ID
PATCH	/api/tasks/{id}/status	Update a task’s status
DELETE	/api/tasks/{id}	Delete a task

New tasks are automatically assigned the PENDING status.

Supported statuses are:

* PENDING
* IN_PROGRESS
* COMPLETED

For detailed request examples, responses, validation rules and HTTP status codes, see API Documentation.

Database

The application uses an H2 file-based database.

The database connection is configured in src/main/resources/application.properties.

Using a file-based database means tasks remain stored between application restarts without needing to install a separate database server.

Hibernate is configured to update the database schema during development.

Validation and Error Handling

The backend validates incoming requests and handles common errors.

This includes:

* Rejecting tasks with missing or blank titles.
* Limiting task titles to 200 characters.
* Requiring a due date and time.
* Rejecting unsupported task statuses.
* Returning 400 Bad Request for invalid requests.
* Returning 404 Not Found when a task cannot be found.

The frontend also displays error messages when API requests fail.

Testing

The application includes automated unit and integration tests.

Unit Tests

The unit tests use JUnit 5 and Mockito to test the controller independently of the database.

They cover task creation, retrieval, status updates, deletion and error handling.

Integration Tests

The integration tests use Spring Boot and MockMvc to verify API behaviour, including validation and HTTP responses.

Running the Tests

From the main project folder, run:

./mvnw test

The latest test run completed successfully:

Tests run: 18, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS

This includes:

* 9 unit tests
* 9 integration tests
 
Project Structure

```text
taskmanager/
├── src/
│   ├── main/
│   │   ├── java/com/example/taskmanager/
│   │   │   ├── TaskmanagerApplication.java
│   │   │   ├── Task.java
│   │   │   ├── TaskRepository.java
│   │   │   └── TaskController.java
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── java/com/example/taskmanager/
│           ├── TaskmanagerApplicationTests.java
│           └── TaskControllerUnitTests.java
├── frontend/
│   ├── src/
│   │   ├── App.jsx
│   │   ├── App.css
│   │   └── index.css
│   ├── package.json
│   └── vite.config.js
├── API.md
├── README.md
└── pom.xml
```

Design Decisions

I chose Spring Boot for the backend because it provides a straightforward way to develop REST APIs, handle validation and connect to a database.

For the frontend, I used React because it makes it easier to build an interactive interface where tasks can be created, updated and removed without refreshing the page.

I chose H2 as the database because it is lightweight and easy to configure. Its file-based storage also allows tasks to persist between restarts.

I focused on keeping the application straightforward and meeting the requirements of the technical test. I also added status filtering, task counters and overdue indicators to make it easier for caseworkers to manage their workload.

Future Improvements

With more time, I would consider:

* Adding authentication and individual caseworker accounts.
* Allowing users to edit task titles, descriptions and due dates.
* Adding task searching and sorting.
* Moving to PostgreSQL for a production environment.
* Adding automated frontend tests.

About

Developed for the HMCTS DTS Developer Technical Test.
