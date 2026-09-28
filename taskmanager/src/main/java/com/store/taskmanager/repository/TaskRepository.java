package com.store.taskmanager.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.store.taskmanager.model.Task;
import com.store.taskmanager.model.TaskStatus;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssociateIdAndStatusNotOrderByPriorityScoreDesc(String associateId, TaskStatus status);
    //get all tasks for an associate  excluding completed ones, sorted by pririty highest forst
    List<Task> findByAssociateIdOrderByPriorityScoreDesc(String associateId);
    //Get all tasks for an associate sorted by priority
}