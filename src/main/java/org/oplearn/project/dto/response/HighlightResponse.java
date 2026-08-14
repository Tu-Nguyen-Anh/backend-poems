package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.PoemHighlight;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class HighlightResponse {
  private Long id;
  private Long poemId;
  private Integer startOffset;
  private Integer endOffset;
  private String selectedText;
  private String note;
  private Instant createdAt;

  public static HighlightResponse from(PoemHighlight h) {
    return new HighlightResponse(
      h.getId(),
      h.getPoemId(),
      h.getStartOffset(),
      h.getEndOffset(),
      h.getSelectedText(),
      h.getNote(),
      h.getCreatedAt()
    );
  }
}
