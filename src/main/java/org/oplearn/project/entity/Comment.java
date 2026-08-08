package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

@Entity
@Table(name = "comments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Comment extends BaseAuditEntity {
  @Column(name = "content", nullable = false)
  private String content;

  @Column(name = "poem_id", nullable = false)
  private Long poemId;

  @Column(name = "user_id", nullable = false)
  private Long userId;
}
