package org.oplearn.project.service;

import org.oplearn.project.entity.*;
import org.oplearn.project.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
  Notification getAvailableNotificationAndThrow(Long id);

  long getUnreadCount(Long userId);

  Page<NotificationRepository.NotificationFeedProjection> getFeed(Long userId, Pageable pageable);

  void createAndSendReplyNotification(Reply reply, Comment comment, User sender);

  void createAndSendCreatePoemNotification(Poem poem , User sender);

  void createAndUpdatePoemNotification(Poem poem, User sender);
}
