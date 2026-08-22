package org.oplearn.project.integration.zalo;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/** Gọi HTTP API của Zalo Bot: {api-base}/bot{token}/{method} (POST JSON). */
@Slf4j
@Component
public class ZaloBotClient {

  private final ObjectMapper om;
  private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

  @Value("${zalo.bot.token}")
  private String token;

  @Value("${zalo.bot.api-base:https://bot-api.zaloplatforms.com}")
  private String apiBase;

  @Value("${zalo.bot.poll-timeout-seconds:30}")
  private int pollTimeout;

  public ZaloBotClient(ObjectMapper om) {
    this.om = om;
  }

  public int getPollTimeout() {
    return pollTimeout;
  }

  private String url(String method) {
    return apiBase + "/bot" + token + "/" + method;
  }

  /** Gửi tin nhắn văn bản về 1 chat (giới hạn 2000 ký tự — nơi gọi tự cắt). */
  public void sendMessage(String chatId, String text) throws Exception {
    String body = om.writeValueAsString(Map.of("chat_id", chatId, "text", text));
    post("sendMessage", body, 25);
  }

  /** Long-poll: chờ tin mới tối đa poll-timeout giây, trả JSON gốc. */
  public JsonNode getUpdates() throws Exception {
    String body = om.writeValueAsString(Map.of("timeout", pollTimeout));
    return om.readTree(post("getUpdates", body, pollTimeout + 20));
  }

  private String post(String method, String json, int timeoutSec) throws Exception {
    HttpRequest req = HttpRequest.newBuilder(URI.create(url(method)))
      .timeout(Duration.ofSeconds(timeoutSec))
      .header("Content-Type", "application/json")
      .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
      .build();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    return resp.body();
  }
}
