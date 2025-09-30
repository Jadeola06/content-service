package com.flexydemy.content.service;


import com.flexydemy.content.dto.*;
import com.flexydemy.content.enums.NotificationTypes;
import com.flexydemy.content.exceptions.ResourceNotFoundException;
import com.flexydemy.content.exceptions.UserRetrievalException;
import com.flexydemy.content.model.Message;
import com.flexydemy.content.model.MessageThread;
import com.flexydemy.content.repository.MessageRepository;
import com.flexydemy.content.repository.MessageThreadRepository;
import com.flexydemy.content.repository.clients.UserClient;
import com.flexydemy.content.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MessageService {

    @Autowired
    private final UserClient userRepository;

    @Autowired
    private final MessageRepository messageRepository;

    @Autowired
    private final MessageThreadRepository messageThreadRepository;

    @Autowired
    private final NotificationService notificationService;

    public MessageService(UserClient userRepository, MessageRepository messageRepository, MessageThreadRepository messageThreadRepository, NotificationService notificationService) {
        this.userRepository = userRepository;
        this.messageRepository = messageRepository;
        this.messageThreadRepository = messageThreadRepository;
        this.notificationService = notificationService;
    }

    public MessageDTO sendMessage(String senderId, String receiverId, String content, HttpServletRequest request) {
        String token = Utils.getToken(request);

        UserDto sender = userRepository.findById(senderId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Sending User", senderId)))
                .block();

        UserDto receiver = userRepository.findById(receiverId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Receiving User", receiverId)))
                .block();

        if (sender == null) {
            throw new UserRetrievalException("Sender", senderId);
        }
        if (receiver == null) {
            throw new UserRetrievalException("Receiver", receiverId);
        }

        String threadKey = generateThreadKey(senderId, receiverId);

        MessageThread thread = messageThreadRepository.findByThreadKey(threadKey)
                .orElseGet(() -> {
                    MessageThread newThread = new MessageThread();
                    newThread.setSenderId(senderId);
                    newThread.setReceiverId(receiverId);
                    newThread.setSenderFirstName(sender.getFirstName());
                    newThread.setSenderLastName(sender.getLastName());
                    newThread.setSenderProfileImageUrl(sender.getProfileImageUrl());
                    newThread.setReceiverFirstName(receiver.getFirstName());
                    newThread.setReceiverLastName(receiver.getLastName());
                    newThread.setReceiverProfileImageUrl(receiver.getProfileImageUrl());

                    return newThread;
                });

        // Create and save the message
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setContent(content);
        message.setThread(thread);

        messageRepository.save(message);

        thread.setRead(false);


        notificationService.sendNotification(
                receiverId,
                NotificationTypes.MESSAGE_RECEIVED,
                thread.getSenderFirstName() + " " + thread.getSenderLastName() + " sent you a message.",
                message.getContent(),
                threadKey,
                senderId);

        // Return a DTO
        return new MessageDTO(
                message.getId(),
                message.getSenderId(),
                message.getReceiverId(),
                message.getSentAt(),
                message.getContent()
        );
    }

    public String markThreadAsRead(String threadId) {
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageThread", threadId));

        if (!thread.isRead()) {
            thread.setRead(true);
            messageThreadRepository.save(thread);
        }

        return "Thread Successfully Marked As Read";
    }

    public MessageThreadDTO getFullMessageThread(String threadId, String viewerId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        // Fetch thread
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new ResourceNotFoundException("MessageThread", threadId));

        // Validate viewer exists
        UserDto viewer = userRepository.findById(viewerId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Viewer", viewerId)))
                .block();

        // Determine receiver (the other person in the thread)
        String receiverId = thread.getSenderId().equals(viewerId) ? thread.getReceiverId() : thread.getSenderId();

        UserDto receiver = userRepository.findById(receiverId, token)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Receiver", receiverId)))
                .block();

        // Load messages
        List<Message> messages = messageRepository.findByThread_IdOrderBySentAtAsc(threadId);

        // Map to DTOs
        List<MessageDTO> messageDTOs = messages.stream().map(msg -> {
            MessageDTO dto = new MessageDTO();
            dto.setId(msg.getId());
            dto.setSenderId(msg.getSenderId());
            dto.setReceiverId(msg.getReceiverId());
            dto.setContent(msg.getContent());
            dto.setSentAt(msg.getSentAt());
            return dto;
        }).collect(Collectors.toList());

        // Build thread DTO
        MessageThreadDTO threadDTO = new MessageThreadDTO();
        threadDTO.setThreadId(thread.getId());
        threadDTO.setReceiverId(receiver.getUserId());
        threadDTO.setReceiverFirstName(receiver.getFirstName());
        threadDTO.setReceiverLastName(receiver.getLastName());
        threadDTO.setReceiverProfileImageUrl(receiver.getProfileImageUrl());
        threadDTO.setLastMessageTime(thread.getLastMessageAt());
        threadDTO.setRead(thread.isRead());
        threadDTO.setMessages(messageDTOs);

        return threadDTO;
    }

    public List<MessageListDisplayDTO> getAllMessageThreadsForUser(String viewerId, HttpServletRequest request) {
        String token = Utils.getToken(request);

        List<MessageThread> threads = messageThreadRepository.findBySenderIdOrReceiverId(viewerId, viewerId);

        return threads.stream().map(thread -> {
            String otherUserId = thread.getSenderId().equals(viewerId)
                    ? thread.getReceiverId()
                    : thread.getSenderId();

            UserDto otherUser = userRepository.findById(otherUserId, token)
                    .switchIfEmpty(Mono.error(new ResourceNotFoundException("User", otherUserId)))
                    .block();

            Message lastMessage = messageRepository.findTopByThread_IdOrderBySentAtDesc(thread.getId()).orElse(null);

            MessageListDisplayDTO display = new MessageListDisplayDTO();
            display.setUserId(otherUserId);
            display.setName(otherUser.getFirstName() + " " + otherUser.getLastName());
            display.setProfileImageUrl(otherUser.getProfileImageUrl());
            display.setTime(lastMessage != null ? lastMessage.getSentAt() : thread.getLastMessageAt());
            display.setUserActive(false);
            display.setRead(!thread.isRead());

            return display;
        }).collect(Collectors.toList());
    }



    private String generateThreadKey(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + "-" + user2 : user2 + "-" + user1;
    }


}
