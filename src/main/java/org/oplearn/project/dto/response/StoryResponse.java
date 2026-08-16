package org.oplearn.project.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Story;

@Getter
@Setter
@NoArgsConstructor
public class StoryResponse {
  private Long id;
  private String title;
  private String author;
  private String authorUrl;
  private Long authorId;
  private Integer year;
  private String genre;
  private String category;
  private String collection;
  private String source;
  private String type;
  private Integer chapterCount;
  private Integer charCount;
  private Integer wordCount;

  // Chỉ có ở trang chi tiết: mục lục chương (KHÔNG kèm nội dung).
  private List<StoryChapterMetaResponse> chapters;

  public static StoryResponse from(Story s) {
    StoryResponse r = new StoryResponse();
    r.id = s.getId();
    r.title = s.getTitle();
    r.author = s.getAuthor();
    r.authorUrl = s.getAuthorUrl();
    r.authorId = s.getAuthorId();
    r.year = s.getYear();
    r.genre = s.getGenre();
    r.category = s.getCategory();
    r.collection = s.getCollection();
    r.source = s.getSource();
    r.type = s.getType();
    r.chapterCount = s.getChapterCount();
    r.charCount = s.getCharCount();
    r.wordCount = s.getWordCount();
    return r;
  }
}
