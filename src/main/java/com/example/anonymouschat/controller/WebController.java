package com.example.anonymouschat.controller;

import com.example.anonymouschat.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class WebController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/api/stats")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineUsers", chatService.getOnlineUsersCount());
        stats.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    @ResponseBody
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(status);
    }
}
