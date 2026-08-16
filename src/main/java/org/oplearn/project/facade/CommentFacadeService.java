package org.oplearn.project.facade;

import org.oplearn.project.dto.request.CommentRequest;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.PageResponse;

public interface CommentFacadeService {
  CommentResponse create(CommentRequest request);

  CommentResponse update(CommentRequest request, Long id);

  CursorPageResponse<CommentResponse> getCommentsByUserId(Long userId, Long cursor, int size);

  void delete(Long id);
}
