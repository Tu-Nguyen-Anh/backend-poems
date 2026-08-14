package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

@Entity
@Table(name = "favorites")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Favorite extends BaseAuditEntity {
  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "poem_id", nullable = false)
  private Long poemId;
}
