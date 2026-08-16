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

/** Một chương của {@link Story}. Bài "single" lưu thành 1 chương (seq = 1). */
@Entity
@Table(name = "story_chapters")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StoryChapter {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "story_id", nullable = false)
  private Long storyId;

  @Column(name = "seq", nullable = false)
  private Integer seq;

  @Column(name = "title")
  private String title;

  @Column(name = "char_count")
  private Integer charCount;

  @Column(name = "word_count")
  private Integer wordCount;

  @Column(name = "content")
  private String content;
}
