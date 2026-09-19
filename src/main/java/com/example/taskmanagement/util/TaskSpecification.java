package com.example.taskmanagement.util;

import com.example.taskmanagement.entity.Task;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class TaskSpecification {

    public static Specification<Task> filter(
        Task.Status status,
        Task.Priority priority,
        Long projectId,
        Long assignedUserId,
        String search
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }
            if (projectId != null) {
                predicates.add(cb.equal(root.get("project").get("id"), projectId));
            }
            if (assignedUserId != null) {
                predicates.add(cb.equal(root.get("assignedUser").get("id"), assignedUserId));
            }
            if (search != null && !search.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("title")), "%" + search.toLowerCase() + "%"));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
