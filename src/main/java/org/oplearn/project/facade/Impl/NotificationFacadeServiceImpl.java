package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.NotificationResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Notification;
import org.oplearn.project.entity.User;
import org.oplearn.project.facade.NotificationFacadeService;
import org.oplearn.project.repository.NotificationRepository;
import org.oplearn.project.service.NotificationReadService;
import org.oplearn.project.service.NotificationService;
import org.oplearn.project.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationFacadeServiceImpl implements NotificationFacadeService {

  private final NotificationService notificationService;
  private final NotificationReadService notificationReadService;
  private final UserService userService;

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.getUsernameOrThrow(authentication.getName());
  }

  @Override
  public long getUnreadCount() {
    User user = currentUser();
    log.info("(facade) getUnreadCount for user: {}", user.getUsername());
    return notificationService.getUnreadCount(user.getId());
  }

  @Override
  public PageResponse<NotificationResponse> getFeed(int page, int size) {
    User user = currentUser();
    log.info("(facade) getFeed for user: {}, page: {}, size: {}", user.getUsername(), page, size);

    Page<NotificationRepository.NotificationFeedProjection> projectionPage =
        notificationService.getFeed(user.getId(), PageRequest.of(page, size));

    List<NotificationResponse> content = projectionPage.getContent()
        .stream()
        .map(NotificationResponse::from)
        .toList();

    return PageResponse.of(content, (int) projectionPage.getTotalElements());
  }

  @Override
  public boolean markAsRead(Long id) {
    log.info("(facade) mark notification as read: id = {}", id);
    User user = currentUser();

    // 1. Kiểm tra thông báo có tồn tại hay không (ném NotificationNotFoundException nếu không có)
    Notification notification = notificationService.getAvailableNotificationAndThrow(id);

    // 2. Ghi nhận đã đọc thông qua NotificationReadService
    return notificationReadService.markAsRead(notification.getId(), user.getId());
  }

  @Override
  public int markAllAsRead() {
    User user = currentUser();
    log.info("(facade) mark all notifications as read for user: {}", user.getUsername());
    return notificationReadService.markAllAsRead(user.getId());
  }
}
