
package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Poem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PoemRepository extends JpaRepository<Poem, Long> {

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
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.id = :id
        AND p.isDeleted = false
    """)
  Optional<PoemResponse> findByIdAndReturnResponse(@Param("id") Long id);

  Optional<Poem> findByIdAndIsDeletedFalse(Long id);

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
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.isDeleted = false
    """)
  Page<PoemResponse> findAllByIsDeletedFalse(Pageable pageable);

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
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.isDeleted = false
          AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
          )
    """)
  Page<PoemResponse> search(@Param("keyword") String keyword, Pageable pageable);


  @Modifying
  @Query("update Poem p set p.isDeleted = true where p.id = :id and p.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

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
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.isDeleted = false
    """)
  Page<PoemResponse> findLatest(Pageable pageable);

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
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.isDeleted = false
        ORDER BY function('random')
    """)
  Page<PoemResponse> findRandomPoem(Pageable pageable);

}
