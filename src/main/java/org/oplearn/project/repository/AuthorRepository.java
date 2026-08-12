package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Author;
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
public interface AuthorRepository extends JpaRepository<Author, Long> {
  Optional<Author> findByIdAndIsDeletedFalse(Long id);

  boolean existsByIdAndIsDeletedFalse(Long id);

  Page<Author> findAllByIsDeletedFalse(Pageable pageable);

  boolean existsByNameAndIsDeletedFalse(String name);

  @Query("""

    select a from Author a
        where a.isDeleted = false
               and(lower(a.name) like lower(concat('%' , :keyword , '%')))
    """)
  Page<Author> search(@Param("keyword") String keyword, Pageable pageable);

  @Transactional
  @Modifying
  @Query("update Author a set a.isDeleted = true where a.id = :id and a.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  @Query("SELECT a.id FROM Author a WHERE a.isDeleted = false ORDER BY a.id")
  java.util.List<Long> findIdsForSitemap();

  @Query("""
    SELECT new org.oplearn.project.dto.response.AuthorResponse(
      a.id, a.name, a.birthYear, a.achievement, a.hometown, COUNT(p.id)
    )
    FROM Author a
    LEFT JOIN Poem p ON p.authorId = a.id AND p.isDeleted = false
    WHERE a.isDeleted = false
    GROUP BY a.id, a.name, a.birthYear, a.achievement, a.hometown
    ORDER BY COUNT(p.id) DESC, a.id
    """)
  Page<org.oplearn.project.dto.response.AuthorResponse> findTopByPoemCount(Pageable pageable);

  @Query("""
    SELECT new org.oplearn.project.dto.response.PoemResponse(
              p.id,
              p.name,
              p.description,
              p.year,
              p.content,
              p.transliteration,
              p.translation,
              p.language,
              g.name,
              a.name
    )
    FROM Poem p
    LEFT JOIN Genre g ON p.genreId = g.id
    LEFT JOIN Author a ON p.authorId = a.id
    WHERE p.authorId = :authorId
    AND p.isDeleted = false
    AND g.isDeleted = false
    AND a.isDeleted = false
    """)
  Page<PoemResponse> findPoemByAuthorId(@Param("authorId") Long AuthorId, Pageable pageable);
}
