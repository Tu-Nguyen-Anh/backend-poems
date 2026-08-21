package org.oplearn.project.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PoemCompositionRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemCompositionResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.CompositionFacadeService;
import org.oplearn.project.service.CompositionService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/compositions")
public class CompositionController {
  private final CompositionFacadeService facade;
  private final CompositionService service;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<PoemCompositionResponse> create(@Valid @RequestBody PoemCompositionRequest request) {
    log.info("(create) poem composition");
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<PoemCompositionResponse> update(
    @Valid @RequestBody PoemCompositionRequest request,
    @PathVariable Long id
  ) {
    log.info("(update) poem composition id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(request, id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) poem composition id: {}", id);
    facade.delete(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/{id}")
  public ResponseGeneral<PoemCompositionResponse> detail(@PathVariable Long id) {
    log.info("(detail) poem composition id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id));
  }

  @GetMapping("/user/{userId}")
  public ResponseGeneral<PageResponse<PoemCompositionResponse>> listByUser(
    @PathVariable Long userId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(list by user) user id: {}, size: {}, page: {}", userId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.listByUserId(userId, size, page));
  }

  @GetMapping("/search")
  public ResponseGeneral<PageResponse<PoemCompositionResponse>> search(
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = "genreId", required = false) Long genreId,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(search) keyword: {}, genreId: {}, size: {}, page: {}", keyword, genreId, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.search(keyword, genreId, size, page));
  }

  @GetMapping("/latest")
  public ResponseGeneral<PageResponse<PoemCompositionResponse>> latest(
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(latest) size: {}, page: {}", size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.ListCompositionLastest(size, page));
  }

  @GetMapping("/random")
  public ResponseGeneral<PageResponse<PoemCompositionResponse>> random() {
    log.info("(random) poem compositions");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.random());
  }
}
