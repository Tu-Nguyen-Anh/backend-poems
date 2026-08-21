package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.enums.PoemCompositionStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoemCompositionResponse {
  private static final int EXCERPT_LINES = 4;

  private Long id;
  private Long userId;
  private String username;
  private String content;
  private String penName;
  private String title;
  private Long genreId;
  private String genreName;
  private PoemCompositionStatus status;

  public static PoemCompositionResponse fromSummary(PoemCompositionResponse composition) {
    if (composition != null) {
      composition.setContent(excerpt(composition.getContent()));
    }
    return composition;
  }

  private static String excerpt(String content) {
    if (content == null) return "";
    String[] lines = content.split("\n");
    if (lines.length <= EXCERPT_LINES) {
      return content;
    }
    return String.join("\n", java.util.Arrays.copyOf(lines, EXCERPT_LINES)) + "\n...";
  }
}
