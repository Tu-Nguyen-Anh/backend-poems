package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.repository.AuthorRepository;
import org.oplearn.project.repository.GenreRepository;
import org.oplearn.project.repository.PoemRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.List;

/**
 * Sitemap động sinh thẳng từ DB — bài thơ/tác giả/thể loại mới tự có mặt,
 * không cần rebuild image. Nginx của frontend proxy /sitemap.xml và
 * /sitemaps/* về đây để sitemap nằm trên domain chính.
 */
@RestController
@Slf4j
@RequiredArgsConstructor
public class SitemapController {

  private final PoemRepository poemRepository;
  private final AuthorRepository authorRepository;
  private final GenreRepository genreRepository;

  @Value("${app.seo.base-url:https://daithihao.tuvidausotoanthu.vn}")
  private String baseUrl;

  /** Giới hạn 50k URL/file theo chuẩn sitemap; chừa biên độ an toàn. */
  private static final int POEMS_PER_FILE = 40000;

  @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> index() {
    long total = poemRepository.countByIsDeletedFalse();
    int chunks = (int) Math.max(1, (total + POEMS_PER_FILE - 1) / POEMS_PER_FILE);

    StringBuilder sb = new StringBuilder(XML_HEADER)
      .append("<sitemapindex xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
    appendSitemapRef(sb, "/sitemaps/static.xml");
    appendSitemapRef(sb, "/sitemaps/authors.xml");
    appendSitemapRef(sb, "/sitemaps/genres.xml");
    for (int i = 1; i <= chunks; i++) {
      appendSitemapRef(sb, "/sitemaps/poems-" + i + ".xml");
    }
    sb.append("</sitemapindex>\n");
    return xml(sb.toString());
  }

  @GetMapping(value = "/sitemaps/static.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> staticPages() {
    StringBuilder sb = urlsetOpen();
    for (String path : List.of("/", "/poems", "/authors", "/genres")) {
      appendUrl(sb, path);
    }
    return xml(urlsetClose(sb));
  }

  @GetMapping(value = "/sitemaps/authors.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> authors() {
    StringBuilder sb = urlsetOpen();
    authorRepository.findIdsForSitemap().forEach(id -> appendUrl(sb, "/authors/" + id));
    return xml(urlsetClose(sb));
  }

  @GetMapping(value = "/sitemaps/genres.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> genres() {
    StringBuilder sb = urlsetOpen();
    genreRepository.findIdsForSitemap().forEach(id -> appendUrl(sb, "/genres/" + id));
    return xml(urlsetClose(sb));
  }

  @GetMapping(value = "/sitemaps/poems-{page}.xml", produces = MediaType.APPLICATION_XML_VALUE)
  public ResponseEntity<String> poems(@PathVariable int page) {
    StringBuilder sb = urlsetOpen();
    poemRepository.findIdsForSitemap(PageRequest.of(page - 1, POEMS_PER_FILE))
      .forEach(id -> appendUrl(sb, "/poems/" + id));
    return xml(urlsetClose(sb));
  }

  private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";

  private StringBuilder urlsetOpen() {
    return new StringBuilder(XML_HEADER)
      .append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
  }

  private String urlsetClose(StringBuilder sb) {
    return sb.append("</urlset>\n").toString();
  }

  private void appendSitemapRef(StringBuilder sb, String path) {
    sb.append("<sitemap><loc>").append(baseUrl).append(path).append("</loc></sitemap>\n");
  }

  private void appendUrl(StringBuilder sb, String path) {
    sb.append("<url><loc>").append(baseUrl).append(path).append("</loc></url>\n");
  }

  private ResponseEntity<String> xml(String body) {
    return ResponseEntity.ok()
      .cacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic())
      .contentType(MediaType.APPLICATION_XML)
      .body(body);
  }
}
