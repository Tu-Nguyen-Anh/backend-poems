package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PoemStatisticsResponse;
import org.oplearn.project.entity.PoemStatistics;
import org.oplearn.project.exception.PoemStatisticNotFoundException;
import org.oplearn.project.exception.StatisticAlreadyExistedException;
import org.oplearn.project.repository.PoemStatisticsRepository;
import org.oplearn.project.repository.redis.StatisticRedisRepository;
import org.oplearn.project.service.StatisticService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {
  private final PoemStatisticsRepository repository;
  private final StatisticRedisRepository redisRepository;

  @Override
  public PoemStatistics create(Long poemId) {
    log.info("(service) create statistic for poemId: {}", poemId);

    if (repository.existsByPoemId(poemId)) {
      log.info("(service) statistic already exists for poemId: {}", poemId);
      throw new StatisticAlreadyExistedException();
    }

    PoemStatistics poemStatistics = PoemStatistics.builder()
      .poemId(poemId)
      .viewCount(0L)
      .favoriteCount(0L)
      .shareCount(0L)
      .commentCount(0L)
      .build();

    return repository.save(poemStatistics);
  }

  @Override
  public PoemStatistics update(Long poemId) {
    log.info("(service) update statistic for poemId: {}", poemId);

    PoemStatistics statistic = repository.findByPoemIdAndIsDeletedFalse(poemId)
      .orElseThrow(PoemStatisticNotFoundException::new);

    return repository.save(statistic);
  }

  @Override
  @Transactional
  public void increaseFavorite(Long poemId) {
    log.info("(service) increase favorite for poemId: {}", poemId);
    int updatedRows = repository.incrementFavoriteCount(poemId);
    if (updatedRows == 0) {
      repository.save(PoemStatistics.builder()
        .poemId(poemId)
        .viewCount(0L)
        .favoriteCount(1L)
        .shareCount(0L)
        .commentCount(0L)
        .build());
    }
  }

  @Override
  @Transactional
  public void decreaseFavorite(Long poemId) {
    if (poemId == null) return;
    log.info("(service) decrease favorite for poemId: {}", poemId);
    repository.decrementFavoriteCount(poemId);
  }

  @Override
  @Transactional
  public void increaseComment(Long poemId) {
    if (poemId == null) return;
    log.info("(service) increase comment for poemId: {}", poemId);
    int updatedRows = repository.incrementCommentCount(poemId);
    if (updatedRows == 0) {
      repository.save(PoemStatistics.builder()
        .poemId(poemId)
        .viewCount(0L)
        .favoriteCount(0L)
        .shareCount(0L)
        .commentCount(1L)
        .build());
    }
  }

  @Override
  @Transactional
  public void decreaseComment(Long poemId) {
    if (poemId == null) return;
    log.info("(service) decrease comment for poemId: {}", poemId);
    repository.decrementCommentCount(poemId);
  }

  @Override
  @Transactional
  public void increaseShare(Long poemId) {
    if (poemId == null) return;
    log.info("(service) increase share for poemId: {}", poemId);
    int updatedRows = repository.incrementShareCount(poemId);
    if (updatedRows == 0) {
      repository.save(PoemStatistics.builder()
        .poemId(poemId)
        .viewCount(0L)
        .favoriteCount(0L)
        .shareCount(1L)
        .commentCount(0L)
        .build());
    }
  }

  @Override
  @Async
  public void increaseView(Long poemId, String userIdentifier) {
    try {
      boolean isEligible = redisRepository.checkAndSetViewCooldown(poemId, userIdentifier);
      if (!isEligible) {
        log.debug("(increaseView) skip view for poemId: {} from user: {} (cooldown active)", poemId, userIdentifier);
        return;
      }

      redisRepository.incrementPendingView(poemId, 1L);
      log.info("(increaseView) recorded +1 pending view for poemId: {} from user: {}", poemId, userIdentifier);
    } catch (Exception ex) {
      log.error("(increaseView) error recording view for poemId: {}, err: {}", poemId, ex.getMessage());
    }
  }

  @Override
  @Transactional
  public void syncPendingViewsToDatabase() {
    Map<Long, Long> pendingViews = redisRepository.getAndClearPendingViews();
    if (pendingViews.isEmpty()) {
      return;
    }

    log.info("(syncPendingViews) syncing {} poems with pending views to database...", pendingViews.size());

    for (Map.Entry<Long, Long> entry : pendingViews.entrySet()) {
      Long poemId = entry.getKey();
      Long delta = entry.getValue();

      if (delta > 0) {
        int updatedRows = repository.addViewCount(poemId, delta);
        if (updatedRows == 0) {
          // Nếu bài thơ chưa có bản ghi thống kê trong DB -> tự tạo mới
          repository.save(PoemStatistics.builder()
            .poemId(poemId)
            .viewCount(delta)
            .favoriteCount(0L)
            .shareCount(0L)
            .commentCount(0L)
            .build());
        }
      }
    }

    log.info("(syncPendingViews) synced successfully!");
  }

  @Override
  public PoemStatisticsResponse detail(Long poemId) {
    return repository.getResponseByPoemId(poemId)
      .orElseThrow(PoemStatisticNotFoundException::new);

  }
}
