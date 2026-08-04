package org.oplearn.project.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@NoArgsConstructor
@AllArgsConstructor
public class PoemRequest {
  @NotBlank(message = "poem.name.not_blank")
  @Size(max = 255, message = "poem.name.max_length")
  private String name;

  @Size(max = 1000, message = "poem.description.max_length")
  private String description;

  @Min(value = 1, message = "poem.year.invalid")
  @Max(value = 2100, message = "poem.year.invalid")
  private Integer year;

  @NotBlank(message = "poem.content.not_blank")
  private String content;

  private String transliteration;

  private String translation;

  @Size(max = 50, message = "poem.language.max_length")
  private String language;

  private Long genreId;

  private Long authorId;
}
