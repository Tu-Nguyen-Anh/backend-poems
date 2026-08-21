package org.oplearn.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.oplearn.project.entity.base.BaseAuditEntity;
import org.oplearn.project.enums.PoemCompositionStatus;

@Entity
@Table(name = "poem_compositions")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = "content")
public class PoemComposition extends BaseAuditEntity {
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "title", nullable = false)
  private String title;

  @Column(name = "content", nullable = false, columnDefinition = "TEXT")
  private String content;

  @Column(name = "pen_name")
  private String penName;

  @Column(name = "genre_id")
  private Long genreId;

  @Column(columnDefinition = "poem_composition_status_enum")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  private PoemCompositionStatus status;
}
