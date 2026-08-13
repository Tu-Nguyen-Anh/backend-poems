package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oplearn.project.entity.base.BaseEntity;

@Entity
@Table(name = "poems")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "content")
public class Poem extends BaseEntity {
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "year")
  private Integer year;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "transliteration")
  private String transliteration;

  @Column(name = "translation")
  private String translation;

  @Column(name = "meaning")
  private String meaning;

  @Column(name = "language")
  private String language;

  @Column(name = "era")
  private String era;

  @Column(name = "genre_id")
  private Long genreId;

  @Column(name = "author_id")
  private Long authorId;
}

