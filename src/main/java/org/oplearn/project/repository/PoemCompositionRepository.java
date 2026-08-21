package org.oplearn.project.repository;

import org.oplearn.project.dto.response.PoemCompositionResponse;
import org.oplearn.project.entity.PoemComposition;
import org.oplearn.project.enums.PoemCompositionStatus;
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
public interface PoemCompositionRepository extends JpaRepository<PoemComposition, Long> {

  @Modifying
  @Transactional
  @Query("update PoemComposition pc set pc.isDeleted = true where pc.id = :id and pc.isDeleted = false")
  void SoftDeleteById(@Param("id") Long id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.PoemCompositionResponse(
        pc.id,
        pc.userId,
        u.username,
        pc.content,
        pc.penName,
        pc.title,
        pc.genreId,
        g.name,
        pc.status
        )
    FROM PoemComposition pc
    LEFT JOIN User u ON pc.userId = u.id
    LEFT JOIN Genre g ON pc.genreId = g.id
    WHERE pc.id = :id
    AND pc.isDeleted = false
    """)
  Optional<PoemCompositionResponse> findByIdAndReturnResponse(@Param("id") Long id);

  @Query(value = "SELECT id FROM poem_compositions WHERE is_deleted = false AND status = 'PUBLISHED' ORDER BY random() LIMIT :n", nativeQuery = true)
  List<Long> findRandomIds(@Param("n") int n);

  @Query("""
    SELECT new org.oplearn.project.dto.response.PoemCompositionResponse(
        pc.id,
        pc.userId,
        u.username,
        pc.content,
        pc.penName,
        pc.title,
        pc.genreId,
        g.name,
        pc.status
        )
    FROM PoemComposition pc
    LEFT JOIN User u ON pc.userId = u.id
    LEFT JOIN Genre g ON pc.genreId = g.id
    WHERE pc.id IN :id
    AND pc.isDeleted = false
    """)
  List<PoemCompositionResponse> findResponsesByIds(@Param("id") List<Long> id);

  @Query("""
    SELECT new org.oplearn.project.dto.response.PoemCompositionResponse(
        pc.id,
        pc.userId,
        u.username,
        pc.content,
        pc.penName,
        pc.title,
        pc.genreId,
        g.name,
        pc.status
        )
    FROM PoemComposition pc
    LEFT JOIN User u ON pc.userId = u.id
    LEFT JOIN Genre g ON pc.genreId = g.id
    WHERE pc.userId = :userId
    AND pc.isDeleted = false
    """)
  Page<PoemCompositionResponse> findByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("""
      SELECT new org.oplearn.project.dto.response.PoemCompositionResponse(
        pc.id,
        pc.userId,
        u.username,
        pc.content,
        pc.penName,
        pc.title,
        pc.genreId,
        g.name,
        pc.status
        )
      FROM PoemComposition pc
      LEFT JOIN User u ON pc.userId = u.id
      LEFT JOIN Genre g ON pc.genreId = g.id
      WHERE pc.userId = :userId
        AND pc.status = 'PUBLISHED'
        AND pc.isDeleted = false
      """)
  Page<PoemCompositionResponse> findPublishedByUserId(@Param("userId") Long userId, Pageable pageable);

  @Query("""
    SELECT new org.oplearn.project.dto.response.PoemCompositionResponse(
        pc.id,
        pc.userId,
        u.username,
        pc.content,
        pc.penName,
        pc.title,
        pc.genreId,
        g.name,
        pc.status
        )
    FROM PoemComposition pc
    LEFT JOIN User u ON pc.userId = u.id
    LEFT JOIN Genre g ON pc.genreId = g.id
    WHERE pc.isDeleted = false
    AND pc.status = 'PUBLISHED'
    """)
  List<PoemCompositionResponse> findLastest(Pageable pageable);

  interface PoemCompositionRow {
    Long getId();

    Long getUserId();

    String getUsername();

    String getContent();

    String getPenName();

    String getTitle();

    Long getGenreId();

    String getGenreName();

    PoemCompositionStatus getStatus();
  }

  @Query(value = """
        SELECT
          pc.id AS "id",
          pc.user_id AS "userId",
          u.username AS "username",
          pc.content AS "content",
          pc.pen_name AS "penName",
          pc.title AS "title",
          pc.genre_id AS "genreId",
          g.name AS "genreName",
          pc.status AS "status"
        FROM poem_compositions pc
        LEFT JOIN users u ON u.id = pc.user_id
        LEFT JOIN genres g ON g.id = pc.genre_id
        WHERE pc.is_deleted = false
          AND pc.status = 'PUBLISHED'
          AND (CAST(:genreId AS bigint) IS NULL OR pc.genre_id = CAST(:genreId AS bigint))
          AND (
              CAST(:keyword AS TEXT) IS NULL
              OR TRIM(CAST(:keyword AS TEXT)) = ''
              OR pc.search_vec @@ websearch_to_tsquery('simple', f_unaccent(CAST(:keyword AS TEXT)))
              OR f_unaccent(lower(pc.title)) LIKE '%' || f_unaccent(lower(CAST(:keyword AS TEXT))) || '%'
          )
        ORDER BY
            (CAST(:keyword AS text) IS NOT NULL AND f_unaccent(lower(pc.title)) = f_unaccent(lower(CAST(:keyword AS text)))) DESC,
            (CAST(:keyword AS text) IS NOT NULL AND f_unaccent(lower(pc.title)) LIKE f_unaccent(lower(CAST(:keyword AS text))) || '%') DESC,
            (CAST(:keyword AS text) IS NOT NULL AND f_unaccent(lower(pc.title)) LIKE '%' || f_unaccent(lower(CAST(:keyword AS text))) || '%') DESC,
            (CAST(:keyword AS text) IS NOT NULL AND pc.search_vec @@ phraseto_tsquery('simple', f_unaccent(CAST(:keyword AS text)))) DESC,
            ts_rank(pc.search_vec, websearch_to_tsquery('simple', f_unaccent(COALESCE(CAST(:keyword AS text), '')))) DESC,
            lower(pc.title), pc.id
        """,
    countQuery = """
          SELECT count(*)
          FROM poem_compositions pc
          WHERE pc.is_deleted = false
            AND (CAST(:genreId AS bigint) IS NULL OR pc.genre_id = CAST(:genreId AS bigint))
            AND (
                CAST(:keyword AS TEXT) IS NULL
                OR TRIM(CAST(:keyword AS TEXT)) = ''
                OR pc.search_vec @@ websearch_to_tsquery('simple', f_unaccent(CAST(:keyword AS TEXT)))
                OR f_unaccent(lower(pc.title)) LIKE '%' || f_unaccent(lower(CAST(:keyword AS TEXT))) || '%'
            )
          """,
    nativeQuery = true
  )
  Page<PoemCompositionRow> search(
    @Param("genreId") Long genreId,
    @Param("keyword") String keyword,
    Pageable pageable
  );

  Optional<PoemComposition> findByIdAndIsDeletedFalse(Long id);
}
