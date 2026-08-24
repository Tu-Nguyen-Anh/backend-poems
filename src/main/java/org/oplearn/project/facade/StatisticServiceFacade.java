package org.oplearn.project.facade;

import org.oplearn.project.dto.response.PoemStatisticsResponse;

public interface StatisticServiceFacade {
  PoemStatisticsResponse create(Long poemId);

  PoemStatisticsResponse update(Long poemId);

  PoemStatisticsResponse detail(Long poemId);
}
