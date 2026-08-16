package org.oplearn.project.service;

import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Reply;

public interface ReplyService {
  Reply create(Reply reply);

  Reply update(Long id, String newContent);

  void delete(Long id);

  ReplyResponse detail(Long id);

  CursorPageResponse<ReplyItemResponse> getReplyByCommentId(Long commentId, Long cursor, int size);

  CursorPageResponse<ReplyResponse> getReplyByUserId(Long userId, Long cursor, int size);
}
