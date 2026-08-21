package org.oplearn.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.oplearn.project.entity.base.BaseAuditEntity;
import org.oplearn.project.enums.FeedbackStatus;

@Entity
@Table(name = "feedbacks")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Feedback extends BaseAuditEntity {
  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "poem_id", nullable = false)
  private Long poemId;

  @Column(columnDefinition = "feedback_status_enum")
  @Enumerated(EnumType.STRING)
  @JdbcTypeCode(SqlTypes.NAMED_ENUM)
  @Builder.Default
  private FeedbackStatus status = FeedbackStatus.PENDING;
}
