package com.unishare.api.modules.chat.repository;

import com.unishare.api.modules.chat.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    Optional<Conversation> findByBookingId(UUID bookingId);
}
