package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.Capability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CapabilityRepository extends JpaRepository<Capability, Integer> {

    /**
     * Fetch all capability names assigned to a user through their roles.
     */
    @Query("""
            SELECT DISTINCT c.name
            FROM Capability c
            JOIN RoleCapability rc ON rc.capabilityId = c.id
            JOIN UserRole ur ON ur.id.roleId = rc.roleId
            WHERE ur.id.userId = :userId
            """)
    List<String> findCapabilityNamesByUserId(Long userId);
}
