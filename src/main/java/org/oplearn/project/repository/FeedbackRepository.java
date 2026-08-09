package org.oplearn.project.repository;

import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.entity.Feedback;
import org.oplearn.project.entity.FeedbackStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
  Optional<Feedback> findByIdAndIsDeletedFalse(Long id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.FeedbackResponse(
        f.id,
        f.content,
        f.userId,
        f.poemId,
        u.username,
        f.createdAt,
        f.status
        )
    FROM Feedback f
    LEFT JOIN User u ON f.userId = u.id
    WHERE f.id = :id
    AND f.isDeleted = false
    """)
  Optional<FeedbackResponse> findByIdAndReturnResponse(@Param("id") Long id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.FeedbackResponse(
        f.id,
        f.content,
        f.userId,
        f.poemId,
        u.username,
        f.createdAt,
        f.status
        )
    FROM Feedback f
    LEFT JOIN User u ON f.userId = u.id
    WHERE f.isDeleted = false
    ORDER BY f.createdAt DESC
    """)
  Page<FeedbackResponse> findAllByIsDeletedFalse(Pageable pageable);

  @Modifying
  @Transactional
  @Query("update Feedback f set f.isDeleted = true where f.id = :id and f.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.FeedbackResponse(
        f.id,
        f.content,
        f.userId,
        f.poemId,
        u.username,
        f.createdAt,
        f.status
        )
    FROM Feedback f
    LEFT JOIN User u ON f.userId = u.id
    WHERE f.poemId = :poemId
    AND f.isDeleted = false
    ORDER BY f.createdAt DESC
    """)
  Page<FeedbackResponse> findFeedbackByPoemId(@Param("poemId") Long poemId, Pageable pageable);

  @Query("""
    SELECT new org.oplearn.project.dto.response.FeedbackResponse(
        f.id,
        f.content,
        f.userId,
        f.poemId,
        u.username,
        f.createdAt,
        f.status
        )
    FROM Feedback f
    LEFT JOIN User u ON f.userId = u.id
    WHERE f.userId = :userId
    AND f.isDeleted = false
    ORDER BY f.createdAt DESC
    """)
  Page<FeedbackResponse> findFeedbackByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("""
    SELECT new org.oplearn.project.dto.response.FeedbackResponse(
        f.id,
        f.content,
        f.userId,
        f.poemId,
        u.username,
        f.createdAt,
        f.status
        )
    FROM Feedback f
    LEFT JOIN User u ON f.userId = u.id
    WHERE f.isDeleted = false
    AND f.status = :status
    """)
  Page<FeedbackResponse> findByStatus(@Param("status") FeedbackStatus status, Pageable pageable);
}
