package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CapabilityRepository extends JpaRepository<Capability, UUID> {

    /**
     * Fetch all capability names assigned to a user through their roles.
     */
    @Query("""
            SELECT DISTINCT c.name
            FROM UserRole ur, RoleCapability rc, Capability c
            WHERE ur.id.roleId = rc.id.roleId
              AND rc.id.capabilityId = c.id
              AND ur.id.userId = :userId
            """)
    List<String> findCapabilityNamesByUserId(@Param("userId") UUID userId);
}
