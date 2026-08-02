package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.GenreRequest;
import org.oplearn.project.dto.response.GenreResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Genre;
import org.oplearn.project.exception.GenreNameAlreadyExistedException;
import org.oplearn.project.exception.GenreNotFoundException;
import org.oplearn.project.repository.GenreRepository;
import org.oplearn.project.service.GenreService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class GenreServiceImpl implements GenreService {
  private final GenreRepository repository;

  public GenreResponse create(GenreRequest request) {
    log.info("(service) create genre");

    if(repository.existsByName(request.getName())) {
      throw new GenreNameAlreadyExistedException();
    }

    Genre genre = Genre.builder()
      .name(request.getName())
      .build();

    return GenreResponse.from(repository.save(genre));
  }

  public GenreResponse update(GenreRequest request, Long id) {
    log.info("(service) update genre with id: {}", id);

    Genre genre = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(GenreNotFoundException::new);

    genre.setName(request.getName());

    return GenreResponse.from(repository.save(genre));
  }

  public void delete(Long id) {
    log.info("(service) delete genre with id: {}", id);

    Genre genre = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(GenreNotFoundException::new);

    repository.deleteById(id);
  }

  public GenreResponse detail(Long id) {
    log.info("(service) detail genre with id: {}", id);

    return repository.findByIdAndIsDeletedFalse(id)
      .map(GenreResponse::from)
      .orElseThrow(GenreNotFoundException::new);
  }

  public PageResponse<GenreResponse> list(String keyword, int size, int page, boolean isAll) {
    log.info("(service) filter genre with keyword: {}, size: {}, page: {}, isAll: {}", keyword, size, page, isAll);

    Pageable pageable = isAll ? Pageable.unpaged() : PageRequest.of(page, size);

    Page<Genre> genre = StringUtils.hasText(keyword)
      ? repository.search(keyword , pageable)
      : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
      genre.map(GenreResponse::from).getContent(),
      (int) genre.getTotalElements()
    );
  }

  public PageResponse<PoemResponse> listPoemByGenreId(Long id, int size, int page) {
    log.info("(service) list poem by genre id: {}, size: {}, page: {}", id, size, page);

    if(!repository.existsByIdAndIsDeletedFalse(id)) {
      throw new GenreNotFoundException();
    }

    Pageable pageable = PageRequest.of(page, size);

    Page<PoemResponse> poemPage = repository.findPoemByGenreId(id , pageable);

    return PageResponse.of(
      poemPage.getContent(),
      (int) poemPage.getTotalElements()
    );
  }
}
