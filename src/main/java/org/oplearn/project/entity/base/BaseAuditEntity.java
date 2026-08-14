package org.oplearn.project.entity.base;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class BaseAuditEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id", nullable = false, updatable = false)
  private Long id;

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = Boolean.FALSE;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;

  @PrePersist
  void prePersist() {
    if (Objects.isNull(isDeleted)) {
      isDeleted = Boolean.FALSE;
    }
  }
}
