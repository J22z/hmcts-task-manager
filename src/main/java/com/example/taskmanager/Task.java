package com.example.taskmanager;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

/**
 * Represents a task stored in the database.
 * Each task has a title, optional description, status and due date.
 */
@Entity
@Table(name = "tasks")
public class Task {

    // The database generates an ID for each new task.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // A title is required and cannot exceed 200 characters.
    @NotBlank(message = "Task title is required")
    @Size(max = 200)
    @Column(nullable = false)
    private String title;

    private String description;

    @NotBlank(message = "Status is required")
    @Column(nullable = false)
    private String status;

    // Tasks need a due date so they can be tracked.
    @NotNull(message = "Due date is required")
    @Column(nullable = false)
    private LocalDateTime dueDate;

    // Required by JPA when loading tasks from the database.
    public Task() {
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}