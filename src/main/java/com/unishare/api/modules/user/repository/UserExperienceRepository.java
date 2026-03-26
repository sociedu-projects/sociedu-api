package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserExperienceRepository extends JpaRepository<UserExperience, Long> {
    List<UserExperience> findByUserId(Long userId);
}
