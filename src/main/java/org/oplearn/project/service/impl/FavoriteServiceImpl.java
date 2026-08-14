package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Favorite;
import org.oplearn.project.repository.FavoriteRepository;
import org.oplearn.project.service.FavoriteService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {
  private final FavoriteRepository repository;

  public boolean isFavorited(Long userId, Long poemId) {
    return repository.existsByUserIdAndPoemId(userId, poemId);
  }

  public void add(Long userId, Long poemId) {
    // Idempotent: đã thích thì thôi, tránh vỡ ràng buộc UNIQUE(user_id, poem_id).
    if (repository.existsByUserIdAndPoemId(userId, poemId)) {
      return;
    }
    repository.save(Favorite.builder().userId(userId).poemId(poemId).build());
  }

  public void remove(Long userId, Long poemId) {
    repository.deleteByUserIdAndPoemId(userId, poemId);
  }

  public PageResponse<PoemResponse> listByUser(Long userId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);
    Page<PoemResponse> poems = repository.findFavoritePoemsByUserId(userId, pageable);

    return PageResponse.of(
      poems.map(PoemResponse::fromSummary).getContent(),
      (int) poems.getTotalElements()
    );
  }
}
