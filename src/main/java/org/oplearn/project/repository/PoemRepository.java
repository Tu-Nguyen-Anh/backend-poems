
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
          AND (:genreId IS NULL OR p.genreId = :genreId)
    """)
  Page<PoemResponse> findAllByIsDeletedFalse(@Param("genreId") Long genreId, Pageable pageable);

  /**
   * Full-text search có xếp hạng (thay LIKE toàn cục): khớp qua tsvector
   * (tiêu đề trọng số A, nội dung C — index GIN idx_poems_search_vec) hoặc
   * tên bài/tên tác giả chứa cụm từ (index trigram). Thứ tự ưu tiên:
   * tiêu đề khớp nguyên → tiêu đề bắt đầu bằng → tiêu đề chứa → tác giả chứa
   * → độ liên quan nội dung (ts_rank).
   */
  @Query(value = """
        SELECT p.id AS "id", p.name AS "name", p.description AS "description",
               p.year AS "year", p.content AS "content",
               p.transliteration AS "transliteration", p.translation AS "translation",
               p.language AS "language", g.name AS "genreName", a.name AS "authorName"
        FROM poems p
        LEFT JOIN authors a ON a.id = p.author_id
        LEFT JOIN genres g ON g.id = p.genre_id
        WHERE p.is_deleted = false
          AND (CAST(:genreId AS bigint) IS NULL OR p.genre_id = CAST(:genreId AS bigint))
          AND (
            p.search_vec @@ websearch_to_tsquery('simple', :keyword)
            OR lower(p.name) LIKE '%' || lower(:keyword) || '%'
            OR lower(a.name) LIKE '%' || lower(:keyword) || '%'
          )
        ORDER BY
          (lower(p.name) = lower(:keyword)) DESC,
          (lower(p.name) LIKE lower(:keyword) || '%') DESC,
          (lower(p.name) LIKE '%' || lower(:keyword) || '%') DESC,
          (lower(a.name) LIKE '%' || lower(:keyword) || '%') DESC,
          ts_rank(p.search_vec, websearch_to_tsquery('simple', :keyword)) DESC,
          p.id
    """,
    countQuery = """
        SELECT count(*)
        FROM poems p
        LEFT JOIN authors a ON a.id = p.author_id
        WHERE p.is_deleted = false
          AND (CAST(:genreId AS bigint) IS NULL OR p.genre_id = CAST(:genreId AS bigint))
          AND (
            p.search_vec @@ websearch_to_tsquery('simple', :keyword)
            OR lower(p.name) LIKE '%' || lower(:keyword) || '%'
            OR lower(a.name) LIKE '%' || lower(:keyword) || '%'
          )
    """,
    nativeQuery = true)
  Page<PoemSearchRow> search(@Param("keyword") String keyword, @Param("genreId") Long genreId, Pageable pageable);

  /** Projection cho native search — alias trong query khớp tên getter. */
  interface PoemSearchRow {
    Long getId();
    String getName();
    String getDescription();
    Integer getYear();
    String getContent();
    String getTransliteration();
    String getTranslation();
    String getLanguage();
    String getGenreName();
    String getAuthorName();
  }


  @Modifying
  @Query("update Poem p set p.isDeleted = true where p.id = :id and p.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  long countByIsDeletedFalse();

  @Query("SELECT p.id FROM Poem p WHERE p.isDeleted = false ORDER BY p.id")
  java.util.List<Long> findIdsForSitemap(Pageable pageable);

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

  /** Bốc id ngẫu nhiên trước (chỉ sort cột id, không kéo content) — rẻ hơn
   *  nhiều so với ORDER BY random() trên cả dòng có nội dung bài thơ. */
  @Query(value = "SELECT id FROM poems WHERE is_deleted = false ORDER BY random() LIMIT :n", nativeQuery = true)
  java.util.List<Long> findRandomIds(@Param("n") int n);

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
        WHERE p.id IN :ids
    """)
  java.util.List<PoemResponse> findResponsesByIds(@Param("ids") java.util.List<Long> ids);

}
