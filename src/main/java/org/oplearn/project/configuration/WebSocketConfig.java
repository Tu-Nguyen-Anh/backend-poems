package org.oplearn.project.configuration;

import lombok.RequiredArgsConstructor;
import org.oplearn.project.websocket.EchoWebSocketHandler;
import org.oplearn.project.websocket.WebSocketAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

  private final EchoWebSocketHandler echoWebSocketHandler;
  private final WebSocketAuthInterceptor webSocketAuthInterceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(echoWebSocketHandler, "/ws/echo", "/ws/poems")
      .addInterceptors(webSocketAuthInterceptor)
      .setAllowedOrigins("*");
  }
}
