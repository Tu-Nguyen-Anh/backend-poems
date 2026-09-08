package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.AuthorRequest;
import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.service.AuthorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/authors")
public class AuthorController {
  private final AuthorService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<AuthorResponse> create(@Valid @RequestBody AuthorRequest request) {
    log.info("create author");

    return ResponseGeneral.ofCreated(CREATED_MESSAGE, service.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<AuthorResponse> update(
    @Valid @RequestBody AuthorRequest request,
    @PathVariable Long id
  ) {
    log.info("update author");

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.update(request, id));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<AuthorResponse> detail(@PathVariable Long id) {
    log.info("detail author");

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("delete author");

    service.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping
  public ResponseGeneral<PageResponse<AuthorResponse>> list(
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = "type", required = false) String type,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page,
    @RequestParam(name = PARAM_ALL, defaultValue = IS_ALL_DEFAULT) boolean isAll
  ) {
    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);
    log.debug("(list) keyword: {}, type: {}, size: {}, page: {}, isAll: {}", keyword, type, size, page, isAll);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(keyword, type, size, page, isAll));
  }

  @GetMapping("/top")
  public ResponseGeneral<PageResponse<AuthorResponse>> listTopByPoemCount(
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);
    log.info("(listTopByPoemCount) size: {}, page: {}", size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listTopByPoemCount(size, page));
  }

  @GetMapping("/featured")
  public ResponseGeneral<java.util.List<AuthorResponse>> featured() {
    log.info("(featured) list pinned featured authors");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.featured());
  }

  @GetMapping("/{authorId}/poems")
  public ResponseGeneral<PageResponse<PoemResponse>> listPoemByAuthorId(
    @PathVariable Long authorId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);
    log.info("(list) authorId: {}, size: {}, page: {}", authorId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listPoemByAuthorId(authorId, size, page));
  }
}
