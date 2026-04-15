package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserExperienceRepository extends JpaRepository<UserExperience, UUID> {
    List<UserExperience> findByUserId(UUID userId);
}
