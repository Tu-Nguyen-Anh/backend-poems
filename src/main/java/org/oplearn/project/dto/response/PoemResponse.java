package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Poem;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PoemResponse {
  private static final int EXCERPT_LINES = 4;

  private Long id;
  private String name;
  private String description;
  private int year;
  private String content;
  private String transliteration;
  private String translation;
  private String language;
  private String genreName;
  private String authorName;

  public static PoemResponse fromSummary(PoemResponse poem) {
    if (poem != null) {
      poem.setContent(excerpt(poem.getContent()));
    }
    return poem;
  }

  public static PoemResponse from(Poem poem, String genreName, String authorName) {
    return new PoemResponse(
      poem.getId(),
      poem.getName(),
      poem.getDescription(),
      poem.getYear(),
      poem.getContent(),
      poem.getTransliteration(),
      poem.getTranslation(),
      poem.getLanguage(),
      genreName,
      authorName
    );
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
