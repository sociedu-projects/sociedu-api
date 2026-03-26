package com.unishare.api.modules.auth.repository;

import com.unishare.api.modules.auth.entity.UserCredential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCredentialRepository extends JpaRepository<UserCredential, Long> {

    Optional<UserCredential> findByUserId(Long userId);
}
