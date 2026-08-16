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

  /** Danh sách tác giả (mặc định): xếp theo QUỐC GIA rồi SỐ TÁC PHẨM giảm dần.
   *  Ưu tiên Việt Nam (country_id=2) → Trung Quốc (country_id=3) → các nước khác;
   *  trong mỗi nhóm, tác giả nhiều bài đứng trước. Dùng native để GROUP BY + COUNT
   *  đếm số bài trực tiếp mà vẫn trả về entity đầy đủ (avatar/bio/country). */
  @Query(value = """
      SELECT a.* FROM authors a
      LEFT JOIN poems p ON p.author_id = a.id AND p.is_deleted = false
      WHERE a.is_deleted = false
      GROUP BY a.id
      ORDER BY
        CASE a.country_id WHEN 2 THEN 0 WHEN 3 THEN 1 ELSE 2 END,
        COUNT(p.id) DESC,
        a.name
    """,
    countQuery = """
      SELECT count(*) FROM authors a WHERE a.is_deleted = false
    """,
    nativeQuery = true)
  Page<Author> findAllOrderByCountryAndPoemCount(Pageable pageable);

  boolean existsByNameAndIsDeletedFalse(String name);

  /** Tìm tác giả BỎ DẤU (gõ "nguyen" ra "Nguyễn…"); index idx_authors_name_unaccent_trgm. */
  @Query(value = """
      SELECT * FROM authors a
      WHERE a.is_deleted = false
        AND f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
      ORDER BY
        (f_unaccent(lower(a.name)) = f_unaccent(lower(:keyword))) DESC,
        (f_unaccent(lower(a.name)) LIKE f_unaccent(lower(:keyword)) || '%') DESC,
        a.name
    """,
    countQuery = """
      SELECT count(*) FROM authors a
      WHERE a.is_deleted = false
        AND f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
    """,
    nativeQuery = true)
  Page<Author> search(@Param("keyword") String keyword, Pageable pageable);

  /**
   * Danh sách tác giả kèm SỐ BÀI THƠ + SỐ TÁC PHẨM VĂN, hỗ trợ tìm không dấu và
   * lọc theo loại: type='poem' (chỉ tác giả có thơ), 'story' (có văn), null (tất cả).
   * Xếp: quốc gia (VN→CN→khác), rồi số tác phẩm theo loại đang lọc (mặc định tổng) giảm dần.
   */
  @Query(value = """
      SELECT a.id AS "id", a.name AS "name", a.birth_year AS "birthYear",
             a.achievement AS "achievement", a.hometown AS "hometown",
             a.avatar_url AS "avatarUrl", a.avatar_local AS "avatarLocal",
             a.bio AS "bio", a.country AS "country", a.country_id AS "countryId",
             COUNT(DISTINCT p.id) AS "poemCount", COUNT(DISTINCT s.id) AS "storyCount"
      FROM authors a
      LEFT JOIN poems p ON p.author_id = a.id AND p.is_deleted = false
      LEFT JOIN stories s ON s.author_id = a.id AND s.is_deleted = false
      WHERE a.is_deleted = false
        AND (CAST(:keyword AS text) IS NULL
             OR f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%')
      GROUP BY a.id
      HAVING (CAST(:type AS text) IS NULL
              OR (:type = 'poem' AND COUNT(DISTINCT p.id) > 0)
              OR (:type = 'story' AND COUNT(DISTINCT s.id) > 0))
      ORDER BY
        CASE WHEN CAST(:keyword AS text) IS NOT NULL
                  AND f_unaccent(lower(a.name)) = f_unaccent(lower(:keyword)) THEN 0 ELSE 1 END,
        CASE a.country_id WHEN 2 THEN 0 WHEN 3 THEN 1 ELSE 2 END,
        (CASE WHEN :type = 'story' THEN COUNT(DISTINCT s.id)
              WHEN :type = 'poem' THEN COUNT(DISTINCT p.id)
              ELSE COUNT(DISTINCT p.id) + COUNT(DISTINCT s.id) END) DESC,
        a.name
    """,
    countQuery = """
      SELECT count(*) FROM (
        SELECT a.id
        FROM authors a
        LEFT JOIN poems p ON p.author_id = a.id AND p.is_deleted = false
        LEFT JOIN stories s ON s.author_id = a.id AND s.is_deleted = false
        WHERE a.is_deleted = false
          AND (CAST(:keyword AS text) IS NULL
               OR f_unaccent(lower(a.name)) LIKE '%' || f_unaccent(lower(:keyword)) || '%')
        GROUP BY a.id
        HAVING (CAST(:type AS text) IS NULL
                OR (:type = 'poem' AND COUNT(DISTINCT p.id) > 0)
                OR (:type = 'story' AND COUNT(DISTINCT s.id) > 0))
      ) t
    """,
    nativeQuery = true)
  Page<AuthorListRow> listWithCounts(@Param("keyword") String keyword,
                                     @Param("type") String type,
                                     Pageable pageable);

  /** Projection native — alias khớp tên getter. */
  interface AuthorListRow {
    Long getId();

    String getName();

    Integer getBirthYear();

    String getAchievement();

    String getHometown();

    String getAvatarUrl();

    String getAvatarLocal();

    String getBio();

    String getCountry();

    Integer getCountryId();

    Long getPoemCount();

    Long getStoryCount();
  }

  @Transactional
  @Modifying
  @Query("update Author a set a.isDeleted = true where a.id = :id and a.isDeleted = false")
  void softDeleteById(@Param("id") Long id);

  @Query("SELECT a.id FROM Author a WHERE a.isDeleted = false ORDER BY a.id")
  java.util.List<Long> findIdsForSitemap();

  @Query("""
    SELECT new org.oplearn.project.dto.response.AuthorResponse(
      a.id, a.name, a.birthYear, a.achievement, a.hometown, COUNT(p.id), a.avatarUrl, a.avatarLocal
    )
    FROM Author a
    LEFT JOIN Poem p ON p.authorId = a.id AND p.isDeleted = false
    WHERE a.isDeleted = false
    GROUP BY a.id, a.name, a.birthYear, a.achievement, a.hometown, a.avatarUrl, a.avatarLocal
    ORDER BY COUNT(p.id) DESC, a.id
    """)
  Page<org.oplearn.project.dto.response.AuthorResponse> findTopByPoemCount(Pageable pageable);

  /** Tác giả tiêu biểu ghim tay: khớp theo tên đã bỏ dấu + viết thường (bền vững
   *  khi re-import DB đổi id). Trả kèm số bài + avatar; service tự sắp lại đúng thứ tự. */
  @Query("""
    SELECT new org.oplearn.project.dto.response.AuthorResponse(
      a.id, a.name, a.birthYear, a.achievement, a.hometown, COUNT(p.id), a.avatarUrl, a.avatarLocal
    )
    FROM Author a
    LEFT JOIN Poem p ON p.authorId = a.id AND p.isDeleted = false
    WHERE a.isDeleted = false AND function('f_unaccent', lower(a.name)) IN :names
    GROUP BY a.id, a.name, a.birthYear, a.achievement, a.hometown, a.avatarUrl, a.avatarLocal
    """)
  java.util.List<org.oplearn.project.dto.response.AuthorResponse> findByUnaccentNames(
    @Param("names") java.util.List<String> names);

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
    LEFT JOIN Genre g ON p.genreId = g.id
    LEFT JOIN Author a ON p.authorId = a.id
    WHERE p.authorId = :authorId
    AND p.isDeleted = false
    AND g.isDeleted = false
    AND a.isDeleted = false
    """)
  Page<PoemResponse> findPoemByAuthorId(@Param("authorId") Long AuthorId, Pageable pageable);
}
