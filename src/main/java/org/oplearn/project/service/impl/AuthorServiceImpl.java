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

    Pageable pageable = isAll ? Pageable.unpaged() : PageRequest.of(page, size);

    Page<Author> authors = StringUtils.hasText(keyword)
      ? repository.search(keyword, pageable)
      : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
      authors.map(AuthorResponse::from).getContent(),
      (int) authors.getTotalElements()
    );
  }

  public PageResponse<AuthorResponse> listTopByPoemCount(int size, int page) {
    log.info("(Service) list top authors by poem count");

    Page<AuthorResponse> authors = repository.findTopByPoemCount(PageRequest.of(page, size));

    return PageResponse.of(authors.getContent(), (int) authors.getTotalElements());
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
