package org.oplearn.project.repository;

import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.entity.Comment;
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
public interface CommentRepository extends JpaRepository<Comment , Long> {
  @Transactional
  @Modifying
  @Query("update Comment c set c.isDeleted = true where c.id = :id and c.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  Optional<Comment> findByIdAndIsDeletedFalse(Long id);

  @Query("""
          SELECT new org.oplearn.project.dto.response.CommentResponse(
                    c.id,
                    c.content,
                    c.poemId,
                    c.userId,
                    u.username,
                    c.createdAt
                    )
          FROM Comment c
          LEFT JOIN User u ON c.userId = u.id
          WHERE c.id = :id
          AND c.isDeleted = false
          """)
  Optional<CommentResponse> findByIdAndReturnResponse(@Param("id") Long id);

  @Query("""
          SELECT new org.oplearn.project.dto.response.CommentResponse(
                    c.id,
                    c.content,
                    c.poemId,
                    c.userId,
                    u.username,
                    c.createdAt
                    )
          FROM Comment c
          LEFT JOIN User u ON c.userId = u.id
          WHERE c.poemId = :poemId
          AND c.isDeleted = false
          ORDER BY c.createdAt DESC
          """)
  Page<CommentResponse> findByPoemId(@Param("poemId") Long poemId, Pageable pageable);

  @Query("""
            SELECT new org.oplearn.project.dto.response.CommentResponse(
                      c.id,
                      c.content,
                      c.poemId,
                      c.userId,
                      u.username,
                      c.createdAt
                      )
            FROM Comment c
            LEFT JOIN User u ON c.userId = u.id
            WHERE c.userId = :userId
            AND c.isDeleted = false
            ORDER BY c.createdAt DESC
            """)
  Page<CommentResponse> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
