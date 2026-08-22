package org.oplearn.project.integration.zalo;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.service.AuthorService;
import org.oplearn.project.service.PoemService;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Biến 1 tin nhắn của user thành DANH SÁCH tin trả lời (Zalo giới hạn 2000
 * ký tự/tin nên danh sách dài được chẻ thành nhiều tin):
 *  - /tho              → ngẫu nhiên 1 bài (kèm bản dịch nếu có)
 *  - /tacgia <tên>     → LIỆT KÊ tối đa 100 bài của tác giả (đánh số) để chọn
 *  - trả lời 1 con số  → đọc bài theo số trong danh sách vừa liệt kê
 *  - "tiếp"            → trang kế của danh sách
 *  - /help             → hướng dẫn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloBotService {

  private final PoemService poemService;
  private final AuthorService authorService;

  private static final int MAX = 1900; // Zalo giới hạn 2000 ký tự/tin
  private static final int PAGE = 100; // số bài liệt kê mỗi trang
  private static final String[] AUTHOR_PREFIXES = { "/tacgia", "/tg", "tac gia", "tacgia" };

  /** Danh sách thơ theo tác giả đang duyệt của mỗi chat (để chọn theo số). */
  private record Browse(String authorName, Long authorId, int page, List<Long> ids, int total) {}

  private final Map<String, Browse> browsing = new ConcurrentHashMap<>();

  public List<String> reply(String chatId, String textRaw) {
    String text = textRaw == null ? "" : textRaw.trim();
    String lower = stripAccents(text.toLowerCase());

    if (text.isEmpty() || lower.equals("start") || lower.equals("/start")
      || lower.equals("help") || lower.startsWith("/help") || lower.equals("menu")) {
      return one(help());
    }

    // /tacgia <tên> → liệt kê để chọn
    for (String p : AUTHOR_PREFIXES) {
      if (lower.equals(p) || lower.startsWith(p + " ")) {
        String name = text.substring(p.length()).trim();
        if (name.isBlank()) return one("Bạn gõ kèm tên tác giả nhé, ví dụ: /tacgia Nguyễn Du");
        return listByAuthorName(chatId, name);
      }
    }

    // Đang có danh sách → 'tiếp' (trang sau) hoặc 1 con số (chọn bài)
    if (browsing.containsKey(chatId)) {
      if (lower.equals("tiep") || lower.equals("/tiep") || lower.equals("next") || lower.contains("xem them")) {
        return nextPage(chatId);
      }
      if (text.matches("\\d{1,3}")) {
        return one(pick(chatId, Integer.parseInt(text)));
      }
    }

    if (lower.equals("/tho") || lower.equals("/random") || lower.equals("tho")
      || lower.equals("random") || lower.contains("ngau nhien") || lower.contains("bai tho")) {
      return one(randomPoem());
    }

    if (text.matches("\\d{1,3}")) {
      return one("Bạn hãy /tacgia <tên> để liệt kê trước, rồi trả lời số để đọc nhé.");
    }

    return one(help());
  }

  private String randomPoem() {
    try {
      List<PoemResponse> list = poemService.randomPersonalized(null, null, null).getContent();
      if (list.isEmpty()) return "Hiện chưa có bài thơ nào.";
      return format(poemService.detail(preferVietnamese(list).getId()));
    } catch (Exception e) {
      log.warn("(zalo-bot) randomPoem error: {}", e.getMessage());
      return "Có lỗi khi lấy bài thơ, bạn thử lại nhé.";
    }
  }

  private List<String> listByAuthorName(String chatId, String name) {
    try {
      List<AuthorResponse> authors = authorService.list(name, "poem", 1, 0, false).getContent();
      if (authors.isEmpty()) return one("Không tìm thấy tác giả \"" + name + "\". Bạn thử tên khác xem.");
      AuthorResponse a = authors.get(0);
      return loadPage(chatId, a.getId(), a.getName(), 0);
    } catch (Exception e) {
      log.warn("(zalo-bot) listByAuthor error: {}", e.getMessage());
      return one("Có lỗi khi tìm theo tác giả, bạn thử lại nhé.");
    }
  }

  private List<String> loadPage(String chatId, Long authorId, String authorName, int page) {
    var pr = authorService.listPoemByAuthorId(authorId, PAGE, page);
    List<PoemResponse> list = pr.getContent();
    if (list.isEmpty()) {
      return one(page == 0 ? ("Tác giả " + authorName + " chưa có bài thơ nào.") : "Đã hết danh sách rồi.");
    }
    int total = pr.getAmount();
    List<Long> ids = list.stream().map(PoemResponse::getId).toList();
    browsing.put(chatId, new Browse(authorName, authorId, page, ids, total));

    boolean more = (long) (page + 1) * PAGE < total;
    String header = "📚 Thơ của " + authorName + " (trang " + (page + 1) + ", tổng " + total + " bài):\n";
    String footer = "\nTrả lời số 1–" + list.size() + " để đọc" + (more ? " · gõ 'tiếp' để xem thêm" : "") + ".";

    // Chẻ danh sách thành nhiều tin ≤ MAX ký tự; footer nằm ở tin cuối.
    List<String> msgs = new ArrayList<>();
    StringBuilder cur = new StringBuilder(header);
    for (int i = 0; i < list.size(); i++) {
      String nm = list.get(i).getName();
      String line = (i + 1) + ". " + (nm == null || nm.isBlank() ? "(Không tên)" : nm) + "\n";
      if (cur.length() + line.length() > MAX) {
        msgs.add(cur.toString());
        cur = new StringBuilder();
      }
      cur.append(line);
    }
    if (cur.length() + footer.length() > MAX) {
      msgs.add(cur.toString());
      cur = new StringBuilder();
    }
    cur.append(footer);
    msgs.add(cur.toString());
    return msgs;
  }

  private List<String> nextPage(String chatId) {
    Browse b = browsing.get(chatId);
    if (b == null) return one("Chưa có danh sách. Gõ /tacgia <tên> trước nhé.");
    return loadPage(chatId, b.authorId(), b.authorName(), b.page() + 1);
  }

  private String pick(String chatId, int n) {
    Browse b = browsing.get(chatId);
    if (b == null) return "Hãy /tacgia <tên> để liệt kê trước, rồi trả lời số nhé.";
    if (n < 1 || n > b.ids().size()) return "Số không hợp lệ, chọn 1–" + b.ids().size() + ".";
    try {
      return format(poemService.detail(b.ids().get(n - 1)));
    } catch (Exception e) {
      log.warn("(zalo-bot) pick error: {}", e.getMessage());
      return "Có lỗi khi mở bài thơ, bạn thử lại nhé.";
    }
  }

  /** Ưu tiên bài tiếng Việt trong lô random (tránh thơ Hán chỉ có phiên âm khó đọc). */
  private PoemResponse preferVietnamese(List<PoemResponse> list) {
    return list.stream()
      .filter(p -> p.getLanguage() == null || !p.getLanguage().equalsIgnoreCase("Hán"))
      .findFirst()
      .orElse(list.get(0));
  }

  private String format(PoemResponse p) {
    StringBuilder sb = new StringBuilder();
    sb.append("🎋 ").append(p.getName() == null ? "(Không tên)" : p.getName()).append("\n");
    if (p.getAuthorName() != null && !p.getAuthorName().isBlank()) {
      sb.append("✍️ ").append(p.getAuthorName()).append("\n");
    }
    sb.append("\n").append(p.getContent() == null ? "" : p.getContent().trim());

    // Gửi kèm BẢN DỊCH (bản đầu tiên) nếu có — hữu ích nhất với thơ Hán.
    if (p.getTranslations() != null && !p.getTranslations().isEmpty()) {
      var t = p.getTranslations().get(0);
      if (t.getContent() != null && !t.getContent().isBlank()) {
        sb.append("\n\n— Bản dịch");
        if (t.getTranslator() != null && !t.getTranslator().isBlank()) {
          sb.append(" (").append(t.getTranslator()).append(")");
        }
        sb.append(" —\n").append(t.getContent().trim());
      }
    }
    return cap(sb.toString());
  }

  private String cap(String s) {
    return s.length() > MAX ? s.substring(0, MAX).trim() + "\n…" : s;
  }

  private static List<String> one(String s) {
    return List.of(s);
  }

  private String help() {
    return "📜 Bot Thi Đàn — các lệnh:\n\n"
      + "• /tho — ngẫu nhiên 1 bài thơ (kèm bản dịch nếu có)\n"
      + "• /tacgia <tên> — liệt kê thơ của tác giả để CHỌN\n"
      + "     ví dụ: /tacgia Nguyễn Du → rồi trả lời số để đọc\n"
      + "• 'tiếp' — xem thêm bài của tác giả đang liệt kê\n"
      + "• /help — xem lại hướng dẫn này\n\n"
      + "Mẹo: gõ không dấu vẫn được (vd: /tacgia nguyen du).";
  }

  /** Bỏ dấu tiếng Việt + đ→d để so khớp lệnh không phụ thuộc dấu. */
  private static String stripAccents(String s) {
    String n = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
    return n.replace('đ', 'd').replace('Đ', 'D');
  }
}
