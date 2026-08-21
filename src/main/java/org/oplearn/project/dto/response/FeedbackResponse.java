package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Feedback;
import org.oplearn.project.enums.FeedbackStatus;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FeedbackResponse {
  private Long id;
  private String content;
  private Long userId;
  private Long poemId;
  private String username;
  private Instant createdAt;
  private FeedbackStatus status;

  public static FeedbackResponse from(Feedback feedback, String username) {
    return new FeedbackResponse(
      feedback.getId(),
      feedback.getContent().strip(),
      feedback.getUserId(),
      feedback.getPoemId(),
      username,
      feedback.getCreatedAt(),
      feedback.getStatus()
    );
  }
}
