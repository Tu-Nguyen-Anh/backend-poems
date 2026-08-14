package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/** Highlight kèm thông tin bài thơ — dùng cho trang "Ghi chú của tôi". */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HighlightWithPoemResponse {
  private Long id;
  private Long poemId;
  private String poemName;
  private String authorName;
  private Integer startOffset;
  private Integer endOffset;
  private String selectedText;
  private String note;
  private Instant createdAt;
}
