package org.oplearn.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;
import org.oplearn.project.enums.NotificationReferenceType;
import org.oplearn.project.enums.NotificationType;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Notification extends BaseAuditEntity {
  @Column(name = "recipient_id")
  private Long recipientId;

  @Column(name = "sender_id")
  private Long senderId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 50, nullable = false)
  private NotificationType type;

  @Column(name = "title")
  private String title;

  @Column(name = "content", columnDefinition = "TEXT")
  private String content;

  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", length = 50)
  private NotificationReferenceType referenceType;

  @Column(name = "reference_id")
  private Long referenceId;
}
