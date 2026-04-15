package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.FieldOfStudy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FieldOfStudyRepository extends JpaRepository<FieldOfStudy, UUID> {
}
