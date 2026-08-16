package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Mục lục chương (không kèm nội dung) — dùng ở trang chi tiết truyện. */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryChapterMetaResponse {
  private Integer seq;
  private String title;
  private Integer wordCount;
  private Integer charCount;
}
