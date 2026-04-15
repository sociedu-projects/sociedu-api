package com.unishare.api.modules.chat.repository;

import com.unishare.api.modules.chat.entity.MessageAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageAttachmentRepository extends JpaRepository<MessageAttachment, UUID> {

    List<MessageAttachment> findByMessageId(UUID messageId);
}
