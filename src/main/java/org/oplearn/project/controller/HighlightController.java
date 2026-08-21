package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.HighlightNoteRequest;
import org.oplearn.project.dto.request.HighlightRequest;
import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.HighlightFacadeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.PAGE_DEFAULT;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/highlights")
public class HighlightController {
  private final HighlightFacadeService facade;

  @PostMapping
  public ResponseGeneral<HighlightResponse> create(@Valid @RequestBody HighlightRequest request) {
    log.info("(create) highlight poem id: {}", request.getPoemId());

    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<HighlightResponse> updateNote(
    @Valid @RequestBody HighlightNoteRequest request,
    @PathVariable Long id
  ) {
    log.info("(update note) highlight id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.updateNote(id, request.getNote()));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) highlight id: {}", id);

    facade.delete(id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/poem/{poemId}")
  public ResponseGeneral<List<HighlightResponse>> listByPoem(@PathVariable Long poemId) {
    log.info("(list) highlights for poem id: {}", poemId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.listByPoem(poemId));
  }

  @GetMapping("/story-chapter/{storyChapterId}")
  public ResponseGeneral<List<HighlightResponse>> listByStoryChapter(@PathVariable Long storyChapterId) {
    log.info("(list) highlights for story chapter id: {}", storyChapterId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.listByStoryChapter(storyChapterId));
  }

  @GetMapping
  public ResponseGeneral<PageResponse<HighlightWithPoemResponse>> myHighlights(
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) my highlights");

    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.myHighlights(size, page));
  }
}
