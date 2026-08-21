package org.oplearn.project.service;

import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.PoemHighlight;

import java.util.List;

public interface HighlightService {
  PoemHighlight create(PoemHighlight highlight);

  PoemHighlight getAvailableAndThrow(Long id);

  PoemHighlight updateNote(Long id, String note);

  void delete(Long id);

  List<HighlightResponse> listByUserAndPoem(Long userId, Long poemId);

  List<HighlightResponse> listByUserAndStoryChapter(Long userId, Long storyChapterId);

  PageResponse<HighlightWithPoemResponse> listByUser(Long userId, int size, int page);
}
