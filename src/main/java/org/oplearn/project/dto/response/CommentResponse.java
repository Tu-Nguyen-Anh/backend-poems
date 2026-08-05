package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Comment;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
  private Long id;
  private String content;
  private Long poemId;
  private Long userId;
  private String username;
  private Instant createdAt;

  public static CommentResponse from(Comment comment , String username) {
    return new CommentResponse(
      comment.getId(),
      comment.getContent().strip(),
      comment.getPoemId(),
      comment.getUserId(),
      username,
      comment.getCreatedAt()
    );
  }
}
