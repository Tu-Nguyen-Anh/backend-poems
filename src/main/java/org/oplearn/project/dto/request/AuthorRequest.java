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
public class AuthorRequest {
  @NotBlank(message = "author.name.not_blank")
  @Size(max = 255, message = "author.name.max_length")
  private String name;

  @Min(value = 1, message = "author.birth_year.invalid")
  @Max(value = 2100, message = "author.birth_year.invalid")
  private Integer birthYear;

  @Size(max = 255, message = "author.hometown.max_length")
  private String hometown;

  @Size(max = 1000, message = "author.achievement.max_length")
  private String achievement;
}
