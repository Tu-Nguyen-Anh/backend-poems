package org.oplearn.project.configuration;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.oplearn.project.constants.OpLearnConstants.CacheConstant;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCache;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class CacheConfiguration {

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    mapper.activateDefaultTyping(
      LaissezFaireSubTypeValidator.instance,
      ObjectMapper.DefaultTyping.NON_FINAL,
      JsonTypeInfo.As.PROPERTY
    );

    GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(mapper);

    RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
      .entryTtl(Duration.ofMinutes(10))
      .disableCachingNullValues()
      .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
      .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put(CacheConstant.CACHE_POEMS_PAGE, config.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put(CacheConstant.CACHE_POEMS_BROWSE, config.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put(CacheConstant.CACHE_POEM_DETAIL, config.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put(CacheConstant.CACHE_AUTHORS_PAGE, config.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put(CacheConstant.CACHE_POEMS_LATEST, config.entryTtl(Duration.ofMinutes(10)));

    return RedisCacheManager.builder(connectionFactory)
      .cacheDefaults(config)
      .withInitialCacheConfigurations(cacheConfigs)
      .enableStatistics()
      .build();
  }

  @org.springframework.web.bind.annotation.RestController
  @org.springframework.web.bind.annotation.RequestMapping("/api/v1/cache")
  @lombok.RequiredArgsConstructor
  public static class CacheStatsController {
    private final RedisCacheManager cacheManager;

    @org.springframework.web.bind.annotation.GetMapping("/stats")
    public org.springframework.http.ResponseEntity<Map<String, Object>> getStats() {
      Map<String, Object> result = new LinkedHashMap<>();
      for (String name : cacheManager.getCacheNames()) {
        RedisCache cache = (RedisCache) cacheManager.getCache(name);
        if (cache != null) {
          var stats = cache.getStatistics();
          long hits = stats.getHits();
          long misses = stats.getMisses();
          double ratio = (hits + misses) > 0 ? ((double) hits / (hits + misses)) * 100 : 0.0;
          result.put(name, Map.of(
            "hits", hits,
            "misses", misses,
            "puts", stats.getPuts(),
            "hit_ratio", String.format("%.2f%%", ratio)
          ));
        }
      }
      return org.springframework.http.ResponseEntity.ok(result);
    }
  }
}
