package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

import java.time.Instant;

@Entity
@Table(name = "notification_reads")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NotificationRead extends BaseAuditEntity {
  @Column(name = "notification_id", nullable = false)
  private Long notificationId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "read_at")
  private Instant readAt;
}
