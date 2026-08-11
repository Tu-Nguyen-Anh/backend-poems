package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oplearn.project.entity.base.BaseEntity;

@Entity
@Table(name = "poem_translations")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "content")
public class PoemTranslation extends BaseEntity {
  @Column(name = "poem_id", nullable = false)
  private Long poemId;

  @Column(name = "translator")
  private String translator;

  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "sort_order")
  private Integer sortOrder;
}
