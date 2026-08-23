package org.oplearn.project.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Poem;

@Getter
@Setter
@NoArgsConstructor
public class PoemResponse {
  private static final int EXCERPT_LINES = 4;

  private Long id;
  private String name;
  private String description;
  private Integer year;
  private String content;
  private String transliteration;
  private String translation;
  private String language;
  private String era;
  private String genreName;
  private String authorName;
  private Long authorId;

  // Bổ sung ở trang chi tiết (không nằm trong projection JPQL 11 tham số)
  private String meaning;
  private List<PoemTranslationResponse> translations;
  private PoemStatisticsResponse statistics;

  /** Constructor dùng cho projection JPQL — GIỮ đúng 11 tham số, thứ tự cố định. */
  public PoemResponse(
    Long id, String name, String description, Integer year, String content,
    String transliteration, String translation, String language, String era,
    String genreName, String authorName
  ) {
    this.id = id;
    this.name = name;
    this.description = description;
    this.year = year;
    this.content = content;
    this.transliteration = transliteration;
    this.translation = translation;
    this.language = language;
    this.era = era;
    this.genreName = genreName;
    this.authorName = authorName;
  }

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
      poem.getEra(),
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
