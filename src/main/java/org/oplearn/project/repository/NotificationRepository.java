package org.oplearn.project.repository;

import org.oplearn.project.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  Optional<Notification> findByIdAndIsDeletedFalse(Long id);

  @Query(value = """
    SELECT COUNT(*) FROM notifications n
    WHERE (n.recipient_id = :userId OR n.recipient_id IS NULL)
    AND n.is_deleted = false
    AND NOT EXISTS (
              SELECT 1 FROM notification_reads nr
              WHERE nr.notification_id = n.id
                AND nr.user_id = :userId
                AND nr.is_deleted = false
            )
    """ , nativeQuery = true)
  long countUnreadNotifications(@Param("userId") Long userId);

  @Query(value = """
    SELECT n.id, n.recipient_id AS recipientId, n.sender_id AS senderId,
           u.username AS senderName, n.type, n.title, n.content,
           n.reference_type AS referenceType, n.reference_id AS referenceId,
           n.created_at AS createdAt,
           (nr.id IS NOT NULL) AS isRead
    FROM notifications n
    LEFT JOIN users u ON u.id = n.sender_id
    LEFT JOIN notification_reads nr ON nr.notification_id = n.id
        AND nr.user_id = :userId
        AND nr.is_deleted = false
    WHERE (n.recipient_id = :userId OR n.recipient_id IS NULL)
    AND n.is_deleted = false
    ORDER BY n.id DESC
    """, countQuery = """
    SELECT COUNT(*) FROM notifications n
    WHERE (n.recipient_id = :userId OR n.recipient_id IS NULL)
    AND n.is_deleted = false
    """ , nativeQuery = true)
  Page<NotificationFeedProjection> findFeedByUserId(@Param("userId") Long userId, Pageable pageable);

  interface NotificationFeedProjection {
    Long getId();
    Long getRecipientId();
    Long getSenderId();
    String getSenderName();
    String getType();
    String getTitle();
    String getContent();
    String getReferenceType();
    Long getReferenceId();
    Instant getCreatedAt();
    Boolean getIsRead();
  }
}
