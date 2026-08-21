package org.oplearn.project.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.StoryChapter;

/** Nội dung 1 chương (tải theo yêu cầu khi người đọc mở chương). */
@Getter
@Setter
@NoArgsConstructor
public class StoryChapterResponse {
  private Long id;
  private Long storyId;
  private Integer seq;
  private String title;
  private Integer wordCount;
  private Integer charCount;
  private String content;

  public static StoryChapterResponse from(StoryChapter c) {
    StoryChapterResponse r = new StoryChapterResponse();
    r.id = c.getId();
    r.storyId = c.getStoryId();
    r.seq = c.getSeq();
    r.title = c.getTitle();
    r.wordCount = c.getWordCount();
    r.charCount = c.getCharCount();
    r.content = c.getContent();
    return r;
  }
}
