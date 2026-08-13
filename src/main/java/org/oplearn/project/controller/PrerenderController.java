package org.oplearn.project.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.repository.AuthorRepository;
import org.oplearn.project.repository.GenreRepository;
import org.oplearn.project.repository.PoemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * "Dynamic rendering" cho bot mạng xã hội (Facebook/Zalo/Twitter…) không chạy JS.
 * Nginx nhận diện User-Agent bot rồi chuyển sang /prerender + path gốc; controller
 * trả HTML kèm thẻ OG/Twitter đúng theo trang (tiêu đề, mô tả, ảnh tuyệt đối).
 * Người dùng thật vẫn nhận SPA bình thường (nginx không đổi luồng của họ).
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class PrerenderController {
  private static final String SITE = "Tiểu Thi Hào";
  private static final String DEFAULT_DESC =
    "Kho tàng thơ ca Việt Nam và thế giới — tra cứu theo tác giả, thể loại, đọc nguyên tác kèm phiên âm, dịch nghĩa và nhiều bản dịch.";
  private static final String PLACEHOLDER_TITLE = "(không tiêu đề)";

  private final PoemRepository poemRepository;
  private final AuthorRepository authorRepository;
  private final GenreRepository genreRepository;

  @Value("${app.seo.base-url:https://daithihao.tuvidausotoanthu.vn}")
  private String webUrl;

  @GetMapping(value = "/prerender/**", produces = MediaType.TEXT_HTML_VALUE + ";charset=UTF-8")
  public String prerender(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String path = uri.length() > "/prerender".length() ? uri.substring("/prerender".length()) : "/";
    path = URLDecoder.decode(path, StandardCharsets.UTF_8);
    if (path.isBlank()) path = "/";

    String title = SITE + " – Kho tàng thơ ca Việt Nam";
    String desc = DEFAULT_DESC;
    String ogType = "website";
    String body = "";

    try {
      if (path.matches("/authors/\\d+")) {
        long id = Long.parseLong(path.substring("/authors/".length()));
        var a = authorRepository.findById(id).orElse(null);
        if (a != null) {
          title = a.getName() + " – Tác giả";
          desc = "Tuyển tập thơ và tác phẩm của " + a.getName() + " trên " + SITE + ".";
          ogType = "profile";
          body = "<h1>" + esc(a.getName()) + "</h1>";
        }
      } else if (path.matches("/genres/\\d+")) {
        long id = Long.parseLong(path.substring("/genres/".length()));
        var g = genreRepository.findById(id).orElse(null);
        if (g != null) {
          title = g.getName() + " – Thể thơ";
          desc = "Tuyển tập các bài thơ thể " + g.getName() + " trên " + SITE + ".";
          body = "<h1>" + esc(g.getName()) + "</h1>";
        }
      } else if (path.equals("/poems")) {
        title = "Kho tàng bài thơ – " + SITE;
        body = "<h1>Kho tàng bài thơ</h1>";
      } else if (!path.equals("/")) {
        // Coi như slug bài thơ: segment cuối là mã base36 của id.
        Long id = idFromSlug(path);
        PoemResponse p = id == null ? null : poemRepository.findByIdAndReturnResponse(id).orElse(null);
        if (p != null) {
          String t = displayTitle(p);
          String author = blank(p.getAuthorName()) ? "Khuyết danh" : p.getAuthorName().trim();
          title = t + " – " + author;
          desc = excerpt(p.getContent());
          ogType = "article";
          body = "<h1>" + esc(t) + "</h1><p>" + esc(author) + "</p><pre>" + esc(p.getContent()) + "</pre>";
        }
      }
    } catch (Exception e) {
      log.warn("(prerender) lỗi dựng meta cho path {}: {}", path, e.toString());
    }

    String url = webUrl + path;
    String image = webUrl + "/logo.png";
    return render(title, desc, url, image, ogType, body);
  }

  /* ---------- helpers ---------- */

  private static boolean blank(String s) {
    return s == null || s.isBlank();
  }

  /** Mã base36 ở segment cuối slug → id. */
  private static Long idFromSlug(String path) {
    String slug = path.startsWith("/") ? path.substring(1) : path;
    int dash = slug.lastIndexOf('-');
    String token = dash >= 0 ? slug.substring(dash + 1) : slug;
    if (token.isEmpty()) return null;
    try {
      long id = Long.parseLong(token, 36);
      return id > 0 ? id : null;
    } catch (NumberFormatException e) {
      return null;
    }
  }

  /** Tựa hiển thị: bài không tựa → lấy câu thơ đầu. */
  private static String displayTitle(PoemResponse p) {
    String name = p.getName() == null ? "" : p.getName().trim();
    if (!name.isEmpty() && !name.equals(PLACEHOLDER_TITLE)) return name;
    for (String line : (p.getContent() == null ? "" : p.getContent()).split("\n")) {
      String l = line.trim();
      if (!l.isEmpty()) return l.length() > 70 ? l.substring(0, 70) + "…" : l;
    }
    return "Không đề";
  }

  /** Mô tả = 1–2 câu thơ đầu, gọn ~180 ký tự. */
  private static String excerpt(String content) {
    if (content == null) return DEFAULT_DESC;
    StringBuilder sb = new StringBuilder();
    int lines = 0;
    for (String line : content.split("\n")) {
      String l = line.trim();
      if (l.isEmpty()) continue;
      if (sb.length() > 0) sb.append(" / ");
      sb.append(l);
      if (++lines >= 2) break;
    }
    String s = sb.toString();
    if (s.length() > 180) s = s.substring(0, 179).trim() + "…";
    return s.isEmpty() ? DEFAULT_DESC : s;
  }

  private static String esc(String s) {
    if (s == null) return "";
    return s.replace("&", "&amp;")
      .replace("<", "&lt;")
      .replace(">", "&gt;")
      .replace("\"", "&quot;")
      .replace("'", "&#39;");
  }

  private String render(String title, String desc, String url, String image, String ogType, String body) {
    String t = esc(title);
    String d = esc(desc);
    return "<!doctype html>\n<html lang=\"vi\"><head>\n"
      + "<meta charset=\"utf-8\">\n"
      + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
      + "<title>" + t + "</title>\n"
      + "<meta name=\"description\" content=\"" + d + "\">\n"
      + "<link rel=\"canonical\" href=\"" + esc(url) + "\">\n"
      + "<meta property=\"og:site_name\" content=\"" + SITE + "\">\n"
      + "<meta property=\"og:type\" content=\"" + ogType + "\">\n"
      + "<meta property=\"og:title\" content=\"" + t + "\">\n"
      + "<meta property=\"og:description\" content=\"" + d + "\">\n"
      + "<meta property=\"og:url\" content=\"" + esc(url) + "\">\n"
      + "<meta property=\"og:image\" content=\"" + esc(image) + "\">\n"
      + "<meta name=\"twitter:card\" content=\"summary_large_image\">\n"
      + "<meta name=\"twitter:title\" content=\"" + t + "\">\n"
      + "<meta name=\"twitter:description\" content=\"" + d + "\">\n"
      + "<meta name=\"twitter:image\" content=\"" + esc(image) + "\">\n"
      + "</head><body>\n" + body + "\n"
      + "<p><a href=\"" + esc(url) + "\">Xem tại " + SITE + "</a></p>\n"
      + "</body></html>";
  }
}
