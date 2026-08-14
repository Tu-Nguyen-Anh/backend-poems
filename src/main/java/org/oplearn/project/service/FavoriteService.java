package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;

public interface FavoriteService {
  boolean isFavorited(Long userId, Long poemId);

  void add(Long userId, Long poemId);

  void remove(Long userId, Long poemId);

  PageResponse<PoemResponse> listByUser(Long userId, int size, int page);
}
