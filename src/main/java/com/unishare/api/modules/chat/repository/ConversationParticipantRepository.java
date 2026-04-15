package com.unishare.api.modules.chat.repository;

import com.unishare.api.modules.chat.entity.ConversationParticipant;
import com.unishare.api.modules.chat.entity.ConversationParticipantId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface ConversationParticipantRepository extends JpaRepository<ConversationParticipant, ConversationParticipantId> {

    @Query("SELECT cp.id.conversationId FROM ConversationParticipant cp WHERE cp.id.userId = :userId")
    List<UUID> findConversationIdsByUserId(@Param("userId") UUID userId);

    @Query("SELECT COUNT(c) > 0 FROM ConversationParticipant c WHERE c.id.conversationId = :cid AND c.id.userId = :uid")
    boolean isParticipant(@Param("cid") UUID conversationId, @Param("uid") UUID userId);
}
