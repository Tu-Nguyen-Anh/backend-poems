package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Poem;

public interface PoemService {
  PageResponse<PoemResponse> list(String keyword, Long genreId, int size, int page);

  PoemResponse detail(Long id);

  Poem update(Long id, Poem poem);

  void delete(Long id);

  Poem create(Poem poem);

  PageResponse<PoemResponse> listPoemLatest(int size, int page);

  PageResponse<PoemResponse> random();

  Poem getAvailablePoemAndThrow(Long id);
}
