package org.oplearn.project.repository;

import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
  Optional<Reply> findByIdAndIsDeletedFalse(Long id);

  Long countByCommentIdAndIsDeletedFalse(Long commentId);

  Long countByUserIdAndIsDeletedFalse(Long userId);

  @Query("""
    SELECT new org.oplearn.project.dto.response.ReplyResponse(
        r.id,
        r.content,
        r.commentId,
        r.userId,
        r.createdAt,
        u.username,
        c.content
        )
    FROM Reply r
    LEFT JOIN User u ON r.userId = u.id
    LEFT JOIN Comment c ON r.commentId = c.id
    Where r.id = :replyId
    AND r.isDeleted = false
    ORDER BY r.createdAt DESC
    """)
  Optional<ReplyResponse> findByIdAndReturnResponse(@Param("replyId") Long replyId);

  @Query("""
    SELECT new org.oplearn.project.dto.response.ReplyResponse(
        r.id,
        r.content,
        r.commentId,
        r.userId,
        r.createdAt,
        u.username,
        c.content
        )
    FROM Reply r
    LEFT JOIN User u ON r.userId = u.id
    LEFT JOIN Comment c ON r.commentId = c.id
    Where r.isDeleted = false
    ORDER BY r.createdAt DESC
    """)
  Page<ReplyResponse> findAllByIsDeletedFalse(Pageable pageable);

  @Transactional
  @Modifying
  @Query("update Reply r set r.isDeleted = true where r.id = :id and r.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.ReplyItemResponse(
        r.id,
        r.userId,
        r.content,
        u.username,
        r.createdAt
        )
    FROM Reply r
    LEFT JOIN User u ON r.userId = u.id
    Where r.commentId = :commentId
    AND r.isDeleted = false
    AND (:cursor IS NULL OR r.id < :cursor)
    ORDER BY r.id DESC
    """)
  List<ReplyItemResponse> findByCommentId(
    @Param("commentId") Long commentId,
    @Param("cursor") Long cursor,
    Pageable pageable
  );

  @Query("""
        SELECT new org.oplearn.project.dto.response.ReplyResponse(
            r.id,
            r.content,
            r.commentId,
            r.userId,
            r.createdAt,
            u.username,
            c.content
            )
        FROM Reply r
        LEFT JOIN User u ON r.userId = u.id
        LEFT JOIN Comment c ON r.commentId = c.id
        WHERE r.userId = :userId
          AND r.isDeleted = false
          AND (:cursor IS NULL OR r.id < :cursor)
        ORDER BY r.id DESC
        """)
  List<ReplyResponse> findByUserId(
    @Param("userId") Long userId,
    @Param("cursor") Long cursor,
    Pageable pageable
  );
}
