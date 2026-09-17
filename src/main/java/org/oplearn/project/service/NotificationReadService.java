package org.oplearn.project.service;

public interface NotificationReadService {
  boolean markAsRead(Long notificationId, Long userId);

  int markAllAsRead(Long userId);
}
