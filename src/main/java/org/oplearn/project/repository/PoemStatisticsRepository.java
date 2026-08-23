package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemStatisticsResponse;
import org.oplearn.project.entity.PoemStatistics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface PoemStatisticsRepository extends JpaRepository<PoemStatistics, Long> {

  Optional<PoemStatistics> findByPoemIdAndIsDeletedFalse(Long poemId);

  Optional<PoemStatistics> getByPoemId(Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.viewCount = s.viewCount + 1, s.updatedAt = CURRENT_TIMESTAMP WHERE s.poemId = :poemId")
  int incrementViewCount(@Param("poemId") Long poemId);

  @Query("""
      SELECT new org.oplearn.project.dto.response.PoemStatisticsResponse(
          s.poemId, s.viewCount, s.favoriteCount, s.shareCount, s.commentCount)
      FROM PoemStatistics s
      WHERE s.poemId = :poemId AND s.isDeleted = false
      """)
  Optional<PoemStatisticsResponse> getResponseByPoemId(@Param("poemId") Long poemId);

  @Query("""
      SELECT new org.oplearn.project.dto.response.PoemStatisticsResponse(
          s.poemId, s.viewCount, s.favoriteCount, s.shareCount, s.commentCount)
      FROM PoemStatistics s
      WHERE s.id = :id AND s.isDeleted = false
      """)
  Optional<PoemStatisticsResponse> findByIdAndReturnResponse(@Param("id") Long id);

  boolean existsByPoemId(Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.viewCount = s.viewCount + :delta WHERE s.poemId = :poemId")
  int addViewCount(@Param("poemId") Long poemId, @Param("delta") long delta);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.favoriteCount = s.favoriteCount + 1 WHERE s.poemId = :poemId")
  int incrementFavoriteCount(@Param("poemId") Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.favoriteCount = CASE WHEN s.favoriteCount > 0 THEN s.favoriteCount - 1 ELSE 0 END WHERE s.poemId = :poemId")
  int decrementFavoriteCount(@Param("poemId") Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.shareCount = s.shareCount + 1 WHERE s.poemId = :poemId")
  int incrementShareCount(@Param("poemId") Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.commentCount = s.commentCount + 1 WHERE s.poemId = :poemId")
  int incrementCommentCount(@Param("poemId") Long poemId);

  @Transactional
  @Modifying
  @Query("UPDATE PoemStatistics s SET s.commentCount = CASE WHEN s.commentCount > 0 THEN s.commentCount - 1 ELSE 0 END WHERE s.poemId = :poemId")
  int decrementCommentCount(@Param("poemId") Long poemId);
}
