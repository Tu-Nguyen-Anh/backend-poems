package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một tác phẩm văn xuôi ("Truyện ngắn"). Nội dung nằm ở {@link StoryChapter}. */
@Entity
@Table(name = "stories")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Story {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "source_url")
  private String sourceUrl;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "author")
  private String author;

  @Column(name = "author_url")
  private String authorUrl;

  @Column(name = "author_id")
  private Long authorId;

  @Column(name = "year")
  private Integer year;

  @Column(name = "genre")
  private String genre;

  @Column(name = "category")
  private String category;

  @Column(name = "collection")
  private String collection;

  @Column(name = "source")
  private String source;

  @Column(name = "type")
  private String type;

  @Column(name = "chapter_count")
  private Integer chapterCount;

  @Column(name = "char_count")
  private Integer charCount;

  @Column(name = "word_count")
  private Integer wordCount;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = Boolean.FALSE;
}
