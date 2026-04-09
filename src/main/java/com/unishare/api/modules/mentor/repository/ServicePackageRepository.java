package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {

    List<ServicePackage> findByMentorId(Long mentorId);

    List<ServicePackage> findByMentorIdAndStatus(Long mentorId, String status);

    Optional<ServicePackage> findByIdAndMentorId(Long id, Long mentorId);
}
