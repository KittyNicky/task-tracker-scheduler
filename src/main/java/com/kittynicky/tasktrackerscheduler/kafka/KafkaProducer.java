package com.kittynicky.tasktrackerscheduler.kafka;

import com.kittynicky.tasktrackerscheduler.dto.ReportMailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Slf4j
@RequiredArgsConstructor
@Component
public class KafkaProducer {
    private final KafkaTemplate<String, ReportMailResponse> kafkaTemplate;

    public void sendMessage(String topic, ReportMailResponse reportMailResponse) {
        CompletableFuture<SendResult<String, ReportMailResponse>> future = kafkaTemplate.send(topic, reportMailResponse);
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Sent message=[\n" + reportMailResponse + "] with offset=[" + result.getRecordMetadata().offset() + "]");
            } else {
                log.error("Unable to send message=[\n" + reportMailResponse + "] due to: " + ex.getMessage());
            }
        });
    }
}
