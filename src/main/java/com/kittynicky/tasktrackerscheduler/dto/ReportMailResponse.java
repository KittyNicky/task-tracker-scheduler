package com.kittynicky.tasktrackerscheduler.dto;

import lombok.*;

import java.lang.annotation.Target;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReportMailResponse {
    private final String subject = "It's a task-tracker-service daily report";
    private String email;
    private String text;
}