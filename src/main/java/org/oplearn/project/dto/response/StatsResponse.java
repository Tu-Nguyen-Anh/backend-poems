package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Thống kê tổng quan cho trang chủ. Phân loại bài theo: chữ Hán (language='Hán'),
 *  tiếng Việt (không phải Hán + tác giả Việt Nam), nước ngoài khác (còn lại). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StatsResponse {
  private long totalPoems;
  private long totalAuthors;
  private long totalCountries;
  private long vietCount;
  private long hanCount;
  private long foreignCount;
}
