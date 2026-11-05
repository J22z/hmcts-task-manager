package com.example.taskmanager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests the task controller without starting Spring Boot or using a real database.
 */
class TaskControllerUnitTests {

    private TaskRepository taskRepository;
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        // Use a mock repository so each test runs independently.
        taskRepository = mock(TaskRepository.class);
        taskController = new TaskController(taskRepository);
    }

    // A newly created task should always start as pending.
    @Test
    void shouldCreateTaskWithPendingStatus() {
        Task task = createTask();
        task.setStatus("COMPLETED");

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<Task> response = taskController.createTask(task);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("PENDING", response.getBody().getStatus());

        verify(taskRepository).save(task);
    }

    // The controller should return every task provided by the repository.
    @Test
    void shouldReturnAllTasks() {
        Task firstTask = createTask();
        Task secondTask = createTask();
        secondTask.setTitle("Review evidence");

        when(taskRepository.findAll())
                .thenReturn(List.of(firstTask, secondTask));

        List<Task> result = taskController.getAllTasks();

        assertEquals(2, result.size());
        assertEquals("Review evidence", result.get(1).getTitle());

        verify(taskRepository).findAll();
    }

    // An existing task should be returned when its ID is requested.
    @Test
    void shouldFindTaskById() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        Task result = taskController.getTaskById(1L);

        assertEquals("Prepare hearing", result.getTitle());

        verify(taskRepository).findById(1L);
    }

    // A missing task should produce a 404 response.
    @Test
    void shouldReturn404WhenTaskIsMissing() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskController.getTaskById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(taskRepository).findById(99L);
    }

    // A valid status change should be saved.
    @Test
    void shouldUpdateTaskStatus() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Task updated = taskController.updateTaskStatus(
                1L,
                Map.of("status", "COMPLETED")
        );

        assertEquals("COMPLETED", updated.getStatus());

        verify(taskRepository).save(task);
    }

    // Unsupported statuses should be rejected without saving anything.
    @Test
    void shouldRejectInvalidStatus() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskController.updateTaskStatus(
                        1L,
                        Map.of("status", "CANCELLED")
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    // A missing status should also be rejected.
    @Test
    void shouldRejectMissingStatus() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskController.updateTaskStatus(
                        1L,
                        Map.of()
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    // Deleting a task should remove the correct record.
    @Test
    void shouldDeleteExistingTask() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(taskRepository).delete(task);
    }

    // A task that doesn't exist should not trigger a delete operation.
    @Test
    void shouldRejectDeletingMissingTask() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskController.deleteTask(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

        verify(taskRepository, never()).delete(any(Task.class));
    }

    private Task createTask() {
        Task task = new Task();

        task.setTitle("Prepare hearing");
        task.setDescription("Check the documents");
        task.setStatus("PENDING");
        task.setDueDate(LocalDateTime.of(2026, 11, 10, 14, 30));

        return task;
    }
}