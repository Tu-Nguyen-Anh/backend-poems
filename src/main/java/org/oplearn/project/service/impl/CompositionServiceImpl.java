package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemCompositionResponse;
import org.oplearn.project.entity.PoemComposition;
import org.oplearn.project.exception.PoemCompositionNotFoundException;
import org.oplearn.project.exception.base.BadRequestException;
import org.oplearn.project.repository.PoemCompositionRepository;
import org.oplearn.project.service.CompositionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class CompositionServiceImpl implements CompositionService {
  private final PoemCompositionRepository repository;

  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_24H_MILLIS = 24 * 60 * 60 * 1000L;
  private final ConcurrentHashMap<Long, List<Long>> userRequestTimestamps = new ConcurrentHashMap<>();

  @Override
  public PoemComposition create(PoemComposition poemComposition) {
    log.info("(service) create poem composition");

    Long userId = poemComposition.getUserId();
    Long now = System.currentTimeMillis();

    userRequestTimestamps.compute(userId, (id , timestamps) -> {
      if(timestamps == null) {
        timestamps = new java.util.ArrayList<>();
      }

      timestamps.removeIf(t -> now - t > WINDOW_24H_MILLIS);

      if(timestamps.size() >= MAX_REQUESTS) {
        log.warn("(create) user {} reached limit {} requests in 24 hours", userId, MAX_REQUESTS);
        throw new BadRequestException("Too many requests");
      }
      timestamps.add(now);
      return timestamps;
    });

    latestCache.clear();

    return repository.save(poemComposition);
  }

  @Override
  public PoemComposition update(PoemComposition poemComposition, Long id) {
    log.info("(service) update poem composition with id = {}", id);

    PoemComposition existingPoemComposition = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemCompositionNotFoundException::new);

    existingPoemComposition.setContent(poemComposition.getContent());
    existingPoemComposition.setTitle(poemComposition.getTitle());
    existingPoemComposition.setPenName(poemComposition.getPenName());
    existingPoemComposition.setGenreId(poemComposition.getGenreId());
    if (poemComposition.getStatus() != null) {
      existingPoemComposition.setStatus(poemComposition.getStatus());
    }

    latestCache.clear();

    return repository.save(existingPoemComposition);
  }

  @Override
  public void delete(Long id) {
    log.info("(service) delete id = {}", id);

    repository.SoftDeleteById(id);

    latestCache.clear();
  }

  @Override
  public PoemCompositionResponse detail(Long id) {
    return repository.findByIdAndReturnResponse(id)
      .orElseThrow(PoemCompositionNotFoundException::new);
  }

  @Override
  public PageResponse<PoemCompositionResponse> listByUserId(Long userId, boolean canViewAll, int size, int page) {
    log.info("(service) list poem composition by user id");

    Pageable pageable = PageRequest.of(page, size);

    Page<PoemCompositionResponse> composition = canViewAll
      ? repository.findByUserId(userId, pageable)
      : repository.findPublishedByUserId(userId, pageable);

    return PageResponse.of(
      composition.map(PoemCompositionResponse::fromSummary).getContent(),
      (int) composition.getTotalElements()
    );
  }

  @Override
  public PageResponse<PoemCompositionResponse> search(String keyword, Long genreId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);

    String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;

    Page<PoemCompositionResponse> composition = repository.search(genreId, kw, pageable)
      .map(CompositionServiceImpl::toResponse);

    return PageResponse.of(
      composition.getContent(),
      (int) composition.getTotalElements()
    );
  }

  @Override
  public PageResponse<PoemCompositionResponse> random() {
    List<Long> ids = repository.findRandomIds(10);

    List<PoemCompositionResponse> composition = repository.findResponsesByIds(ids).stream()
      .map(PoemCompositionResponse::fromSummary)
      .toList();

    return PageResponse.of(composition, composition.size());
  }

  @Override
  public PageResponse<PoemCompositionResponse> ListCompositionLastest(int size, int page) {
    String cacheKey = "latest|" + page + "|" + size;

    return cached(cacheKey, LATEST_TTL_MS, () -> {
      Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

      List<PoemCompositionResponse> composition = repository.findLastest(pageable)
        .stream()
        .map(PoemCompositionResponse::fromSummary)
        .toList();

      return PageResponse.of(composition, composition.size());
    });
  }

  private static PoemCompositionResponse toResponse(PoemCompositionRepository.PoemCompositionRow row) {
    return new PoemCompositionResponse(
      row.getId(),
      row.getUserId(),
      row.getUsername(),
      row.getContent(),
      row.getPenName(),
      row.getTitle(),
      row.getGenreId(),
      row.getGenreName(),
      row.getStatus()
    );
  }

  private static final long LATEST_TTL_MS = 60_000;

  private record CacheEntry(long at, Object data) {
  }

  private final ConcurrentHashMap<String, CacheEntry> latestCache = new ConcurrentHashMap<>();

  @SuppressWarnings("unchecked")
  private <T> T cached(String key, long ttlMs, java.util.function.Supplier<T> loader) {
    long now = System.currentTimeMillis();
    CacheEntry e = latestCache.get(key);
    if (e != null && now - e.at() < ttlMs) {
      return (T) e.data();
    }
    T value = loader.get();
    latestCache.put(key, new CacheEntry(now, value));
    return value;
  }

  @Override
  public PoemComposition getAvailableCompositionAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(PoemCompositionNotFoundException::new);
  }
}
