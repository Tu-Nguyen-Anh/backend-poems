package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PoemStatisticsResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.entity.PoemStatistics;
import org.oplearn.project.facade.StatisticServiceFacade;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.StatisticService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class StatisticServiceFacadeImpl implements StatisticServiceFacade {
  private final StatisticService statisticService;
  private final PoemService poemService;

  @Override
  public PoemStatisticsResponse create(Long poemId) {
    log.info("(facade) create");

    Poem poem = poemService.getAvailablePoemAndThrow(poemId);

    PoemStatistics savedStatistic = statisticService.create(poem.getId());

    return PoemStatisticsResponse.from(savedStatistic);
  }

  @Override
  public PoemStatisticsResponse update(Long poemId) {
    log.info("(facade) update statistic for poemId: {}", poemId);

    Poem poem = poemService.getAvailablePoemAndThrow(poemId);

    PoemStatistics updatedStatistic = statisticService.update(poem.getId());

    return PoemStatisticsResponse.from(updatedStatistic);
  }

  @Override
  public PoemStatisticsResponse detail(Long poemId) {
    log.info("(facade) detail statistic for poemId: {}", poemId);

    Poem poem = poemService.getAvailablePoemAndThrow(poemId);

    return statisticService.detail(poem.getId());
  }
}
