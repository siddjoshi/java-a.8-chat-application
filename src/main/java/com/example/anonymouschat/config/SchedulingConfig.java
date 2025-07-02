package com.example.anonymouschat.config;

import com.example.anonymouschat.service.ChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
public class SchedulingConfig {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

    @Autowired
    private ChatService chatService;

    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupInactiveUsers() {
        logger.debug("Running cleanup task for inactive users");
        chatService.cleanupInactiveUsers();
    }
}
