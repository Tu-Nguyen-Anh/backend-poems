package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FavoriteRepository extends JpaRepository<Favorite, Long> {

  boolean existsByUserIdAndPoemId(Long userId, Long poemId);

  @Transactional
  @Modifying
  @Query("DELETE FROM Favorite f WHERE f.userId = :userId AND f.poemId = :poemId")
  void deleteByUserIdAndPoemId(@Param("userId") Long userId, @Param("poemId") Long poemId);

  /** Danh sách bài thơ đã thích của 1 user, mới thích trước (projection như findLatest). */
  @Query("""
          SELECT new org.oplearn.project.dto.response.PoemResponse(
                    p.id, p.name, p.description, p.year, p.content,
                    p.transliteration, p.translation, p.language, p.era,
                    g.name, a.name)
          FROM Favorite f
          JOIN Poem p ON f.poemId = p.id
          LEFT JOIN Author a ON p.authorId = a.id
          LEFT JOIN Genre g ON p.genreId = g.id
          WHERE f.userId = :userId AND p.isDeleted = false
          ORDER BY f.createdAt DESC
          """)
  Page<PoemResponse> findFavoritePoemsByUserId(@Param("userId") Long userId, Pageable pageable);
}
