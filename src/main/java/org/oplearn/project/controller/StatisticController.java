package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PoemStatisticsResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.StatisticServiceFacade;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.CREATED_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/statistics")
public class StatisticController {
  private final StatisticServiceFacade facade;

  @PostMapping("/{poemId}")
  @ResponseStatus(HttpStatus.CREATED)
  public ResponseGeneral<PoemStatisticsResponse> create(@PathVariable Long poemId) {
    log.info("(create) statistic for poemId: {}", poemId);
    return ResponseGeneral.ofCreated(CREATED_MESSAGE, facade.create(poemId));
  }

  @PutMapping("/{poemId}")
  public ResponseGeneral<PoemStatisticsResponse> update(@PathVariable Long poemId) {
    log.info("(update) statistic for poemId: {}", poemId);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.update(poemId));
  }

  @GetMapping("/{poemId}")
  public ResponseGeneral<PoemStatisticsResponse> detail(@PathVariable Long poemId) {
    log.info("(detail) statistic for poemId: {}", poemId);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.detail(poemId));
  }
}
