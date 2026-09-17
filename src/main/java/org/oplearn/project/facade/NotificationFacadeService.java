package org.oplearn.project.facade;

import org.oplearn.project.dto.response.NotificationResponse;
import org.oplearn.project.dto.response.PageResponse;

public interface NotificationFacadeService {
  long getUnreadCount();

  PageResponse<NotificationResponse> getFeed(int page, int size);

  boolean markAsRead(Long id);

  int markAllAsRead();
}
