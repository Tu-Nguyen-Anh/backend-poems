package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.dto.response.PoemTranslationResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.exception.PoemNotFoundException;
import org.oplearn.project.exception.RandomPoemBadRequestException;
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

import java.util.List;

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
    return cached("eras", repository::findDistinctEras);
  }

  @Override
  public java.util.List<org.oplearn.project.dto.response.FacetItemResponse> facets(String language, String era, Long genreId) {
    String lang = StringUtils.hasText(language) ? language.trim() : null;
    String er = StringUtils.hasText(era) ? era.trim() : null;

    // Chọn cấp dựa trên đường dẫn đã cho: chưa có gì → ngôn ngữ; có ngôn ngữ → thời kỳ;
    // có thời kỳ → thể thơ; có thể thơ → tác giả. Cache theo đường dẫn (ít khi đổi).
    return cached("facets|" + lang + "|" + er + "|" + genreId, () -> {
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
    });
  }

  @Override
  public PageResponse<PoemResponse> browse(String language, String era, Long genreId, Long authorId, String keyword, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);
    String lang = StringUtils.hasText(language) ? language.trim() : null;
    String er = StringUtils.hasText(era) ? era.trim() : null;
    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

    Page<PoemResponse> poems = repository.browse(lang, er, genreId, authorId, kw, pageable)
      .map(PoemServiceImpl::toResponse);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }

  @Override
  public java.util.List<String> listLanguages() {
    return cached("langs", repository::findDistinctLanguages);
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

    // Dịch nghĩa (văn xuôi) + authorId — lấy từ entity vì projection không có
    repository.findByIdAndIsDeletedFalse(id)
      .ifPresent(poem -> {
        response.setMeaning(poem.getMeaning());
        response.setAuthorId(poem.getAuthorId());
      });

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

  // Tổng số bài (cho banner trang chủ) đổi rất chậm nhưng count(*) là seq scan
  // toàn bảng — cache lại, làm mới mỗi 60s thay vì đếm mỗi lần tải trang chủ.
  private static final long ACTIVE_COUNT_TTL_MS = 60_000;
  private volatile long cachedActiveCount = -1;
  private volatile long cachedActiveCountAt = 0;

  private long activeCount() {
    long now = System.currentTimeMillis();
    if (cachedActiveCount < 0 || now - cachedActiveCountAt > ACTIVE_COUNT_TTL_MS) {
      cachedActiveCount = repository.countActive();
      cachedActiveCountAt = now;
    }
    return cachedActiveCount;
  }

  // Thống kê trang chủ: JOIN + FILTER toàn bảng, khá nặng nhưng gần như bất biến
  // giữa các lần import → cache 10 phút.
  private static final long STATS_TTL_MS = 10 * 60_000;
  private volatile org.oplearn.project.dto.response.StatsResponse cachedStats;
  private volatile long cachedStatsAt = 0;

  public org.oplearn.project.dto.response.StatsResponse getStats() {
    long now = System.currentTimeMillis();
    if (cachedStats == null || now - cachedStatsAt > STATS_TTL_MS) {
      PoemRepository.StatsRow r = repository.getStats();
      cachedStats = new org.oplearn.project.dto.response.StatsResponse(
        r.getTotalPoems(), r.getTotalAuthors(), r.getTotalCountries(),
        r.getVietCount(), r.getHanCount(), r.getForeignCount());
      cachedStatsAt = now;
    }
    return cachedStats;
  }

  // Cache TTL cho cây duyệt facet + danh sách thời kỳ/ngôn ngữ: đều là GROUP BY
  // toàn bảng poems, chỉ đổi khi import lại nên cache 30 phút. Số key hữu hạn
  // (theo taxonomy: ngôn ngữ × thời kỳ × thể thơ) nên map không phình vô hạn.
  private static final long FACET_TTL_MS = 30 * 60_000;

  private record CacheEntry(long at, Object data) {
  }

  private final java.util.concurrent.ConcurrentHashMap<String, CacheEntry> facetCache =
    new java.util.concurrent.ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  private <T> T cached(String key, java.util.function.Supplier<T> loader) {
    long now = System.currentTimeMillis();
    CacheEntry e = facetCache.get(key);
    if (e != null && now - e.at() < FACET_TTL_MS) {
      return (T) e.data();
    }
    T value = loader.get();
    facetCache.put(key, new CacheEntry(now, value));
    return value;
  }

  public PageResponse<PoemResponse> listPoemLatest(int size, int page) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    // Chỉ nạp đúng `size` bài mới nhất (index scan trên created_at) — không count/join thừa.
    java.util.List<PoemResponse> poems = repository.findLatest(pageable).stream()
      .map(PoemResponse::fromSummary)
      .toList();

    return PageResponse.of(poems, (int) activeCount());
  }

  public PageResponse<PoemResponse> randomPersonalized(List<Long> authorIds, List<Long> genreIds, List<String> eras) {

    if (
      (authorIds != null && authorIds.size() > 3)
        || (genreIds != null && genreIds.size() > 3)
        || (eras != null && eras.size() > 3)
    ) {
      throw new RandomPoemBadRequestException();
    }


    Long[] authorArray = (authorIds != null && !authorIds.isEmpty()) ? authorIds.toArray(new Long[0]) : null;
    Long[] genreArray = (genreIds != null && !genreIds.isEmpty()) ? genreIds.toArray(new Long[0]) : null;
    String[] eraArray = (eras != null && !eras.isEmpty()) ? eras.toArray(new String[0]) : null;

    boolean hasPreferences = (authorArray != null || genreArray != null || eraArray != null);

    List<Long> ids = hasPreferences
      ? repository.findPersonalizedRandomIds(authorArray, genreArray, eraArray, 10)
      : repository.findRandomIds(10);


    if (ids.isEmpty()) {
      ids = repository.findRandomIds(10);
    }

    if (ids.isEmpty()) {
      return PageResponse.of(List.of(), 0);
    }


    List<PoemResponse> poems = repository.findResponsesByIds(ids).stream()
      .map(PoemResponse::fromSummary)
      .toList();

    return PageResponse.of(poems, poems.size());
  }

  public Poem getAvailablePoemAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemNotFoundException::new);
  }
}
