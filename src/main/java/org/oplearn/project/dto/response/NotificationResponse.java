package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.repository.NotificationRepository;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
  private Long id;
  private Long recipientId;
  private Long senderId;
  private String senderName;
  private String type;
  private String title;
  private String content;
  private String referenceType;
  private Long referenceId;
  private Instant createdAt;
  private Boolean isRead;

  public static NotificationResponse from(NotificationRepository.NotificationFeedProjection projection) {
    return NotificationResponse.builder()
      .id(projection.getId())
      .recipientId(projection.getRecipientId())
      .senderId(projection.getSenderId())
      .senderName(projection.getSenderName())
      .type(projection.getType())
      .title(projection.getTitle())
      .content(projection.getContent())
      .referenceType(projection.getReferenceType())
      .referenceId(projection.getReferenceId())
      .createdAt(projection.getCreatedAt())
      .isRead(projection.getIsRead())
      .build();
  }
}
