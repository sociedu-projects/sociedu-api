package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, UUID> {
    List<UserEducation> findByUserId(UUID userId);
}
