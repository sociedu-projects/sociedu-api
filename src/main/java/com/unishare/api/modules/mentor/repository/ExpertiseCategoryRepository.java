package com.unishare.api.modules.mentor.repository;

import com.unishare.api.modules.mentor.entity.ExpertiseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExpertiseCategoryRepository extends JpaRepository<ExpertiseCategory, Long> {

    List<ExpertiseCategory> findByParentIdIsNullOrderBySortOrderAsc();

    List<ExpertiseCategory> findByParentIdOrderBySortOrderAsc(Long parentId);

    List<ExpertiseCategory> findAllByOrderBySortOrderAsc();
}
