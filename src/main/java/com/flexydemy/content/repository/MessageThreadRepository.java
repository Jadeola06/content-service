package com.flexydemy.content.repository;


import com.flexydemy.content.model.MessageThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface MessageThreadRepository extends JpaRepository<MessageThread, String> {
    Optional<MessageThread> findByThreadKey(String threadKey);

    List<MessageThread> findBySenderIdOrReceiverId(String senderId, String receiverId);

}
