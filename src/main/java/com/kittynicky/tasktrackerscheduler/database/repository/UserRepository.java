package com.kittynicky.tasktrackerscheduler.database.repository;

import com.kittynicky.tasktrackerscheduler.database.entity.TaskStatus;
import com.kittynicky.tasktrackerscheduler.database.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query(value = "SELECT u " +
                   "FROM User AS u " +
                   "LEFT JOIN u.tasks AS t " +
                   "WHERE t.updatedAt >= :date " +
                   "      OR t.status = :status ")
    List<User> findAllByUpdatedAtGreaterThanOrStatus(LocalDateTime date, TaskStatus status);
}
