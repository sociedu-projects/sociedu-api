package com.unishare.api.modules.profile.repository;

import com.unishare.api.modules.user.entity.UserCertificate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository cho UserCertificate trong context module profile.
 */
@Repository
public interface ProfileCertificateRepository extends JpaRepository<UserCertificate, Long> {
    List<UserCertificate> findByUserId(Long userId);
}
