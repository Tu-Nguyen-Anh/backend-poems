
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
            p.era,
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
            p.era,
            g.name,
            a.name
        )
        FROM Poem p
        LEFT JOIN Author a ON p.authorId = a.id
        LEFT JOIN Genre g ON p.genreId = g.id
        WHERE p.isDeleted = false
          AND (:genreId IS NULL OR p.genreId = :genreId)
          AND (:era IS NULL OR p.era = :era)
          AND (:language IS NULL OR p.language = :language)
    """)
  Page<PoemResponse> findAllByIsDeletedFalse(
    @Param("genreId") Long genreId,
    @Param("era") String era,
    @Param("language") String language,
    Pageable pageable);

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
               p.language AS "language", p.era AS "era", g.name AS "genreName", a.name AS "authorName"
        FROM poems p
        LEFT JOIN authors a ON a.id = p.author_id
        LEFT JOIN genres g ON g.id = p.genre_id
        WHERE p.is_deleted = false
          AND (CAST(:genreId AS bigint) IS NULL OR p.genre_id = CAST(:genreId AS bigint))
          AND (CAST(:era AS text) IS NULL OR p.era = CAST(:era AS text))
          AND (CAST(:language AS text) IS NULL OR p.language = CAST(:language AS text))
          AND (
            p.search_vec @@ websearch_to_tsquery('simple', f_unaccent(:keyword))
            OR f_unaccent(lower(p.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
            OR f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
          )
        ORDER BY
          (f_unaccent(lower(p.name)) = f_unaccent(lower(:keyword))) DESC,
          (f_unaccent(lower(p.name)) LIKE f_unaccent(lower(:keyword)) || '%') DESC,
          (f_unaccent(lower(p.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%') DESC,
          (f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%') DESC,
          ts_rank(p.search_vec, websearch_to_tsquery('simple', f_unaccent(:keyword))) DESC,
          p.id
    """,
    countQuery = """
        SELECT count(*)
        FROM poems p
        LEFT JOIN authors a ON a.id = p.author_id
        WHERE p.is_deleted = false
          AND (CAST(:genreId AS bigint) IS NULL OR p.genre_id = CAST(:genreId AS bigint))
          AND (CAST(:era AS text) IS NULL OR p.era = CAST(:era AS text))
          AND (CAST(:language AS text) IS NULL OR p.language = CAST(:language AS text))
          AND (
            p.search_vec @@ websearch_to_tsquery('simple', f_unaccent(:keyword))
            OR f_unaccent(lower(p.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
            OR f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
          )
    """,
    nativeQuery = true)
  Page<PoemSearchRow> search(
    @Param("keyword") String keyword,
    @Param("genreId") Long genreId,
    @Param("era") String era,
    @Param("language") String language,
    Pageable pageable);

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
    String getEra();
    String getGenreName();
    String getAuthorName();
  }

  /** Danh sách thời kỳ (era) đang có, xếp theo số bài giảm dần — cho bộ lọc. */
  @Query(value = """
      SELECT era FROM poems
      WHERE is_deleted = false AND era IS NOT NULL AND btrim(era) <> ''
      GROUP BY era ORDER BY count(*) DESC
    """, nativeQuery = true)
  java.util.List<String> findDistinctEras();

  /** Danh sách ngôn ngữ (Việt/Hán…) đang có, xếp theo số bài giảm dần. */
  @Query(value = """
      SELECT language FROM poems
      WHERE is_deleted = false AND language IS NOT NULL AND btrim(language) <> ''
      GROUP BY language ORDER BY count(*) DESC
    """, nativeQuery = true)
  java.util.List<String> findDistinctLanguages();


  /* ===================== DUYỆT PHÂN CẤP (facets) ===================== *
   * Cây: Ngôn ngữ → Thời kỳ (era) → Thể thơ (genre) → Tác giả → Bài thơ.
   * Mỗi truy vấn trả nhánh con của đường dẫn hiện tại KÈM số bài (count),
   * lazy-load: mở nhánh nào mới gọi truy vấn đó. Bài thiếu era/genre gom
   * vào rổ '(Chưa phân loại)' (era) hoặc genre_id = -1 để không mất bài. */

  /** Projection cho nhánh cây: id (genre/author) hoặc null, nhãn, số bài. */
  interface FacetCount {
    Long getId();
    String getLabel();
    Long getCount();
  }

  /** Cấp 1: các ngôn ngữ (Việt/Hán…) + số bài. */
  @Query(value = """
      SELECT NULL AS id, language AS label, count(*) AS count
      FROM poems
      WHERE is_deleted = false AND language IS NOT NULL AND btrim(language) <> ''
      GROUP BY language
      ORDER BY count(*) DESC, language
    """, nativeQuery = true)
  java.util.List<FacetCount> facetLanguages();

  /** Cấp 2: các thời kỳ trong 1 ngôn ngữ + số bài (null → '(Chưa phân loại)'). */
  @Query(value = """
      SELECT NULL AS id, COALESCE(NULLIF(btrim(era), ''), '(Chưa phân loại)') AS label, count(*) AS count
      FROM poems
      WHERE is_deleted = false AND language = :language
      GROUP BY COALESCE(NULLIF(btrim(era), ''), '(Chưa phân loại)')
      ORDER BY count(*) DESC
    """, nativeQuery = true)
  java.util.List<FacetCount> facetEras(@Param("language") String language);

  /** Cấp 3: các thể thơ trong ngôn ngữ + thời kỳ + số bài (null genre → id = -1). */
  @Query(value = """
      SELECT COALESCE(g.id, -1) AS id, COALESCE(g.name, '(Chưa phân loại)') AS label, count(*) AS count
      FROM poems p
      LEFT JOIN genres g ON g.id = p.genre_id
      WHERE p.is_deleted = false AND p.language = :language
        AND COALESCE(NULLIF(btrim(p.era), ''), '(Chưa phân loại)') = :era
      GROUP BY COALESCE(g.id, -1), COALESCE(g.name, '(Chưa phân loại)')
      ORDER BY count(*) DESC
    """, nativeQuery = true)
  java.util.List<FacetCount> facetGenres(@Param("language") String language, @Param("era") String era);

  /** Cấp 4: các tác giả trong ngôn ngữ + thời kỳ + thể thơ + số bài. */
  @Query(value = """
      SELECT a.id AS id, a.name AS label, count(*) AS count
      FROM poems p
      JOIN authors a ON a.id = p.author_id
      WHERE p.is_deleted = false AND p.language = :language
        AND COALESCE(NULLIF(btrim(p.era), ''), '(Chưa phân loại)') = :era
        AND COALESCE(p.genre_id, -1) = :genreId
      GROUP BY a.id, a.name
      ORDER BY count(*) DESC, a.name
    """, nativeQuery = true)
  java.util.List<FacetCount> facetAuthors(@Param("language") String language, @Param("era") String era, @Param("genreId") Long genreId);

  /** Cấp lá: danh sách bài theo đường dẫn (tham số nào null thì bỏ lọc chiều đó). */
  @Query(value = """
      SELECT p.id AS "id", p.name AS "name", p.description AS "description",
             p.year AS "year", p.content AS "content",
             p.transliteration AS "transliteration", p.translation AS "translation",
             p.language AS "language", p.era AS "era", g.name AS "genreName", a.name AS "authorName"
      FROM poems p
      LEFT JOIN authors a ON a.id = p.author_id
      LEFT JOIN genres g ON g.id = p.genre_id
      WHERE p.is_deleted = false
        AND (CAST(:language AS text) IS NULL OR p.language = CAST(:language AS text))
        AND (CAST(:era AS text) IS NULL OR COALESCE(NULLIF(btrim(p.era), ''), '(Chưa phân loại)') = CAST(:era AS text))
        AND (CAST(:genreId AS bigint) IS NULL OR COALESCE(p.genre_id, -1) = CAST(:genreId AS bigint))
        AND (CAST(:authorId AS bigint) IS NULL OR p.author_id = CAST(:authorId AS bigint))
      ORDER BY lower(p.name), p.id
    """,
    countQuery = """
      SELECT count(*)
      FROM poems p
      WHERE p.is_deleted = false
        AND (CAST(:language AS text) IS NULL OR p.language = CAST(:language AS text))
        AND (CAST(:era AS text) IS NULL OR COALESCE(NULLIF(btrim(p.era), ''), '(Chưa phân loại)') = CAST(:era AS text))
        AND (CAST(:genreId AS bigint) IS NULL OR COALESCE(p.genre_id, -1) = CAST(:genreId AS bigint))
        AND (CAST(:authorId AS bigint) IS NULL OR p.author_id = CAST(:authorId AS bigint))
    """,
    nativeQuery = true)
  Page<PoemSearchRow> browse(
    @Param("language") String language,
    @Param("era") String era,
    @Param("genreId") Long genreId,
    @Param("authorId") Long authorId,
    Pageable pageable);

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
            p.era,
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
            p.era,
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
