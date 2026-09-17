package org.oplearn.project.repository;

import org.oplearn.project.entity.NotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public interface NotificationReadRepository extends JpaRepository<NotificationRead, Long> {
  boolean existsByUserIdAndNotificationIdAndIsDeletedFalse(Long userId, Long notificationId);

  Optional<NotificationRead> findByUserIdAndNotificationId(Long userId, Long notificationId);

  @Modifying
  @Transactional
  @Query(value = """
    INSERT INTO notification_reads (notification_id , user_id , read_at, is_deleted, created_at, updated_at)
    SELECT n.id , :userId , now() , false , now() , now()
    FROM notifications n
    WHERE (n.recipient_id = :userId OR n.recipient_id IS NULL)
    AND n.is_deleted = false
    AND NOT EXISTS (
                SELECT 1 FROM notification_reads nr
                WHERE nr.notification_id = n.id
                  AND nr.user_id = :userId
                  AND nr.is_deleted = false
              )
    ON CONFLICT (notification_id, user_id) DO NOTHING;
    """, nativeQuery = true)
  int markAllAsRead(@Param("userId") Long userId);
}
