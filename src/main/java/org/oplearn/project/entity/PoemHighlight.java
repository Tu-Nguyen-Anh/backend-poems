package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

@Entity
@Table(name = "poem_highlights")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PoemHighlight extends BaseAuditEntity {
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "poem_id", nullable = false)
  private Long poemId;

  @Column(name = "start_offset", nullable = false)
  private Integer startOffset;

  @Column(name = "end_offset", nullable = false)
  private Integer endOffset;

  @Column(name = "selected_text", nullable = false)
  private String selectedText;

  @Column(name = "note")
  private String note;
}
