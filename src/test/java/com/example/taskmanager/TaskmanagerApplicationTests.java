package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/*
 * These tests use a separate in-memory database, so they won't
 * affect any tasks saved while running the application.
 */
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:taskmanager_test;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.h2.console.enabled=false"
})
@AutoConfigureMockMvc
class TaskmanagerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TaskRepository taskRepository;

	// Start each test with an empty database.
	@BeforeEach
	void setUp() {
		taskRepository.deleteAll();
	}

	// Check that Spring Boot starts without any issues.
	@Test
	void contextLoads() {
	}

	// A valid task should be saved and returned with a 201 response.
	@Test
	void shouldCreateTask() throws Exception {

		String task = """
                {
                    "title": "Review court documents",
                    "description": "Review evidence",
                    "status": "PENDING",
                    "dueDate": "2026-11-10T14:30:00"
                }
                """;

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(task))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Review court documents"))
				.andExpect(jsonPath("$.status").value("PENDING"));
	}

	// The task list endpoint should return a successful JSON response.
	@Test
	void shouldRetrieveAllTasks() throws Exception {

		mockMvc.perform(get("/api/tasks"))
				.andExpect(status().isOk())
				.andExpect(content().contentTypeCompatibleWith(
						MediaType.APPLICATION_JSON
				));
	}

	// Check that we can retrieve a saved task using its ID.
	@Test
	void shouldRetrieveTaskById() throws Exception {

		Task saved = saveTask();

		mockMvc.perform(get("/api/tasks/" + saved.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("Prepare hearing"));
	}

	// Requesting a task that doesn't exist should return 404.
	@Test
	void shouldReturn404ForMissingTask() throws Exception {

		mockMvc.perform(get("/api/tasks/999999"))
				.andExpect(status().isNotFound());
	}

	// Tasks without a title should be rejected.
	@Test
	void shouldRejectInvalidTask() throws Exception {

		String task = """
                {
                    "title": "",
                    "status": "PENDING",
                    "dueDate": "2026-11-10T14:30:00"
                }
                """;

		mockMvc.perform(post("/api/tasks")
						.contentType(MediaType.APPLICATION_JSON)
						.content(task))
				.andExpect(status().isBadRequest());
	}

	// Check that a task's status can be changed to completed.
	@Test
	void shouldUpdateTaskStatus() throws Exception {

		Task saved = saveTask();

		mockMvc.perform(patch("/api/tasks/" + saved.getId() + "/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"COMPLETED\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("COMPLETED"));
	}

	// Reject statuses that aren't supported by the application.
	@Test
	void shouldRejectInvalidStatus() throws Exception {

		Task saved = saveTask();

		mockMvc.perform(patch("/api/tasks/" + saved.getId() + "/status")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"INVALID\"}"))
				.andExpect(status().isBadRequest());
	}

	// After deleting a task, it should no longer be available.
	@Test
	void shouldDeleteTask() throws Exception {

		Task saved = saveTask();

		mockMvc.perform(delete("/api/tasks/" + saved.getId()))
				.andExpect(status().isNoContent());

		mockMvc.perform(get("/api/tasks/" + saved.getId()))
				.andExpect(status().isNotFound());
	}

	// Create a sample task for tests that need an existing record.
	private Task saveTask() {

		Task task = new Task();

		task.setTitle("Prepare hearing");
		task.setStatus("PENDING");
		task.setDueDate(
				LocalDateTime.of(2026, 11, 10, 14, 30)
		);

		return taskRepository.save(task);
	}
}