package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.FeedbackRequest;
import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.entity.FeedbackStatus;
import org.oplearn.project.facade.FeedbackFacadeService;
import org.oplearn.project.service.FeedbackService;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@RestController
@Slf4j
@RequestMapping("/api/v1/feedbacks")
@RequiredArgsConstructor
public class FeedbackController {
  private final FeedbackFacadeService facade;
  private final FeedbackService service;

  @PostMapping
  public ResponseGeneral<FeedbackResponse> create(@Valid @RequestBody FeedbackRequest request) {
    log.info("(create) feedback");

    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<FeedbackResponse> update(@Valid @RequestBody FeedbackRequest request, @PathVariable Long id) {
    log.info("(update) feedback id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(request, id));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<FeedbackResponse> detail(@PathVariable Long id) {
    log.info("(detail) feedback id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) feedback id: {}", id);

    facade.delete(id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping
  public ResponseGeneral<PageResponse<FeedbackResponse>> list(
    @RequestParam(name = PARAM_STATUS, required = false) FeedbackStatus status,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page,
    @RequestParam(name = PARAM_ALL, defaultValue = IS_ALL_DEFAULT) boolean isAll
  ) {
    log.info("(list) status: {}, size: {}, page: {}, isAll: {}", status, size, page, isAll);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(status, size, page, isAll));
  }

  @GetMapping("/poem/{poemId}")
  public ResponseGeneral<PageResponse<FeedbackResponse>> listByPoemId(
    @PathVariable Long poemId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) poemId: {}, size: {}, page: {}", poemId, size, page);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getFeedbackByPoemId(poemId, size, page));
  }

  @GetMapping("/user/{userId}")
  public ResponseGeneral<PageResponse<FeedbackResponse>> listByUserId(
    @PathVariable Long userId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) userId: {}, size: {}, page: {}", userId, size, page);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getFeedbackByUserId(userId, size, page));
  }

  @PutMapping("/status/{id}")
  public ResponseGeneral<Void> updateStatus(@PathVariable Long id, @RequestParam FeedbackStatus status) {
    log.info("(update status) feedback id: {}, status: {}", id, status);

    service.updateStatus(id, status);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }
}
