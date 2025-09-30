package com.flexydemy.content.repository;

import com.flexydemy.content.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, String> {
    List<Message> findByThread_IdOrderBySentAtAsc(String threadId);

    List<Message> findTop3ByReceiverIdOrderBySentAtDesc(String receiverId);

    Optional<Message> findTopByThread_IdOrderBySentAtDesc(String id);
}
