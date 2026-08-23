package org.oplearn.project.repository.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class StatisticRedisRepository {
  private static final String VIEW_COOLDOWN_PREFIX = "poem:view_cooldown:";
  private static final String PENDING_VIEWS_KEY = "poem:views:pending";
  private static final Duration COOLDOWN_DURATION = Duration.ofMinutes(1);

  private final StringRedisTemplate redisTemplate;

  public boolean checkAndSetViewCooldown(Long poemId, String userIdentifier) {
    String key = VIEW_COOLDOWN_PREFIX + poemId + ":" + userIdentifier;
    Boolean isNewView = redisTemplate.opsForValue().setIfAbsent(key, "1", COOLDOWN_DURATION);
    return Boolean.TRUE.equals(isNewView);
  }

  public void incrementPendingView(Long poemId, long delta) {
    redisTemplate.opsForHash().increment(PENDING_VIEWS_KEY, poemId.toString(), delta);
  }

  public Map<Long, Long> getAndClearPendingViews() {
    if (!Boolean.TRUE.equals(redisTemplate.hasKey(PENDING_VIEWS_KEY))) {
      return Collections.emptyMap();
    }

    String syncKey = PENDING_VIEWS_KEY + ":syncing:" + System.currentTimeMillis();
    try {
      redisTemplate.rename(PENDING_VIEWS_KEY, syncKey);
    } catch (Exception e) {
      log.warn("(getAndClearPendingViews) rename failed: {}", e.getMessage());
      return Collections.emptyMap();
    }

    Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(syncKey);
    redisTemplate.delete(syncKey);

    Map<Long, Long> result = new HashMap<>();
    for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
      try {
        Long poemId = Long.valueOf(entry.getKey().toString());
        Long delta = Long.valueOf(entry.getValue().toString());
        if (delta > 0) {
          result.put(poemId, delta);
        }
      } catch (NumberFormatException ex) {
        log.error("(getAndClearPendingViews) parse error for key: {}", entry.getKey(), ex);
      }
    }

    return result;
  }
}
