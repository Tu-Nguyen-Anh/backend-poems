package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Poem;

import java.util.List;

public interface PoemService {
  PageResponse<PoemResponse> list(String keyword, Long genreId, String era, String language, int size, int page);

  java.util.List<String> listEras();

  java.util.List<String> listLanguages();

  java.util.List<org.oplearn.project.dto.response.FacetItemResponse> facets(String language, String era, Long genreId);

  PageResponse<PoemResponse> browse(String language, String era, Long genreId, Long authorId, String keyword, int size, int page);

  PoemResponse detail(Long id);

  Poem update(Long id, Poem poem);

  void delete(Long id);

  Poem create(Poem poem);

  PageResponse<PoemResponse> listPoemLatest(int size, int page);

  PageResponse<PoemResponse> randomPersonalized(List<Long> authorIds, List<Long> genreIds, List<String> eras);

  org.oplearn.project.dto.response.StatsResponse getStats();

  Poem getAvailablePoemAndThrow(Long id);
}
