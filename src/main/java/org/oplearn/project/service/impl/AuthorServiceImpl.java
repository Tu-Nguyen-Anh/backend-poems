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
import org.oplearn.project.service.AuthorService;
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

  public void delete(Long id) {
    log.info("(Service) delete author");

    if (!repository.existsByIdAndIsDeletedFalse(id)) {
      throw new AuthorNotFoundException();
    }

    repository.softDeleteById(id);
  }

  public AuthorResponse detail(Long id) {
    log.info("(Service) detail author");

    return repository.findByIdAndIsDeletedFalse(id)
      .map(AuthorResponse::from)
      .orElseThrow(AuthorNotFoundException::new);
  }

  public PageResponse<AuthorResponse> list(String keyword, int size, int page, boolean isAll) {
    log.info("Service) list author");

    Pageable pageable = isAll
      ? PageRequest.of(0, org.oplearn.project.constants.OpLearnConstants.VariableConstant.MAX_ALL_SIZE)
      : PageRequest.of(page, size);

    Page<Author> authors = StringUtils.hasText(keyword)
      ? repository.search(keyword, pageable)
      : repository.findAllOrderByCountryAndPoemCount(pageable);

    return PageResponse.of(
      authors.map(AuthorResponse::from).getContent(),
      (int) authors.getTotalElements()
    );
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
}
