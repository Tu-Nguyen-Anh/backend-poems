package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.entity.NotificationRead;
import org.oplearn.project.repository.NotificationReadRepository;
import org.oplearn.project.service.NotificationReadService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationReadServiceImpl implements NotificationReadService {

  private final NotificationReadRepository repository;

  @Override
  @Transactional
  public boolean markAsRead(Long notificationId, Long userId) {
    log.info("(service) markAsRead notificationId: {}, userId: {}", notificationId, userId);

    boolean alreadyRead = repository.existsByUserIdAndNotificationIdAndIsDeletedFalse(userId, notificationId);
    if (!alreadyRead) {
      NotificationRead record = NotificationRead.builder()
          .notificationId(notificationId)
          .userId(userId)
          .readAt(Instant.now())
          .build();
      repository.save(record);
      return true;
    }
    return false;
  }

  @Override
  @Transactional
  public int markAllAsRead(Long userId) {
    log.info("(service) markAllAsRead userId: {}", userId);
    int affected = repository.markAllAsRead(userId);
    log.info("✅ [NotificationRead] Đã đánh dấu đã đọc {} thông báo cho user {}", affected, userId);
    return affected;
  }
}
