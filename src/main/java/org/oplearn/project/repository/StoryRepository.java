package org.oplearn.project.repository;

import org.oplearn.project.dto.response.StoryCollectionResponse;
import org.oplearn.project.entity.Story;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoryRepository extends JpaRepository<Story, Long> {

  Optional<Story> findByIdAndIsDeletedFalse(Long id);

  /** Duyệt danh sách (không từ khoá) — lọc theo thể loại (collection) và/hoặc tác giả. */
  @Query(value = """
        SELECT s.id AS "id", s.title AS "title", s.author AS "author",
               s.author_url AS "authorUrl", s.author_id AS "authorId", s.year AS "year", s.genre AS "genre",
               s.category AS "category", s.collection AS "collection", s.source AS "source",
               s.type AS "type", s.chapter_count AS "chapterCount",
               s.char_count AS "charCount", s.word_count AS "wordCount"
        FROM stories s
        WHERE s.is_deleted = false
          AND (CAST(:collection AS text) IS NULL OR s.collection = CAST(:collection AS text))
          AND (CAST(:authorId AS bigint) IS NULL OR s.author_id = CAST(:authorId AS bigint))
        ORDER BY s.title, s.id
    """,
    countQuery = """
          SELECT count(*) FROM stories s
          WHERE s.is_deleted = false
            AND (CAST(:collection AS text) IS NULL OR s.collection = CAST(:collection AS text))
            AND (CAST(:authorId AS bigint) IS NULL OR s.author_id = CAST(:authorId AS bigint))
      """,
    nativeQuery = true)
  Page<StoryRow> browse(@Param("collection") String collection,
                        @Param("authorId") Long authorId,
                        Pageable pageable);

  long countByAuthorIdAndIsDeletedFalse(Long authorId);

  /**
   * Tìm kiếm full-text không dấu (tiêu đề + tác giả qua search_vec) hoặc tiêu đề
   * chứa cụm từ. Xếp hạng: tiêu đề khớp nguyên → bắt đầu bằng → chứa → ts_rank.
   */
  @Query(value = """
        SELECT s.id AS "id", s.title AS "title", s.author AS "author",
               s.author_url AS "authorUrl", s.author_id AS "authorId", s.year AS "year", s.genre AS "genre",
               s.category AS "category", s.collection AS "collection", s.source AS "source",
               s.type AS "type", s.chapter_count AS "chapterCount",
               s.char_count AS "charCount", s.word_count AS "wordCount"
        FROM stories s
        WHERE s.is_deleted = false
          AND (CAST(:collection AS text) IS NULL OR s.collection = CAST(:collection AS text))
          AND (CAST(:authorId AS bigint) IS NULL OR s.author_id = CAST(:authorId AS bigint))
          AND f_unaccent(lower(s.title)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
        ORDER BY
          (f_unaccent(lower(s.title)) = f_unaccent(lower(:keyword))) DESC,
          (f_unaccent(lower(s.title)) LIKE f_unaccent(lower(:keyword)) || '%') DESC,
          s.title,
          s.id
    """,
    countQuery = """
          SELECT count(*) FROM stories s
          WHERE s.is_deleted = false
            AND (CAST(:collection AS text) IS NULL OR s.collection = CAST(:collection AS text))
            AND (CAST(:authorId AS bigint) IS NULL OR s.author_id = CAST(:authorId AS bigint))
            AND f_unaccent(lower(s.title)) LIKE '%' || f_unaccent(lower(:keyword)) || '%'
      """,
    nativeQuery = true)
  Page<StoryRow> search(@Param("keyword") String keyword,
                        @Param("collection") String collection,
                        @Param("authorId") Long authorId,
                        Pageable pageable);

  /** Danh sách thể loại + số tác phẩm (cho dropdown lọc). */
  @Query(value = """
        SELECT new org.oplearn.project.dto.response.StoryCollectionResponse(s.collection, count(s))
        FROM Story s
        WHERE s.isDeleted = false AND s.collection IS NOT NULL
        GROUP BY s.collection
        ORDER BY count(s) DESC
    """)
  List<StoryCollectionResponse> collections();

  /** Projection native — alias khớp tên getter. */
  interface StoryRow {
    Long getId();

    String getTitle();

    String getAuthor();

    String getAuthorUrl();

    Long getAuthorId();

    Integer getYear();

    String getGenre();

    String getCategory();

    String getCollection();

    String getSource();

    String getType();

    Integer getChapterCount();

    Integer getCharCount();

    Integer getWordCount();
  }
}
