package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.AuthorRequest;
import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Author;
import org.oplearn.project.exception.AuthorNameAlreadyExistedException;
import org.oplearn.project.exception.AuthorNotFoundException;
import org.oplearn.project.repository.AuthorRepository;
import org.oplearn.project.repository.PoemRepository;
import org.oplearn.project.repository.StoryRepository;
import org.oplearn.project.service.AuthorService;
import static org.oplearn.project.constants.OpLearnConstants.CacheConstant.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorServiceImpl implements AuthorService {
  private final AuthorRepository repository;
  private final PoemRepository poemRepository;
  private final StoryRepository storyRepository;

  @CacheEvict(value = CACHE_AUTHORS_PAGE, allEntries = true)
  public AuthorResponse create(AuthorRequest request) {
    log.info("(Service) create author");

    if (repository.existsByNameAndIsDeletedFalse(request.getName())) {
      throw new AuthorNameAlreadyExistedException();
    }

    Author author = Author.builder()
      .name(request.getName())
      .birthYear(request.getBirthYear())
      .achievement(request.getAchievement())
      .hometown(request.getHometown())
      .build();

    return AuthorResponse.from(repository.save(author));
  }

  @CacheEvict(value = CACHE_AUTHORS_PAGE, allEntries = true)
  public AuthorResponse update(AuthorRequest request, Long id) {
    log.info("(Service) update author");

    Author author = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(AuthorNotFoundException::new);

    if (!author.getName().equals(request.getName())
      && repository.existsByNameAndIsDeletedFalse(request.getName())) {
      throw new AuthorNameAlreadyExistedException();
    }

    author.setName(request.getName());
    author.setBirthYear(request.getBirthYear());
    author.setAchievement(request.getAchievement());
    author.setHometown(request.getHometown());

    return AuthorResponse.from(repository.save(author));
  }

  @CacheEvict(value = CACHE_AUTHORS_PAGE, allEntries = true)
  public void delete(Long id) {
    log.info("(Service) delete author");

    if (!repository.existsByIdAndIsDeletedFalse(id)) {
      throw new AuthorNotFoundException();
    }

    repository.softDeleteById(id);
  }

  public AuthorResponse detail(Long id) {
    log.info("(Service) detail author");

    Author author = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(AuthorNotFoundException::new);
    AuthorResponse response = AuthorResponse.from(author);
    response.setPoemCount(poemRepository.countByAuthorIdAndIsDeletedFalse(id));
    response.setStoryCount(storyRepository.countByAuthorIdAndIsDeletedFalse(id));
    return response;
  }

  @Cacheable(
    value = CACHE_AUTHORS_PAGE,
    key = "'list:' + #keyword + ':' + #type + ':' + #size + ':' + #page + ':' + #isAll",
    unless = "#result == null"
  )
  public PageResponse<AuthorResponse> list(String keyword, String type, int size, int page, boolean isAll) {
    log.debug("(Service) list author keyword: {}, type: {}", keyword, type);

    Pageable pageable = isAll
      ? PageRequest.of(0, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_ALL_SIZE)
      : PageRequest.of(page, size);

    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
    String ty = ("poem".equals(type) || "story".equals(type)) ? type : null;

    Page<AuthorRepository.AuthorListRow> authors = repository.listWithCounts(kw, ty, pageable);

    return PageResponse.of(
      authors.map(AuthorServiceImpl::toResponse).getContent(),
      (int) authors.getTotalElements()
    );
  }

  private static AuthorResponse toResponse(AuthorRepository.AuthorListRow row) {
    AuthorResponse r = new AuthorResponse(
      row.getId(), row.getName(), row.getBirthYear(), row.getAchievement(), row.getHometown()
    );
    r.setAvatarUrl(row.getAvatarUrl());
    r.setAvatarLocal(row.getAvatarLocal());
    r.setBio(row.getBio());
    r.setCountry(row.getCountry());
    r.setCountryId(row.getCountryId());
    r.setPoemCount(row.getPoemCount());
    r.setStoryCount(row.getStoryCount());
    return r;
  }

  /** Cache in-memory cho widget "tác giả tiêu biểu" trang chủ — query GROUP BY
   *  đếm toàn bảng poems khá nặng mà kết quả gần như không đổi giữa các lần import. */
  private static final long TOP_AUTHORS_TTL_MS = 10 * 60 * 1000L;
  private final java.util.concurrent.atomic.AtomicReference<CachedTopAuthors> topAuthorsCache =
    new java.util.concurrent.atomic.AtomicReference<>();

  private record CachedTopAuthors(String key, long at, PageResponse<AuthorResponse> data) {
  }

  public PageResponse<AuthorResponse> listTopByPoemCount(int size, int page) {
    log.info("(Service) list top authors by poem count");

    String key = size + ":" + page;
    CachedTopAuthors hit = topAuthorsCache.get();
    if (hit != null && hit.key().equals(key)
        && System.currentTimeMillis() - hit.at() < TOP_AUTHORS_TTL_MS) {
      return hit.data();
    }

    Page<AuthorResponse> authors = repository.findTopByPoemCount(PageRequest.of(page, size));
    PageResponse<AuthorResponse> result =
      PageResponse.of(authors.getContent(), (int) authors.getTotalElements());

    topAuthorsCache.set(new CachedTopAuthors(key, System.currentTimeMillis(), result));
    return result;
  }

  public PageResponse<PoemResponse> listPoemByAuthorId(Long id, int size, int page) {
    log.info("(Service) list poem by author id");

    if (!repository.existsByIdAndIsDeletedFalse(id)) {
      throw new AuthorNotFoundException();
    }

    Pageable pageable = PageRequest.of(page, size);

    Page<PoemResponse> poemPage = repository.findPoemByAuthorId(id, pageable);

    return PageResponse.of(
      poemPage.getContent(),
      (int) poemPage.getTotalElements()
    );
  }

  public Author getAvailableAuthorAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(AuthorNotFoundException::new);
  }

  /** Danh sách tác giả tiêu biểu ghim tay, khớp theo tên bỏ dấu (viết thường). */
  private static final java.util.List<String> FEATURED_KEYS = java.util.List.of(
    "nguyen du", "ho xuan huong", "nguyen trai", "nguyen binh khiem", "le thanh tong",
    "nguyen cong tru", "cao ba quat", "nguyen khuyen", "tran te xuong", "tan da",
    "xuan dieu", "han mac tu", "huy can", "che lan vien", "nguyen binh", "to huu",
    "xuan quynh", "te hanh", "nguyen dinh thi", "bui giang"
  );

  public java.util.List<AuthorResponse> featured() {
    log.info("(Service) list featured authors (pinned)");

    java.util.List<AuthorResponse> rows = repository.findByUnaccentNames(FEATURED_KEYS);

    // Nhiều tác giả có thể trùng tên bỏ dấu → giữ người nhiều bài nhất cho mỗi key.
    java.util.Map<String, AuthorResponse> best = new java.util.HashMap<>();
    for (AuthorResponse r : rows) {
      String key = unaccentLower(r.getName());
      AuthorResponse cur = best.get(key);
      long rc = r.getPoemCount() == null ? 0 : r.getPoemCount();
      long cc = (cur == null || cur.getPoemCount() == null) ? -1 : cur.getPoemCount();
      if (cur == null || rc > cc) {
        best.put(key, r);
      }
    }

    java.util.List<AuthorResponse> out = new java.util.ArrayList<>();
    for (String key : FEATURED_KEYS) {
      AuthorResponse r = best.get(key);
      if (r != null) out.add(r);
    }
    return out;
  }

  /** Bỏ dấu tiếng Việt + viết thường, khớp cách f_unaccent(lower(...)) trong DB. */
  private static String unaccentLower(String s) {
    if (s == null) return "";
    String n = java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
      .replaceAll("\\p{M}+", "");
    n = n.replace('đ', 'd').replace('Đ', 'D');
    return n.toLowerCase().trim();
  }
}
