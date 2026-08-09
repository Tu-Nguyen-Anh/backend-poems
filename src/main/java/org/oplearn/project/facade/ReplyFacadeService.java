package org.oplearn.project.facade;

import org.oplearn.project.dto.request.ReplyRequest;
import org.oplearn.project.dto.response.CommentWithRepliesResponse;
import org.oplearn.project.dto.response.ReplyResponse;

public interface ReplyFacadeService {
  ReplyResponse create(ReplyRequest request);

  ReplyResponse update(ReplyRequest request, Long id);

  void delete(Long id);

  CommentWithRepliesResponse getReplyByCommentId(Long commentId , int size, int page);
}
