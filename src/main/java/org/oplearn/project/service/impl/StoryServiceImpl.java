package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.StoryChapterMetaResponse;
import org.oplearn.project.dto.response.StoryChapterResponse;
import org.oplearn.project.dto.response.StoryCollectionResponse;
import org.oplearn.project.dto.response.StoryResponse;
import org.oplearn.project.entity.Story;
import org.oplearn.project.exception.StoryNotFoundException;
import org.oplearn.project.repository.StoryChapterRepository;
import org.oplearn.project.repository.StoryRepository;
import org.oplearn.project.service.StoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class StoryServiceImpl implements StoryService {
  private final StoryRepository repository;
  private final StoryChapterRepository chapterRepository;

  @Override
  public PageResponse<StoryResponse> list(String keyword, String collection, Long authorId, int size, int page) {
    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
    String coll = (collection == null || collection.isBlank()) ? null : collection.trim();
    Pageable pageable = PageRequest.of(Math.max(page, 0), Math.max(size, 1));

    Page<StoryRepository.StoryRow> rows = (kw == null)
      ? repository.browse(coll, authorId, pageable)
      : repository.search(kw, coll, authorId, pageable);

    List<StoryResponse> content = rows.getContent().stream().map(StoryServiceImpl::toResponse).toList();
    return PageResponse.of(content, (int) rows.getTotalElements());
  }

  @Override
  public List<StoryCollectionResponse> collections() {
    return repository.collections();
  }

  @Override
  public StoryResponse detail(Long id) {
    Story story = repository.findByIdAndIsDeletedFalse(id).orElseThrow(StoryNotFoundException::new);
    StoryResponse response = StoryResponse.from(story);
    List<StoryChapterMetaResponse> chapters = chapterRepository.findByStoryIdOrderBySeqAsc(id).stream()
      .map(c -> new StoryChapterMetaResponse(c.getSeq(), c.getTitle(), c.getWordCount(), c.getCharCount()))
      .toList();
    response.setChapters(chapters);
    return response;
  }

  @Override
  public StoryChapterResponse chapter(Long storyId, Integer seq) {
    // Đảm bảo truyện tồn tại (và chưa xoá) trước khi trả chương.
    repository.findByIdAndIsDeletedFalse(storyId).orElseThrow(StoryNotFoundException::new);
    return chapterRepository.findByStoryIdAndSeq(storyId, seq)
      .map(StoryChapterResponse::from)
      .orElseThrow(StoryNotFoundException::new);
  }

  private static StoryResponse toResponse(StoryRepository.StoryRow row) {
    StoryResponse r = new StoryResponse();
    r.setId(row.getId());
    r.setTitle(row.getTitle());
    r.setAuthor(row.getAuthor());
    r.setAuthorUrl(row.getAuthorUrl());
    r.setAuthorId(row.getAuthorId());
    r.setYear(row.getYear());
    r.setGenre(row.getGenre());
    r.setCategory(row.getCategory());
    r.setCollection(row.getCollection());
    r.setSource(row.getSource());
    r.setType(row.getType());
    r.setChapterCount(row.getChapterCount());
    r.setCharCount(row.getCharCount());
    r.setWordCount(row.getWordCount());
    return r;
  }
}
