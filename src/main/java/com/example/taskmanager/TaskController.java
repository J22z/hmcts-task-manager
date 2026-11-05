package com.example.taskmanager;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * Handles requests for creating, viewing, updating and deleting tasks.
 */
@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskRepository taskRepository;

    public TaskController(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * Creates a task and sets its initial status to pending.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@Valid @RequestBody Task task) {
        task.setStatus("PENDING");
        Task savedTask = taskRepository.save(task);

        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    /**
     * Gets all tasks currently stored in the database.
     */
    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    /**
     * Finds a task by its ID. Returns 404 if it doesn't exist.
     */
    @GetMapping("/{id}")
    public Task getTaskById(@PathVariable Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Task not found"
                ));
    }

    /**
     * Updates the status of an existing task.
     */
    @PatchMapping("/{id}/status")
    public Task updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {

        Task task = getTaskById(id);

        String status = request.get("status");

        // Only allow the three statuses used by the task manager.
        if (status == null ||
                !List.of("PENDING", "IN_PROGRESS", "COMPLETED").contains(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Status must be PENDING, IN_PROGRESS or COMPLETED"
            );
        }

        task.setStatus(status);
        return taskRepository.save(task);
    }

    /**
     * Deletes a task by its ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {

        Task task = getTaskById(id);
        taskRepository.delete(task);

        return ResponseEntity.noContent().build();
    }
}