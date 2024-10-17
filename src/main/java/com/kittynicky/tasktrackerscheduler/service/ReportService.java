package com.kittynicky.tasktrackerscheduler.service;

import com.kittynicky.tasktrackerscheduler.database.entity.Task;
import com.kittynicky.tasktrackerscheduler.database.entity.TaskStatus;
import com.kittynicky.tasktrackerscheduler.database.entity.User;
import com.kittynicky.tasktrackerscheduler.database.repository.UserRepository;
import com.kittynicky.tasktrackerscheduler.dto.ReportMailResponse;
import com.kittynicky.tasktrackerscheduler.kafka.KafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    private final UserRepository userRepository;
    private final KafkaProducer kafkaProducer;

    @Value("${spring.kafka.email-sending-tasks-topic}")
    private String topic;

    @Scheduled(cron = "0 0 0 * * MON-FRI")
    public void report() {
        LocalDateTime date = LocalDateTime.now().minusDays(1);
        TaskStatus status = TaskStatus.CREATED;

        userRepository.findAllByUpdatedAtGreaterThanOrStatus(date, status)
                .stream()
                .map(user -> {
                    var text = buildText(user);
                    return ReportMailResponse.builder()
                            .email(user.getEmail())
                            .text(text)
                            .build();
                })
                .forEach(mailResponse -> kafkaProducer.sendMessage(topic, mailResponse));
    }

    private String buildText(User user) {
        StringBuilder text = new StringBuilder();

        var unfinishedTasksText = buildUnfinishedTasksText(user);
        if (unfinishedTasksText != null) {
            text.append(unfinishedTasksText);
        }

        var completedTasksForDay = buildCompletedTasksForDay(user);
        if (completedTasksForDay != null) {
            text.append(completedTasksForDay);
        }

        if (!text.isEmpty()) {
            text.insert(0, "<h1>Dear " + user.getUsername() + ", this is a daily task report for you.</h1>");
        }

        return text.toString();

    }

    private String buildCompletedTasksForDay(User user) {
        var completedTasksForDay = getCompletedTasksTitlesForDay(user);

        if (!completedTasksForDay.isEmpty()) {
            return String.format("""
                            <h3>You have %d completed task(s) for a day.</h3>
                            <p>Your completed task(s): %s</p>
                            """,
                    completedTasksForDay.size(),
                    completedTasksForDay.stream()
                            .limit(5)
                            .collect(Collectors.joining(", ")));
        }
        return null;
    }

    private List<String> getCompletedTasksTitlesForDay(User user) {
        return user.getTasks()
                .stream()
                .filter(task -> task.getStatus().equals(TaskStatus.DONE)
                                && task.getUpdatedAt().isAfter(LocalDateTime.now().minusDays(1)))
                .sorted(Comparator.comparing(Task::getTitle))
                .map(task -> "\"" + task.getTitle() + "\"")
                .collect(Collectors.toList());
    }

    private String buildUnfinishedTasksText(User user) {
        var unfinishedTasksTitles = getUnfinishedTasksTitles(user);

        if (!unfinishedTasksTitles.isEmpty()) {
            return String.format("""
                    <h3>You have %d unfinished task(s).</h3>
                    <p>Your unfinished task(s): %s</p>
                    """, unfinishedTasksTitles.size(), unfinishedTasksTitles.stream().limit(5).collect(Collectors.joining(", ")));
        }
        return null;
    }

    private List<String> getUnfinishedTasksTitles(User user) {
        return user.getTasks().stream().filter(task -> task.getStatus().equals(TaskStatus.CREATED)).sorted(Comparator.comparing(Task::getTitle)).map(task -> "\"" + task.getTitle() + "\"").collect(Collectors.toList());
    }
}
