package org.oplearn.project.repository;

import org.oplearn.project.entity.StoryChapter;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StoryChapterRepository extends JpaRepository<StoryChapter, Long> {

  /** Mục lục chương (không kèm content) — projection tránh kéo cả nội dung. */
  List<ChapterMeta> findByStoryIdOrderBySeqAsc(Long storyId);

  Optional<StoryChapter> findByStoryIdAndSeq(Long storyId, Integer seq);

  interface ChapterMeta {
    Integer getSeq();

    String getTitle();

    Integer getWordCount();

    Integer getCharCount();
  }
}
