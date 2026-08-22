package org.oplearn.project.integration.zalo;

import java.text.Normalizer;
import java.util.List;

import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.service.AuthorService;
import org.oplearn.project.service.PoemService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Biến 1 tin nhắn của user thành nội dung trả lời (thơ ngẫu nhiên / theo tác giả). */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloBotService {

  private final PoemService poemService;
  private final AuthorService authorService;

  /** Zalo giới hạn 2000 ký tự/tin — chừa lề an toàn. */
  private static final int MAX = 1900;
  private static final String[] AUTHOR_PREFIXES = { "/tacgia", "/tg", "tac gia", "tacgia" };

  public String reply(String textRaw) {
    String text = textRaw == null ? "" : textRaw.trim();
    String lower = stripAccents(text.toLowerCase());

    if (text.isEmpty() || lower.equals("start") || lower.equals("/start")
      || lower.equals("help") || lower.startsWith("/help") || lower.equals("menu")) {
      return help();
    }

    // /tacgia <tên>  (giữ nguyên dấu ở tên để tìm chính xác)
    for (String p : AUTHOR_PREFIXES) {
      if (lower.equals(p) || lower.startsWith(p + " ")) {
        String name = text.substring(p.length()).trim();
        if (name.isBlank()) return "Bạn gõ kèm tên tác giả nhé, ví dụ: /tacgia Nguyễn Du";
        return randomByAuthor(name);
      }
    }

    if (lower.equals("/tho") || lower.equals("/random") || lower.equals("tho")
      || lower.equals("random") || lower.contains("ngau nhien") || lower.contains("bai tho")) {
      return randomPoem();
    }

    return help();
  }

  private String randomPoem() {
    try {
      List<PoemResponse> list = poemService.randomPersonalized(null, null, null).getContent();
      if (list.isEmpty()) return "Hiện chưa có bài thơ nào.";
      return format(poemService.detail(list.get(0).getId()));
    } catch (Exception e) {
      log.warn("(zalo-bot) randomPoem error: {}", e.getMessage());
      return "Có lỗi khi lấy bài thơ, bạn thử lại nhé.";
    }
  }

  private String randomByAuthor(String name) {
    try {
      List<AuthorResponse> authors = authorService.list(name, "poem", 1, 0, false).getContent();
      if (authors.isEmpty()) return "Không tìm thấy tác giả \"" + name + "\". Bạn thử tên khác xem.";
      AuthorResponse author = authors.get(0);
      List<PoemResponse> list = poemService.randomPersonalized(List.of(author.getId()), null, null).getContent();
      if (list.isEmpty()) return "Tác giả " + author.getName() + " chưa có bài thơ nào.";
      return format(poemService.detail(list.get(0).getId()));
    } catch (Exception e) {
      log.warn("(zalo-bot) randomByAuthor error: {}", e.getMessage());
      return "Có lỗi khi tìm theo tác giả, bạn thử lại nhé.";
    }
  }

  private String format(PoemResponse p) {
    StringBuilder sb = new StringBuilder();
    sb.append("🎋 ").append(p.getName() == null ? "(Không tên)" : p.getName()).append("\n");
    if (p.getAuthorName() != null && !p.getAuthorName().isBlank()) {
      sb.append("✍️ ").append(p.getAuthorName()).append("\n");
    }
    sb.append("\n").append(p.getContent() == null ? "" : p.getContent().trim());
    String out = sb.toString();
    if (out.length() > MAX) out = out.substring(0, MAX).trim() + "\n…";
    return out;
  }

  private String help() {
    return "Chào bạn! Mình là bot Thi Đàn 📜\n\n"
      + "• /tho — nhận ngẫu nhiên 1 bài thơ\n"
      + "• /tacgia <tên> — ngẫu nhiên 1 bài của tác giả\n"
      + "   ví dụ: /tacgia Nguyễn Du";
  }

  /** Bỏ dấu tiếng Việt + đ→d để so khớp lệnh không phụ thuộc dấu. */
  private static String stripAccents(String s) {
    String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return n.replace('đ', 'd').replace('Đ', 'D');
  }
}
