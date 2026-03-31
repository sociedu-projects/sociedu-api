package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Long> {
    List<ServicePackage> findByMentorId(Long mentorId);
}
