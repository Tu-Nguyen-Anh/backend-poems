package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import org.oplearn.project.entity.base.BaseAuditEntity;

@Entity
@Table(name = "poem_statistics")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class PoemStatistics extends BaseAuditEntity {

  @Column(name = "poem_id", nullable = false, unique = true)
  private Long poemId;

  @Builder.Default
  @Column(name = "view_count", nullable = false)
  private Long viewCount = 0L;

  @Builder.Default
  @Column(name = "favorite_count", nullable = false)
  private Long favoriteCount = 0L;

  @Builder.Default
  @Column(name = "share_count", nullable = false)
  private Long shareCount = 0L;

  @Builder.Default
  @Column(name = "comment_count", nullable = false)
  private Long commentCount = 0L;
}
