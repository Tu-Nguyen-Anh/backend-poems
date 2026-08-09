package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ReplyRequest;
import org.oplearn.project.dto.response.CommentWithRepliesResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.ReplyFacadeService;
import org.oplearn.project.service.ReplyService;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_PAGE;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.PAGE_DEFAULT;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/replies")
public class ReplyController {
  private final ReplyFacadeService facade;
  private final ReplyService service;

  @PostMapping
  public ResponseGeneral<ReplyResponse> create(@Valid @RequestBody ReplyRequest request) {
    log.info("(create) reply");

    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<ReplyResponse> update(@Valid @RequestBody ReplyRequest request, @PathVariable Long id) {
    log.info("(update) reply id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(request, id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) reply id: {}", id);

    facade.delete(id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/{id}")
  public ResponseGeneral<ReplyResponse> detail(@PathVariable Long id) {
    log.info("(detail) reply id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @GetMapping("/comment/{commentId}")
  public ResponseGeneral<CommentWithRepliesResponse> getByCommentId(
    @PathVariable Long commentId,
    @RequestParam(name=PARAM_SIZE, defaultValue =SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list by comment id) comment id: {}", commentId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE , facade.getReplyByCommentId(commentId, size, page));
  }
}
