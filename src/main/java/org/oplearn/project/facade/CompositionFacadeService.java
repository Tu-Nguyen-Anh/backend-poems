package org.oplearn.project.facade;

import org.oplearn.project.dto.request.PoemCompositionRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemCompositionResponse;

public interface CompositionFacadeService {
  PoemCompositionResponse create(PoemCompositionRequest request);

  PoemCompositionResponse update(PoemCompositionRequest request, Long id);

  void delete(Long id);

  PoemCompositionResponse detail(Long id);

  PageResponse<PoemCompositionResponse> listByUserId(Long userId, int size, int page);
}
