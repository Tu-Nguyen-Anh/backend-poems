package org.oplearn.project.configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.service.AuthorService;
import org.oplearn.project.service.GenreService;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.StoryService;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Làm "nóng" ứng dụng ngay sau khi UP: gọi trước các đường đọc mà trang chủ hay dùng
 * để JVM JIT-compile, Hibernate dựng sẵn query, và Postgres prime execution plan.
 * Nhờ đó user ĐẦU TIÊN sau deploy/idle không phải "ăn" trọn cú cold-start (đo được
 * ~1.8–3.7s → ~0.13s khi đã ấm). Chạy trên thread riêng để KHÔNG làm chậm lúc khởi động.
 * Tắt bằng ENV: APP_WARMUP_ENABLED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.warmup.enabled", havingValue = "true", matchIfMissing = true)
public class WarmupRunner implements ApplicationRunner {

  private final PoemService poemService;
  private final AuthorService authorService;
  private final GenreService genreService;
  private final StoryService storyService;

  @Override
  public void run(ApplicationArguments args) {
    Thread t = new Thread(this::warmUp, "app-warmup");
    t.setDaemon(true);
    t.start();
  }

  private void warmUp() {
    long started = System.currentTimeMillis();
    runQuietly("poems.latest", () -> poemService.listPoemLatest(6, 0));
    runQuietly("poems.list", () -> poemService.list(null, null, null, null, 10, 0));
    runQuietly("poems.random", () -> poemService.randomPersonalized(null, null, null));
    runQuietly("poems.stats", poemService::getStats);
    runQuietly("poems.eras", poemService::listEras);
    runQuietly("authors.featured", authorService::featured);
    runQuietly("authors.top", () -> authorService.listTopByPoemCount(1, 0));
    runQuietly("authors.list", () -> authorService.list(null, null, 24, 0, false));
    runQuietly("genres.list", () -> genreService.list(null, 8, 0, false));
    runQuietly("stories.list", () -> storyService.list(null, null, null, 1, 0));
    runQuietly("stories.collections", storyService::collections);
    log.info("(warmup) done in {}ms", System.currentTimeMillis() - started);
  }

  private void runQuietly(String label, Runnable task) {
    try {
      task.run();
    } catch (Exception e) {
      log.warn("(warmup) {} failed: {}", label, e.getMessage());
    }
  }
}
