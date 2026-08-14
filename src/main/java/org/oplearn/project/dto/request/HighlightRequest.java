package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class HighlightRequest {
  private Long poemId;

  @NotNull(message = "highlight.start_offset.not_null")
  private Integer startOffset;

  @NotNull(message = "highlight.end_offset.not_null")
  private Integer endOffset;

  @NotBlank(message = "highlight.selected_text.not_blank")
  private String selectedText;

  @Size(max = 2000, message = "highlight.note.max_length")
  private String note;
}
