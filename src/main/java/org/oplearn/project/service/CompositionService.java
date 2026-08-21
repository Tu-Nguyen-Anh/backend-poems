package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemCompositionResponse;
import org.oplearn.project.entity.PoemComposition;

public interface CompositionService {
  PoemComposition create(PoemComposition poemComposition);

  PoemComposition update(PoemComposition poemComposition, Long id);

  void delete(Long id);

  PoemCompositionResponse detail(Long id);

  PageResponse<PoemCompositionResponse> listByUserId(Long userId, boolean canViewAll, int size, int page);

  PageResponse<PoemCompositionResponse> search(String keyword, Long genreId, int size, int page);

  PageResponse<PoemCompositionResponse> random();

  PageResponse<PoemCompositionResponse> ListCompositionLastest(int size, int page);

  PoemComposition getAvailableCompositionAndThrow(Long id);
}
