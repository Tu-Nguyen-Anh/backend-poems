package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.PoemHighlight;
import org.oplearn.project.exception.HighlightNotFoundException;
import org.oplearn.project.repository.PoemHighlightRepository;
import org.oplearn.project.service.HighlightService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HighlightServiceImpl implements HighlightService {
  private final PoemHighlightRepository repository;

  public PoemHighlight create(PoemHighlight highlight) {
    return repository.save(highlight);
  }

  public PoemHighlight getAvailableAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(HighlightNotFoundException::new);
  }

  public PoemHighlight updateNote(Long id, String note) {
    PoemHighlight highlight = getAvailableAndThrow(id);
    highlight.setNote(note);
    return repository.save(highlight);
  }

  public void delete(Long id) {
    repository.softDeleteById(id);
  }

  public List<HighlightResponse> listByUserAndPoem(Long userId, Long poemId) {
    return repository.findByUserIdAndPoemId(userId, poemId);
  }

  public List<HighlightResponse> listByUserAndStoryChapter(Long userId, Long storyChapterId) {
    return repository.findByUserIdAndStoryChapterId(userId, storyChapterId);
  }

  public PageResponse<HighlightWithPoemResponse> listByUser(Long userId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);
    Page<HighlightWithPoemResponse> highlights = repository.findByUserId(userId, pageable);
    return PageResponse.of(highlights.getContent(), (int) highlights.getTotalElements());
  }
}
