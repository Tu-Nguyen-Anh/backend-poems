package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.PoemTranslationResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.exception.PoemNotFoundException;
import org.oplearn.project.repository.PoemRepository;
import org.oplearn.project.repository.PoemTranslationRepository;
import org.oplearn.project.service.PoemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoemServiceImpl implements PoemService {
  private final PoemRepository repository;
  private final PoemTranslationRepository translationRepository;

  @Override
  public PageResponse<PoemResponse> list(String keyword, Long genreId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);

    Page<PoemResponse> poems = StringUtils.hasText(keyword)
      ? repository.search(keyword.trim(), genreId, pageable).map(PoemServiceImpl::toResponse)
      : repository.findAllByIsDeletedFalse(genreId, pageable);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  private static PoemResponse toResponse(PoemRepository.PoemSearchRow row) {
    return new PoemResponse(
      row.getId(), row.getName(), row.getDescription(), row.getYear(), row.getContent(),
      row.getTransliteration(), row.getTranslation(), row.getLanguage(),
      row.getGenreName(), row.getAuthorName()
    );
  }

  @Override
  public PoemResponse detail(Long id) {
    PoemResponse response = repository.findByIdAndReturnResponse(id)
      .orElseThrow(PoemNotFoundException::new);

    // Dịch nghĩa (văn xuôi) — lấy từ entity vì projection không có
    repository.findByIdAndIsDeletedFalse(id)
      .ifPresent(poem -> response.setMeaning(poem.getMeaning()));

    // Nhiều bản dịch thơ
    response.setTranslations(
      translationRepository.findByPoemIdAndIsDeletedFalseOrderBySortOrderAsc(id)
        .stream()
        .map(PoemTranslationResponse::from)
        .toList()
    );

    return response;
  }

  @Transactional
  public void delete(Long id) {
    if (repository.findByIdAndIsDeletedFalse(id).isEmpty()) {
      throw new PoemNotFoundException();
    }
    repository.softDeleteById(id);
  }

  public Poem create(Poem poem) {
    log.info("(service) create poem");

    return repository.save(poem);
  }

  public Poem update(Long id, Poem poem) {
    log.info("(service) update poem");

    Poem existingPoem = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemNotFoundException::new);

    existingPoem.setName(poem.getName());
    existingPoem.setDescription(poem.getDescription());
    existingPoem.setYear(poem.getYear());
    existingPoem.setContent(poem.getContent());
    existingPoem.setTransliteration(poem.getTransliteration());
    existingPoem.setTranslation(poem.getTranslation());
    existingPoem.setLanguage(poem.getLanguage());
    existingPoem.setGenreId(poem.getGenreId());
    existingPoem.setAuthorId(poem.getAuthorId());

    return repository.save(existingPoem);
  }

  public PageResponse<PoemResponse> listPoemLatest(int size, int page) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    Page<PoemResponse> poems = repository.findLatest(pageable);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  public PageResponse<PoemResponse> random() {
    Pageable pageable = PageRequest.of(0, 10);

    Page<PoemResponse> poems = repository.findRandomPoem(pageable);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  public Poem getAvailablePoemAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemNotFoundException::new);
  }
}
