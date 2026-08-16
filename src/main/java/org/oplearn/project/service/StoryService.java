package org.oplearn.project.service;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.StoryChapterResponse;
import org.oplearn.project.dto.response.StoryCollectionResponse;
import org.oplearn.project.dto.response.StoryResponse;

import java.util.List;

public interface StoryService {
  PageResponse<StoryResponse> list(String keyword, String collection, Long authorId, int size, int page);

  List<StoryCollectionResponse> collections();

  StoryResponse detail(Long id);

  StoryChapterResponse chapter(Long storyId, Integer seq);
}
