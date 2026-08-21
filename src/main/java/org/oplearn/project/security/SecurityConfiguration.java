package org.oplearn.project.security;

import lombok.RequiredArgsConstructor;
import org.oplearn.project.security.error.UnAuthenticationCustomHandler;
import org.oplearn.project.security.error.UnAuthorizationCustomHandler;
import org.oplearn.project.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.MATCHER_ADMIN_API;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.MATCHER_AUTH_PUBLIC_API;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.ROLE_ADMIN;
import static org.oplearn.project.constants.OpLearnConstants.AuthConstant.WHITE_LIST;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final UnAuthenticationCustomHandler unAuthenticationCustomHandler;
  private final UnAuthorizationCustomHandler unAuthorizationCustomHandler;

  @Value("${security.cors.allowed-origins:*}")
  private List<String> allowedOrigins;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity
      .csrf(AbstractHttpConfigurer::disable)
      .cors(cors -> cors.configurationSource(corsConfigurationSource()))
      .sessionManagement(session ->
        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(WHITE_LIST).permitAll()
        .requestMatchers(MATCHER_AUTH_PUBLIC_API).permitAll()
        .requestMatchers(HttpMethod.GET,
          "/api/v1/poems/**",
          "/api/v1/stories/**",
          "/api/v1/authors/**",
          "/api/v1/genres/**",
          "/api/v1/comments/**",
          "/api/v1/replies/**",
          "/api/v1/compositions/**").permitAll()
        // Chỉ ADMIN được liệt kê toàn bộ user / feedback + xem chi tiết theo id
        // (chống IDOR + lộ dữ liệu). Profile chính chủ đi qua GET /users/{id} có
        // kiểm owner ở service; feedback của user qua /feedbacks/user/{id}.
        .requestMatchers(HttpMethod.GET, "/api/v1/users").hasRole(ROLE_ADMIN)
        .requestMatchers(HttpMethod.GET, "/api/v1/feedbacks", "/api/v1/feedbacks/*").hasRole(ROLE_ADMIN)
        .requestMatchers(MATCHER_ADMIN_API).hasRole(ROLE_ADMIN)
        .requestMatchers(HttpMethod.POST, "/api/v1/authors/**", "/api/v1/genres/**", "/api/v1/poems/**", "/api/v1/users/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.PUT, "/api/v1/authors/**", "/api/v1/genres/**", "/api/v1/poems/**" , "/api/v1/feedbacks/status/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/v1/authors/**", "/api/v1/genres/**", "/api/v1/poems/**" , "/api/v1/users/**").hasRole("ADMIN")
        .anyRequest().authenticated())
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint(unAuthenticationCustomHandler)
        .accessDeniedHandler(unAuthorizationCustomHandler));
    return httpSecurity.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    configuration.setAllowedOriginPatterns(allowedOrigins);
    configuration.addAllowedHeader(CorsConfiguration.ALL);
    configuration.addAllowedMethod(CorsConfiguration.ALL);
    // Cho phép gửi/nhận cookie (refresh_token HttpOnly) cross-origin. Bắt buộc
    // dùng allowedOriginPatterns (không phải "*" cứng) khi bật credentials.
    configuration.setAllowCredentials(true);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }
}
