package org.oplearn.project.dto.response;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentWithRepliesResponse {
  private Long commentId;
  private String contentComment;
  private CursorPageResponse<ReplyItemResponse> replies;
}
