package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.FavoriteFacadeService;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.*;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.PAGE_DEFAULT;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/favorites")
public class FavoriteController {
  private final FavoriteFacadeService facade;

  @PostMapping("/{poemId}")
  public ResponseGeneral<Boolean> add(@PathVariable Long poemId) {
    log.info("(add) favorite poem id: {}", poemId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.add(poemId));
  }

  @DeleteMapping("/{poemId}")
  public ResponseGeneral<Boolean> remove(@PathVariable Long poemId) {
    log.info("(remove) favorite poem id: {}", poemId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.remove(poemId));
  }

  @GetMapping("/{poemId}/status")
  public ResponseGeneral<Boolean> status(@PathVariable Long poemId) {
    log.info("(status) favorite poem id: {}", poemId);

    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.status(poemId));
  }

  @GetMapping
  public ResponseGeneral<PageResponse<PoemResponse>> myFavorites(
    @RequestParam(name = PARAM_SIZE, defaultValue = SIZE_DEFAULT) int size,
    @RequestParam(name = PARAM_PAGE, defaultValue = PAGE_DEFAULT) int page
  ) {
    log.info("(list) my favorites");

    size = Math.min(size, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_PAGE_SIZE);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.myFavorites(size, page));
  }
}
