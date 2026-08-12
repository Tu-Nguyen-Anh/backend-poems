package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
  Optional<Genre> findByIdAndIsDeletedFalse(Long id);

  boolean existsByIdAndIsDeletedFalse(Long id);

  Page<Genre> findAllByIsDeletedFalse(Pageable pageable);

  boolean existsByNameAndIsDeletedFalse(String name);

  @Query("SELECT g.id FROM Genre g WHERE g.isDeleted = false ORDER BY g.id")
  java.util.List<Long> findIdsForSitemap();

  @Query("""

    select g from Genre g
        where g.isDeleted = false
               and(lower(g.name) like lower(concat('%' , :keyword , '%')))
    """)
  Page<Genre> search(@Param("keyword") String keyword, Pageable pageable);

  @Modifying
  @Query("update Genre g set g.isDeleted = true where g.id = :id and g.isDeleted = false")
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
    LEFT JOIN Genre g ON p.genreId = g.id
    LEFT JOIN Author a ON p.authorId = a.id
    WHERE p.genreId = :genreId
    AND p.isDeleted = false
    AND g.isDeleted = false
    AND a.isDeleted = false
    """)
  Page<PoemResponse> findPoemByGenreId(@Param("genreId") Long genreId, Pageable pageable);
}
