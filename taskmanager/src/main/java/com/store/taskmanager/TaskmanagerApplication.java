package com.store.taskmanager;

import com.store.taskmanager.model.Task;
import com.store.taskmanager.model.TaskStatus;
import com.store.taskmanager.repository.TaskRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class TaskmanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TaskmanagerApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(TaskRepository repo) {
        return args -> {
            repo.save(Task.builder()
                    .title("Restock Shelves - Aisle 5")
                    .description("Replenish cereal and breakfast items")
                    .associateId("associate-1")
                    .status(TaskStatus.IN_PROGRESS)
                    .priorityScore(40)
                    .category("PLANNED")
                    .build());

            repo.save(Task.builder()
                    .title("BOPIS Order #4521")
                    .description("Collect items for online pickup order")
                    .associateId("associate-1")
                    .status(TaskStatus.PENDING)
                    .priorityScore(70)
                    .category("BOPIS")
                    .build());

            repo.save(Task.builder()
                    .title("Customer Assistance - Gluten Free")
                    .description("Help customer locate gluten-free products")
                    .associateId("associate-1")
                    .status(TaskStatus.PENDING)
                    .priorityScore(50)
                    .category("CUSTOMER")
                    .build());
        };
    }
}
