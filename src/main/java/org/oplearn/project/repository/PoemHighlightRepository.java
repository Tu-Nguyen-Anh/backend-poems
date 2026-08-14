package org.oplearn.project.repository;

import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.entity.PoemHighlight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PoemHighlightRepository extends JpaRepository<PoemHighlight, Long> {

  Optional<PoemHighlight> findByIdAndIsDeletedFalse(Long id);

  @Transactional
  @Modifying
  @Query("UPDATE PoemHighlight h SET h.isDeleted = true WHERE h.id = :id AND h.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  /** Highlight của user trên 1 bài (để render), sắp theo vị trí bắt đầu. */
  @Query("""
          SELECT new org.oplearn.project.dto.response.HighlightResponse(
                    h.id, h.poemId, h.startOffset, h.endOffset,
                    h.selectedText, h.note, h.createdAt)
          FROM PoemHighlight h
          WHERE h.userId = :userId AND h.poemId = :poemId AND h.isDeleted = false
          ORDER BY h.startOffset ASC
          """)
  List<HighlightResponse> findByUserIdAndPoemId(@Param("userId") Long userId, @Param("poemId") Long poemId);

  /** Toàn bộ highlight của user, kèm tên bài + tác giả — trang "Ghi chú của tôi". */
  @Query("""
          SELECT new org.oplearn.project.dto.response.HighlightWithPoemResponse(
                    h.id, h.poemId, p.name, a.name,
                    h.startOffset, h.endOffset, h.selectedText, h.note, h.createdAt)
          FROM PoemHighlight h
          JOIN Poem p ON h.poemId = p.id
          LEFT JOIN Author a ON p.authorId = a.id
          WHERE h.userId = :userId AND h.isDeleted = false AND p.isDeleted = false
          ORDER BY h.createdAt DESC
          """)
  Page<HighlightWithPoemResponse> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
