package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

@Entity
@Table(name = "replies")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Reply extends BaseAuditEntity {
  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "comment_id", nullable = false)
  private Long commentId;

  @Column(name = "user_id", nullable = false)
  private Long userId;
}
