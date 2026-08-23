package org.oplearn.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PoemRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.PoemFacadeService;
import org.oplearn.project.service.PoemService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/poems")
public class PoemController {
  private final PoemService service;
  private final PoemFacadeService facade;

  @GetMapping
  public ResponseGeneral<PageResponse<PoemResponse>> list(
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = "genreId", required = false) Long genreId,
    @RequestParam(name = "era", required = false) String era,
    @RequestParam(name = "language", required = false) String language,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(list) keyword: {}, genreId: {}, era: {}, language: {}, size: {}, page: {}", keyword, genreId, era, language, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.list(keyword, genreId, era, language, size, page));
  }

  @GetMapping("/eras")
  public ResponseGeneral<java.util.List<String>> listEras() {
    log.info("(list eras)");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listEras());
  }

  @GetMapping("/languages")
  public ResponseGeneral<java.util.List<String>> listLanguages() {
    log.info("(list languages)");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listLanguages());
  }

  /**
   * Nhánh con của cây duyệt phân cấp (Ngôn ngữ → Thời kỳ → Thể thơ → Tác giả).
   */
  @GetMapping("/facets")
  public ResponseGeneral<java.util.List<org.oplearn.project.dto.response.FacetItemResponse>> facets(
    @RequestParam(name = "language", required = false) String language,
    @RequestParam(name = "era", required = false) String era,
    @RequestParam(name = "genreId", required = false) Long genreId
  ) {
    log.info("(facets) language: {}, era: {}, genreId: {}", language, era, genreId);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.facets(language, era, genreId));
  }

  /**
   * Danh sách bài ở cấp lá theo đường dẫn duyệt (lọc theo ngôn ngữ/thời kỳ/thể thơ/tác giả).
   */
  @GetMapping("/browse")
  public ResponseGeneral<PageResponse<PoemResponse>> browse(
    @RequestParam(name = "language", required = false) String language,
    @RequestParam(name = "era", required = false) String era,
    @RequestParam(name = "genreId", required = false) Long genreId,
    @RequestParam(name = "authorId", required = false) Long authorId,
    @RequestParam(name = PARAM_KEYWORD, required = false) String keyword,
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(browse) language: {}, era: {}, genreId: {}, authorId: {}, keyword: {}, size: {}, page: {}", language, era, genreId, authorId, keyword, size, page);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.browse(language, era, genreId, authorId, keyword, size, page));
  }

  @GetMapping("/{id}")
  public ResponseGeneral<PoemResponse> detail(@PathVariable Long id, HttpServletRequest request) {
    log.info("(detail) id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(id, request));
  }

  @PostMapping("/{id}/share")
  public ResponseGeneral<Void> share(@PathVariable Long id) {
    log.info("(share) poem id: {}", id);
    facade.share(id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<PoemResponse> create(@RequestBody PoemRequest request) {
    log.info("(create) poem");

    return ResponseGeneral.ofSuccess(CREATED_MESSAGE, facade.create(request));
  }

  @PutMapping("/{id}")
  public ResponseGeneral<PoemResponse> update(@RequestBody PoemRequest request, @PathVariable Long id) {
    log.info("(update) poem id: {}", id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(request, id));
  }

  @DeleteMapping("/{id}")
  public ResponseGeneral<Void> delete(@PathVariable Long id) {
    log.info("(delete) poem id: {}", id);

    service.delete(id);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE);
  }

  @GetMapping("/latest")
  public ResponseGeneral<PageResponse<PoemResponse>> listLatest(
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    size = Math.min(size, MAX_PAGE_SIZE);
    log.info("(list latest) poem with size: {}, page: {}", size, page);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.listPoemLatest(size, page));
  }

  @GetMapping("/random")
  public ResponseGeneral<PageResponse<PoemResponse>> listRandom(
    @RequestParam(name = "authorIds", required = false) @Size(max = 3, message = "author.max_selection_3") List<Long> authorIds,
    @RequestParam(name = "genreIds", required = false) @Size(max = 3, message = "genre.max_selection_3") List<Long> genreIds,
    @RequestParam(name = "eras", required = false) @Size(max = 3, message = "era.max_selection_3") List<String> eras
  ) {
    log.info("(list random) poem");

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.randomPersonalized(authorIds, genreIds, eras));
  }

  @GetMapping("/stats")
  public ResponseGeneral<org.oplearn.project.dto.response.StatsResponse> stats() {
    log.info("(stats) poem library overview");

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, service.getStats());
  }
}
