package com.store.taskmanager.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "tasks")
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String associateId;

    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    private int priorityScore;
    private String category;
    private String aiReasoning;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Default Constructor
    public Task() {}

    // Builder Constructor
    private Task(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.associateId = builder.associateId;
        this.status = builder.status;
        this.priorityScore = builder.priorityScore;
        this.category = builder.category;
        this.aiReasoning = builder.aiReasoning;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    // Lifecycle Hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Builder Entry Point
    public static Builder builder() {
        return new Builder();
    }

    // Builder Static Inner Class
    public static class Builder {
        private Long id;
        private String title;
        private String description;
        private String associateId;
        private TaskStatus status;
        private int priorityScore;
        private String category;
        private String aiReasoning;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long v) {
            this.id = v;
            return this;
        }

        public Builder title(String v) {
            this.title = v;
            return this;
        }

        public Builder description(String v) {
            this.description = v;
            return this;
        }

        public Builder associateId(String v) {
            this.associateId = v;
            return this;
        }

        public Builder status(TaskStatus v) {
            this.status = v;
            return this;
        }

        public Builder priorityScore(int v) {
            this.priorityScore = v;
            return this;
        }

        public Builder category(String v) {
            this.category = v;
            return this;
        }

        public Builder aiReasoning(String v) {
            this.aiReasoning = v;
            return this;
        }

        public Builder createdAt(LocalDateTime v) {
            this.createdAt = v;
            return this;
        }

        public Builder updatedAt(LocalDateTime v) {
            this.updatedAt = v;
            return this;
        }

        public Task build() {
            return new Task(this);
        }
    }

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAssociateId() { return associateId; }
    public TaskStatus getStatus() { return status; }
    public int getPriorityScore() { return priorityScore; }
    public String getCategory() { return category; }
    public String getAiReasoning() { return aiReasoning; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setAssociateId(String associateId) { this.associateId = associateId; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public void setPriorityScore(int priorityScore) { this.priorityScore = priorityScore; }
    public void setCategory(String category) { this.category = category; }
    public void setAiReasoning(String aiReasoning) { this.aiReasoning = aiReasoning; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }
    public void setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
