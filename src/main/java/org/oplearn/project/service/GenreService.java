package org.oplearn.project.service;

import org.oplearn.project.dto.request.GenreRequest;
import org.oplearn.project.dto.response.GenreResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;

public interface GenreService {
  GenreResponse create(GenreRequest request);

  GenreResponse update(GenreRequest request, Long id);

  void delete(Long id);

  GenreResponse detail(Long id);

  PageResponse<GenreResponse> list(String keyword, int size, int page, boolean isAll);

  PageResponse<PoemResponse> listPoemByGenreId(Long id, int size, int page);
}
