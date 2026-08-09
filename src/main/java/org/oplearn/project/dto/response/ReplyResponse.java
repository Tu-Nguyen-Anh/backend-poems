package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Reply;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReplyResponse {
  Long id;
  String content;
  Long commentId;
  Long userId;
  Instant createdAt;
  String username;
  String contentComment;

  public static ReplyResponse from(Reply reply, String username , String contentComment) {
    return new ReplyResponse(
      reply.getId(),
      reply.getContent().strip(),
      reply.getCommentId(),
      reply.getUserId(),
      reply.getCreatedAt(),
      username,
      contentComment
    );
  }
}
