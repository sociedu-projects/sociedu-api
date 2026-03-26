package com.unishare.api.modules.user.repository;

import com.unishare.api.modules.user.entity.UserLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserLanguageRepository extends JpaRepository<UserLanguage, Long> {
    List<UserLanguage> findByUserId(Long userId);
}
