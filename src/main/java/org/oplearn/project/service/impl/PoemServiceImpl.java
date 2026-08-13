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
  public PageResponse<PoemResponse> list(String keyword, Long genreId, String era, String language, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);
    String eraFilter = StringUtils.hasText(era) ? era.trim() : null;
    String languageFilter = StringUtils.hasText(language) ? language.trim() : null;

    Page<PoemResponse> poems = StringUtils.hasText(keyword)
      ? repository.search(keyword.trim(), genreId, eraFilter, languageFilter, pageable).map(PoemServiceImpl::toResponse)
      : repository.findAllByIsDeletedFalse(genreId, eraFilter, languageFilter, pageable);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  @Override
  public java.util.List<String> listEras() {
    return repository.findDistinctEras();
  }

  @Override
  public java.util.List<org.oplearn.project.dto.response.FacetItemResponse> facets(String language, String era, Long genreId) {
    String lang = StringUtils.hasText(language) ? language.trim() : null;
    String er = StringUtils.hasText(era) ? era.trim() : null;

    // Chọn cấp dựa trên đường dẫn đã cho: chưa có gì → ngôn ngữ; có ngôn ngữ → thời kỳ;
    // có thời kỳ → thể thơ; có thể thơ → tác giả.
    java.util.List<PoemRepository.FacetCount> rows;
    if (lang == null) {
      rows = repository.facetLanguages();
    } else if (er == null) {
      rows = repository.facetEras(lang);
    } else if (genreId == null) {
      rows = repository.facetGenres(lang, er);
    } else {
      rows = repository.facetAuthors(lang, er, genreId);
    }

    return rows.stream()
      .map(r -> new org.oplearn.project.dto.response.FacetItemResponse(
        r.getId(), r.getLabel(), r.getCount() == null ? 0L : r.getCount()))
      .toList();
  }

  @Override
  public PageResponse<PoemResponse> browse(String language, String era, Long genreId, Long authorId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);
    String lang = StringUtils.hasText(language) ? language.trim() : null;
    String er = StringUtils.hasText(era) ? era.trim() : null;

    Page<PoemResponse> poems = repository.browse(lang, er, genreId, authorId, pageable)
      .map(PoemServiceImpl::toResponse);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  @Override
  public java.util.List<String> listLanguages() {
    return repository.findDistinctLanguages();
  }

  private static PoemResponse toResponse(PoemRepository.PoemSearchRow row) {
    return new PoemResponse(
      row.getId(), row.getName(), row.getDescription(), row.getYear(), row.getContent(),
      row.getTransliteration(), row.getTranslation(), row.getLanguage(), row.getEra(),
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
    // 2 bước: bốc id ngẫu nhiên (sort nhẹ trên id) rồi mới nạp nội dung —
    // tránh ORDER BY random() kéo + sort cả cột content trên toàn bảng.
    java.util.List<Long> ids = repository.findRandomIds(10);
    if (ids.isEmpty()) {
      return PageResponse.of(java.util.List.of(), 0);
    }

    java.util.List<PoemResponse> poems = repository.findResponsesByIds(ids).stream()
      .map(PoemResponse::fromSummary)
      .toList();

    return PageResponse.of(poems, poems.size());
  }

  public Poem getAvailablePoemAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemNotFoundException::new);
  }
}
