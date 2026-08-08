package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Reply;

public interface ReplyService {
  Reply create(Reply reply);

  Reply update(Long id, String newContent);

  void delete(Long id);

  ReplyResponse detail(Long id);

  PageResponse<ReplyItemResponse> getReplyByCommentId(Long commentId, int size, int page);
}
