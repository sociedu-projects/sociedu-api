package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserEducation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserEducationRepository extends JpaRepository<UserEducation, Long> {
    List<UserEducation> findByUserId(Long userId);
}
