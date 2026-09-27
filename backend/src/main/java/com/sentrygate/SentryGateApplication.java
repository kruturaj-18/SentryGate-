package com.sentrygate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class SentryGateApplication {
    public static void main(String[] args) {
        SpringApplication.run(SentryGateApplication.class, args);
    }
}
