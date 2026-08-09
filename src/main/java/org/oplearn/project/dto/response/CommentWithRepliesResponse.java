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
  private PageResponse<ReplyItemResponse> replies;
}
