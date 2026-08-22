package org.oplearn.project.integration.zalo;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Vòng lặp long-poll: liên tục gọi getUpdates, mỗi tin nhắn đến thì nhờ
 * ZaloBotService soạn trả lời rồi gửi lại. Chạy trên thread daemon nên không
 * chặn khởi động. Tắt bằng ENV ZALO_BOT_ENABLED=false.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "zalo.bot.enabled", havingValue = "true", matchIfMissing = false)
public class ZaloBotPoller implements ApplicationRunner {

  private final ZaloBotClient client;
  private final ZaloBotService service;

  // Chống xử lý trùng: nhớ ~500 message_id gần nhất (docs Zalo mỏng về offset).
  private final Set<String> seen = Collections.synchronizedSet(
    Collections.newSetFromMap(new LinkedHashMap<>() {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
        return size() > 500;
      }
    }));

  private volatile boolean running = true;

  @Override
  public void run(ApplicationArguments args) {
    Thread t = new Thread(this::loop, "zalo-bot-poll");
    t.setDaemon(true);
    t.start();
    log.info("(zalo-bot) long-polling started");
  }

  @PreDestroy
  void stop() {
    running = false;
  }

  private void loop() {
    while (running) {
      try {
        JsonNode root = client.getUpdates();
        if (root == null || !root.path("ok").asBoolean(false)) {
          sleep(1000);
          continue;
        }
        JsonNode result = root.path("result");
        if (result.isArray()) {
          result.forEach(this::handle);
        } else {
          handle(result);
        }
      } catch (Exception e) {
        log.warn("(zalo-bot) poll error: {}", e.getMessage());
        sleep(2000);
      }
    }
  }

  private void handle(JsonNode update) {
    JsonNode msg = update.path("message");
    if (msg.isMissingNode() || msg.isNull()) return;
    String text = msg.path("text").asText(null);
    String chatId = msg.path("chat").path("id").asText(null);
    String id = msg.path("message_id").asText(null);
    if (chatId == null || text == null) return;
    if (id != null && !seen.add(id)) return; // đã trả lời rồi
    try {
      client.sendMessage(chatId, service.reply(text));
    } catch (Exception e) {
      log.warn("(zalo-bot) send fail: {}", e.getMessage());
    }
  }

  private void sleep(long ms) {
    try {
      Thread.sleep(ms);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
      running = false;
    }
  }
}
