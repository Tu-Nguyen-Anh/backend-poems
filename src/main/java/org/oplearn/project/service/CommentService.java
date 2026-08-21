package org.oplearn.project.service;

import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Comment;

public interface CommentService {
  Comment create(Comment comment);

  Comment update(String newContent, Long id);

  void delete(Long id);

  CommentResponse detail(Long id);

  CursorPageResponse<CommentResponse> getCommentsByPoemId(Long poemId, Long cursor, int size);

  CursorPageResponse<CommentResponse> getCommentsByPoemCompositionId(Long poemCompositionId, Long cursor, int size);

  CursorPageResponse<CommentResponse> getCommentsByUserId(Long userId, Long cursor, int size);

  Comment getAvailableCommentAndThrow(Long id);
}
