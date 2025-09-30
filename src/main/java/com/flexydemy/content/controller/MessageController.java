package com.flexydemy.content.controller;


import com.flexydemy.content.dto.MessageDTO;
import com.flexydemy.content.dto.MessageListDisplayDTO;
import com.flexydemy.content.dto.MessageThreadDTO;
import com.flexydemy.content.service.MessageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @PostMapping("/send")
    public ResponseEntity<MessageDTO> sendMessage(
            @RequestParam String senderId,
            @RequestParam String receiverId,
            @RequestParam String content,
            HttpServletRequest request
    ) {
        return ResponseEntity.ok(messageService.sendMessage(senderId, receiverId, content, request));
    }

    @PostMapping("/threads/{threadId}/read")
    public ResponseEntity<String> markThreadAsRead(@PathVariable String threadId) {
        return ResponseEntity.ok(messageService.markThreadAsRead(threadId));
    }

    @GetMapping("/threads/{threadId}")
    public ResponseEntity<MessageThreadDTO> getThread(@PathVariable String threadId, @RequestParam String viewerId,  HttpServletRequest request) {
        return ResponseEntity.ok(messageService.getFullMessageThread(threadId, viewerId, request));
    }

    @GetMapping("/threads")
    public ResponseEntity<List<MessageListDisplayDTO>> getAllThreadsForUser(@RequestParam String userId, HttpServletRequest request) {
        return ResponseEntity.ok(messageService.getAllMessageThreadsForUser(userId, request));
    }
}
