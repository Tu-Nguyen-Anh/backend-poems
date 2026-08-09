package org.oplearn.project.service;

import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Feedback;
import org.oplearn.project.entity.FeedbackStatus;

public interface FeedbackService {
  Feedback create(Feedback feedback);

  Feedback update(Long id, String newContent);

  void delete(Long id);

  FeedbackResponse detail(Long id);

  PageResponse<FeedbackResponse> list(FeedbackStatus status, int size, int page, boolean isAll);

  PageResponse<FeedbackResponse> listByUserId(Long userId, int size, int page);

  PageResponse<FeedbackResponse> listByPoemId(Long poemId, int size, int page);

  void updateStatus(Long id, FeedbackStatus status);
}
