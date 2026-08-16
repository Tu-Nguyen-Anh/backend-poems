package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.CommentRequest;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.CommentFacadeService;
import org.oplearn.project.service.CommentService;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/comments")
public class CommentController {
  private final CommentFacadeService facade;
  private final CommentService service;

  @PostMapping
  public ResponseGeneral<CommentResponse> create(@Valid @RequestBody CommentRequest request) {
    log.info("(create) comment");

    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<CommentResponse> update(@Valid @RequestBody CommentRequest request, @PathVariable Long id) {
    log.info("(update) comment id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(request, id));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<CommentResponse> detail(@PathVariable Long id) {
    log.info("(detail) comment id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) comment id: {}", id);

    facade.delete(id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/user/{userId}")
  public ResponseGeneral<CursorPageResponse<CommentResponse>> listByUser(
    @PathVariable Long userId,
    @RequestParam(name = PARAM_CURSOR, required = false) Long cursor,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size
  ) {
    log.info("(list by user) comment user id: {}", userId);

    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getCommentsByUserId(userId, cursor, size));
  }

  @GetMapping("/poem/{poemId}")
  public ResponseGeneral<CursorPageResponse<CommentResponse>> listByPoem(
    @PathVariable Long poemId,
    @RequestParam(name = PARAM_CURSOR, required = false) Long cursor,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size
  ) {
    log.info("(list by poem) comment poem id: {}, cursor: {}, size: {}", poemId, cursor, size);

    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.getCommentsByPoemId(poemId, cursor, size));
  }
}
