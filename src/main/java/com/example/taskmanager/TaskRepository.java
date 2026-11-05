package com.example.taskmanager;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Handles database access for tasks.
 * Spring Data JPA provides the standard save, find and delete methodss.
 */
public interface TaskRepository extends JpaRepository<Task, Long> {
}