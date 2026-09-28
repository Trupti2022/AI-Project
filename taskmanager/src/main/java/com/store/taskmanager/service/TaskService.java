package com.store.taskmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.store.taskmanager.model.Task;
import com.store.taskmanager.model.TaskStatus;
import com.store.taskmanager.repository.TaskRepository;
import com.store.taskmanager.websocket.TaskWebSocketHandler;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final AiPriorityService aiPriorityService;
    private final TaskWebSocketHandler webSocketHandler;

    public TaskService(TaskRepository taskRepository,
                       AiPriorityService aiPriorityService,
                       TaskWebSocketHandler webSocketHandler) {
        this.taskRepository = taskRepository;
        this.aiPriorityService = aiPriorityService;
        this.webSocketHandler = webSocketHandler;
    }

    public List<Task> getTasksForAssociate(String associateId) {
        return taskRepository
            .findByAssociateIdAndStatusNotOrderByPriorityScoreDesc(
                associateId, TaskStatus.COMPLETED
            );
    }

    @Transactional
    public Task createTask(String associateId, String title,
                           String description, String category) {

        List<Task> currentTasks = getTasksForAssociate(associateId);
        List<String> currentTitles = currentTasks.stream()
                .map(Task::getTitle).toList();

        AiPriorityService.PriorityResult priority =
                aiPriorityService.scorePriority(title, description, currentTitles);

        // auto-pause any IN_PROGRESS task if new task has higher priority
        currentTasks.stream()
                .filter(t -> t.getStatus() == TaskStatus.IN_PROGRESS
                          && priority.score() > t.getPriorityScore())
                .forEach(t -> {
                    t.setStatus(TaskStatus.PAUSED);
                    taskRepository.save(t);
                });

        Task task = Task.builder()
                .title(title)
                .description(description)
                .associateId(associateId)
                .status(TaskStatus.PENDING)
                .priorityScore(priority.score())
                .category(category)
                .aiReasoning(priority.reasoning())
                .build();

        Task saved = taskRepository.save(task);
        webSocketHandler.broadcastToAssociate(
            associateId, getTasksForAssociate(associateId)
        );
        return saved;
    }

    @Transactional
    public Task updateStatus(Long taskId, TaskStatus newStatus) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found: " + taskId));

        task.setStatus(newStatus);
        Task saved = taskRepository.save(task);
        webSocketHandler.broadcastToAssociate(
            task.getAssociateId(), getTasksForAssociate(task.getAssociateId())
        );
        return saved;
    }
}
