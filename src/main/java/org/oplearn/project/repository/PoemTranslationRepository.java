package org.oplearn.project.repository;

import org.oplearn.project.entity.PoemTranslation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PoemTranslationRepository extends JpaRepository<PoemTranslation, Long> {
  List<PoemTranslation> findByPoemIdAndIsDeletedFalseOrderBySortOrderAsc(Long poemId);
}
