package org.oplearn.project.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.service.StatisticService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PoemViewSyncJob {
  private final StatisticService statisticService;

  /**
   * Chạy định kỳ mỗi 5 phút một lần (300.000 ms)
   * Đồng bộ toàn bộ lượt xem tích lũy từ Redis xuống PostgreSQL
   */
  @Scheduled(fixedRate = 300000)
  public void syncPoemViews() {
    log.info("Syncing poem views from Redis to PostgreSQL");
    statisticService.syncPendingViewsToDatabase();
  }
}
