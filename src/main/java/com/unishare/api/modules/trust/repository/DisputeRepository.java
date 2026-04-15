package com.unishare.api.modules.trust.repository;

import com.unishare.api.modules.trust.entity.Dispute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DisputeRepository extends JpaRepository<Dispute, UUID> {

    List<Dispute> findByRaisedByOrderByCreatedAtDesc(UUID raisedBy);
}
