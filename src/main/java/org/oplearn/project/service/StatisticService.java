package org.oplearn.project.service;

import org.oplearn.project.dto.response.PoemStatisticsResponse;
import org.oplearn.project.entity.PoemStatistics;

public interface StatisticService {
  PoemStatistics create(Long poemId);

  PoemStatistics update(Long poemId);

  void increaseFavorite(Long poemId);

  void decreaseFavorite(Long poemId);

  void increaseComment(Long poemId);

  void decreaseComment(Long poemId);

  void increaseShare(Long poemId);

  void increaseView(Long poemId, String userIdentifier);

  void syncPendingViewsToDatabase();

  PoemStatisticsResponse detail(Long poemId);
}
