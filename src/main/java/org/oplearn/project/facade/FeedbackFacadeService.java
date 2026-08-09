package org.oplearn.project.facade;

import org.oplearn.project.dto.request.FeedbackRequest;
import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.dto.response.PageResponse;

public interface FeedbackFacadeService {
  FeedbackResponse create(FeedbackRequest request);

  FeedbackResponse update(FeedbackRequest request, Long id);

  void delete(Long id);

  PageResponse<FeedbackResponse> getFeedbackByUserId(Long userId, int size, int page);

  PageResponse<FeedbackResponse> getFeedbackByPoemId(Long poemId, int size, int page);
}
