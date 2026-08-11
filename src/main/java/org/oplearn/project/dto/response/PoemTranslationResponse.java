package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.PoemTranslation;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoemTranslationResponse {
  private String translator;
  private String content;
  private Integer sortOrder;

  public static PoemTranslationResponse from(PoemTranslation t) {
    return new PoemTranslationResponse(t.getTranslator(), t.getContent(), t.getSortOrder());
  }
}
