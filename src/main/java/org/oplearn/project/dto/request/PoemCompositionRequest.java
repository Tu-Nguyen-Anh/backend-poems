package org.oplearn.project.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.enums.PoemCompositionStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PoemCompositionRequest {
  @NotBlank(message = "poem.composition.content.not_blank")
  @Size(max = 2000, message = "poem.composition.content.max_length")
  private String content;

  @NotBlank(message = "poem.composition.title.not_blank")
  @Size(max = 200, message = "poem.composition.title.max_length")
  private String title;

  private String penName;

  private Long genreId;

  private PoemCompositionStatus status;
}
