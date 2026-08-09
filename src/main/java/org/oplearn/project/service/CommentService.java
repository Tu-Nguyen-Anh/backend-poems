package org.oplearn.project.service;

import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Comment;

public interface CommentService {
  Comment create(Comment comment);

  Comment update(String newContent, Long id);

  void delete(Long id);

  CommentResponse detail(Long id);

  PageResponse<CommentResponse> getCommentsByPoemId(Long poemId, int size, int page);

  PageResponse<CommentResponse> getCommentsByUserId(Long userId, int size, int page);

  Comment getAvailableCommentAndThrow(Long id);
}
