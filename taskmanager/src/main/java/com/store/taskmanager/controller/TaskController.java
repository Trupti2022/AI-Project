package com.store.taskmanager.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.store.taskmanager.model.Task;
import com.store.taskmanager.model.TaskStatus;
import com.store.taskmanager.service.TaskService;

@RestController
@RequestMapping("/api")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping("/tasks/{associateId}")
    public List<Task> getTasks(@PathVariable String associateId) {
        return taskService.getTasksForAssociate(associateId);
    }

    @PostMapping("/tasks")
    public ResponseEntity<Task> createTask(@RequestBody Map<String, String> body) {
        Task task = taskService.createTask(
                body.get("associateId"),
                body.get("title"),
                body.get("description"),
                body.getOrDefault("category", "ADHOC")
        );
        return ResponseEntity.ok(task);
    }

    @PutMapping("/tasks/{id}/start")
    public ResponseEntity<Task> startTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateStatus(id, TaskStatus.IN_PROGRESS));
    }

    @PutMapping("/tasks/{id}/pause")
    public ResponseEntity<Task> pauseTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateStatus(id, TaskStatus.PAUSED));
    }

    @PutMapping("/tasks/{id}/resume")
    public ResponseEntity<Task> resumeTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateStatus(id, TaskStatus.IN_PROGRESS));
    }

    @PutMapping("/tasks/{id}/complete")
    public ResponseEntity<Task> completeTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.updateStatus(id, TaskStatus.COMPLETED));
    }
}