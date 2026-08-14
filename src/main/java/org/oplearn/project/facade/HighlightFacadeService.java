package org.oplearn.project.facade;

import org.oplearn.project.dto.request.HighlightRequest;
import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.dto.response.PageResponse;

import java.util.List;

public interface HighlightFacadeService {
  HighlightResponse create(HighlightRequest request);

  HighlightResponse updateNote(Long id, String note);

  void delete(Long id);

  List<HighlightResponse> listByPoem(Long poemId);

  PageResponse<HighlightWithPoemResponse> myHighlights(int size, int page);
}
