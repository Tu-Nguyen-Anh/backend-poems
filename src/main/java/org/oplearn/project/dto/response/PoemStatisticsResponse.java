package org.oplearn.project.dto.response;

import lombok.*;
import org.oplearn.project.entity.PoemStatistics;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PoemStatisticsResponse {
  private Long poemId;
  private Long viewCount;
  private Long favoriteCount;
  private Long shareCount;
  private Long commentCount;

  public static PoemStatisticsResponse from(PoemStatistics entity) {
    if (entity == null) {
      return null;
    }
    return PoemStatisticsResponse.builder()
      .poemId(entity.getPoemId())
      .viewCount(entity.getViewCount())
      .favoriteCount(entity.getFavoriteCount())
      .shareCount(entity.getShareCount())
      .commentCount(entity.getCommentCount())
      .build();
  }

  public static PoemStatisticsResponse ofDefault(Long poemId) {
    return PoemStatisticsResponse.builder()
      .poemId(poemId)
      .viewCount(0L)
      .favoriteCount(0L)
      .shareCount(0L)
      .commentCount(0L)
      .build();
  }
}
