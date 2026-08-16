package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.dto.response.StoryChapterResponse;
import org.oplearn.project.dto.response.StoryCollectionResponse;
import org.oplearn.project.dto.response.StoryResponse;
import org.oplearn.project.service.StoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/stories")
public class StoryController {
  private final StoryService service;

  @GetMapping
  public ResponseGeneral<PageResponse<StoryResponse>> list(
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = "collection", required = false) String collection,
    @RequestParam(name = "authorId", required = false) Long authorId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(list) story keyword: {}, collection: {}, authorId: {}, size: {}, page: {}", keyword, collection, authorId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(keyword, collection, authorId, size, page));
  }

  @GetMapping("/collections")
  public ResponseGeneral<List<StoryCollectionResponse>> collections() {
    log.info("(list collections) story");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.collections());
  }

  @GetMapping("/{id}")
  public ResponseGeneral<StoryResponse> detail(@PathVariable Long id) {
    log.info("(detail) story id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @GetMapping("/{id}/chapters/{seq}")
  public ResponseGeneral<StoryChapterResponse> chapter(@PathVariable Long id, @PathVariable Integer seq) {
    log.info("(chapter) story id: {}, seq: {}", id, seq);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.chapter(id, seq));
  }
}
