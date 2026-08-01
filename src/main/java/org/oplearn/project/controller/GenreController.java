package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.GenreRequest;
import org.oplearn.project.dto.response.*;
import org.oplearn.project.service.GenreService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_ALL;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_PAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.PARAM_SIZE;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/genres")
public class GenreController {
  private final GenreService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<GenreResponse> create(@Valid @RequestBody GenreRequest request) {
    log.info("(create) request: {}", request);
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, service.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<GenreResponse> update(
        @Valid @RequestBody GenreRequest request,
        @PathVariable Long id
  ) {
    log.info("(update) id: {}, request: {}", id, request);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.update(request, id));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<GenreResponse> detail(@PathVariable Long id) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.detail(id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) id: {}", id);
    service.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping
  public ResponseGeneral<PageResponse<GenreResponse>> list(
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page,
    @RequestParam(name = PARAM_ALL, defaultValue = IS_ALL_DEFAULT) boolean isAll
  ) {
    log.info("(list) keyword: {}, size: {}, page: {}, isAll: {}", keyword, size, page, isAll);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(keyword, size, page, isAll));
  }

  @GetMapping("/{genreId}/poems")
  public ResponseGeneral<PageResponse<PoemResponse>> listPoemByGenreId(
    @PathVariable Long genreId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) genreId: {}, size: {}, page: {}", genreId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listPoemByGenreId(genreId, size, page));
  }
}
