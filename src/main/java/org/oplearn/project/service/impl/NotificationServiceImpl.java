package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.entity.*;
import org.oplearn.project.enums.NotificationReferenceType;
import org.oplearn.project.enums.NotificationType;
import org.oplearn.project.exception.NotificationNotFoundException;
import org.oplearn.project.repository.NotificationRepository;
import org.oplearn.project.service.NotificationService;
import org.oplearn.project.websocket.EchoWebSocketHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository repository;
  private final EchoWebSocketHandler echoWebSocketHandler;

  @Override
  public Notification getAvailableNotificationAndThrow(Long id) {
    log.info("(service) getAvailableNotificationAndThrow id: {}", id);
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(NotificationNotFoundException::new);
  }

  @Override
  public long getUnreadCount(Long userId) {
    log.info("(service) getUnreadCount userId: {}", userId);
    return repository.countUnreadNotifications(userId);
  }

  @Override
  public Page<NotificationRepository.NotificationFeedProjection> getFeed(Long userId, Pageable pageable) {
    log.info("(service) getFeed userId: {}, pageable: {}", userId, pageable);
    return repository.findFeedByUserId(userId, pageable);
  }

  @Override
  @Transactional
  public void createAndSendReplyNotification(Reply reply, Comment comment, User sender) {
    Long recipientId = comment.getUserId();
    if (recipientId == null || recipientId.equals(sender.getId())) {
      return;
    }

    String preview = reply.getContent().length() > 60
        ? reply.getContent().substring(0, 60) + "..."
        : reply.getContent();

    Notification notification = Notification.builder()
        .recipientId(recipientId)
        .senderId(sender.getId())
        .type(NotificationType.COMMENT_REPLY)
        .title("Phản hồi mới")
        .content(sender.getUsername() + " đã trả lời bình luận của bạn: \"" + preview + "\"")
        .referenceType(comment.getPoemId() != null ? NotificationReferenceType.POEM : NotificationReferenceType.COMMENT)
        .referenceId(comment.getPoemId() != null ? comment.getPoemId() : comment.getId())
        .build();

    Notification saved = repository.save(notification);

    Map<String, Object> payload = Map.of(
        "notificationId", saved.getId(),
        "senderId", sender.getId(),
        "senderName", sender.getUsername(),
        "poemId", comment.getPoemId() != null ? comment.getPoemId() : -1,
        "commentId", comment.getId(),
        "replyId", reply.getId(),
        "message", saved.getContent()
    );

    echoWebSocketHandler.sendToUser(recipientId, "COMMENT_REPLY", payload);
  }

  @Override
  @Transactional
  public void createAndSendCreatePoemNotification(Poem poem, User sender) {
    String poemName = poem.getName() != null ? poem.getName() : "";
    Notification notification = Notification.builder()
      .recipientId(null)
      .senderId(sender.getId())
      .type(NotificationType.POEM_CREATED)
      .title("Bài thơ mới")
      .content("Bài thơ \"" + poemName + "\" vừa được tạo")
      .referenceType(NotificationReferenceType.POEM)
      .referenceId(poem.getId())
      .build();

    Notification saved = repository.save(notification);

    Map<String, Object> payload = Map.of(
      "notificationId", saved.getId(),
      "senderId", sender.getId(),
      "senderName", sender.getUsername(),
      "poemId", poem.getId(),
      "poemName", poemName,
      "message", saved.getContent()
    );

    echoWebSocketHandler.broadcast("POEM_CREATED", payload);
  }

  @Override
  @Transactional
  public void createAndUpdatePoemNotification(Poem poem, User sender) {
    String poemName = poem.getName() != null ? poem.getName() : "";
    Notification notification = Notification.builder()
      .recipientId(null)
      .senderId(sender.getId())
      .type(NotificationType.POEM_UPDATED)
      .title("Cập nhật bài thơ")
      .content("Bài thơ \"" + poemName + "\" vừa được sửa lại")
      .referenceType(NotificationReferenceType.POEM)
      .referenceId(poem.getId())
      .build();

    Notification saved = repository.save(notification);

    Map<String, Object> payload = Map.of(
      "notificationId", saved.getId(),
      "senderId", sender.getId(),
      "senderName", sender.getUsername(),
      "poemId", poem.getId(),
      "poemName", poemName,
      "message", saved.getContent()
    );

    echoWebSocketHandler.broadcast("POEM_UPDATED", payload);
  }
}
