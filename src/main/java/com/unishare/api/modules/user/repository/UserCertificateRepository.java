package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCertificateRepository extends JpaRepository<UserCertificate, Long> {
    List<UserCertificate> findByUserId(Long userId);
}
