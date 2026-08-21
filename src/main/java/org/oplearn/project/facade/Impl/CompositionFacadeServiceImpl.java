package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PoemCompositionRequest;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemCompositionResponse;
import org.oplearn.project.entity.Genre;
import org.oplearn.project.entity.PoemComposition;
import org.oplearn.project.entity.User;
import org.oplearn.project.enums.PoemCompositionStatus;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.facade.CompositionFacadeService;
import org.oplearn.project.service.CompositionService;
import org.oplearn.project.service.GenreService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompositionFacadeServiceImpl implements CompositionFacadeService {
  private final CompositionService compositionService;
  private final GenreService genreService;
  private final UserService userService;

  @Override
  public PoemCompositionResponse create(PoemCompositionRequest request) {
    log.info("(facade) create composition");

    Genre genre = genreService.getAvailableGenreAndThrow(request.getGenreId());

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    PoemComposition poemComposition = PoemComposition.builder()
      .userId(currentUser.getId())
      .content(request.getContent())
      .title(request.getTitle())
      .penName(request.getPenName())
      .genreId(genre.getId())
      .status(request.getStatus() != null ? request.getStatus() : PoemCompositionStatus.PUBLISHED)
      .build();

    PoemComposition savedPoemComposition = compositionService.create(poemComposition);

    return new PoemCompositionResponse(
      savedPoemComposition.getId(),
      savedPoemComposition.getUserId(),
      username,
      savedPoemComposition.getContent(),
      savedPoemComposition.getPenName(),
      savedPoemComposition.getTitle(),
      savedPoemComposition.getGenreId(),
      genre.getName(),
      savedPoemComposition.getStatus()
    );
  }

  @Override
  public PoemCompositionResponse update(PoemCompositionRequest request, Long id) {
    log.info("(facade) update composition");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    PoemCompositionResponse existingComposition = compositionService.detail(id);

    if (!currentUser.getId().equals(existingComposition.getUserId())) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    Genre genre = genreService.getAvailableGenreAndThrow(request.getGenreId());

    PoemComposition poemComposition = PoemComposition.builder()
      .title(request.getTitle())
      .content(request.getContent())
      .penName(request.getPenName())
      .genreId(genre.getId())
      .status(request.getStatus())
      .build();

    PoemComposition updatedPoemComposition = compositionService.update(poemComposition, id);

    return new PoemCompositionResponse(
      updatedPoemComposition.getId(),
      updatedPoemComposition.getUserId(),
      username,
      updatedPoemComposition.getContent(),
      updatedPoemComposition.getPenName(),
      updatedPoemComposition.getTitle(),
      updatedPoemComposition.getGenreId(),
      genre.getName(),
      updatedPoemComposition.getStatus()
    );
  }

  @Override
  public void delete(Long id) {
    log.info("(facade) delete composition");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);
    PoemCompositionResponse existingComposition = compositionService.detail(id);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(existingComposition.getUserId())) {
      log.warn("(delete) user not authorized");
      throw new UserUnauthorizedException();
    }

    compositionService.delete(id);
  }

  @Override
  public PoemCompositionResponse detail(Long id) {
    log.info("(facade) get composition by id = {}", id);

    PoemCompositionResponse composition = compositionService.detail(id);

    if (composition.getStatus() == PoemCompositionStatus.PUBLISHED) {
      return composition;
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
      throw new UserUnauthorizedException();
    }

    String username = authentication.getName();
    User currentUser = userService.getUsernameOrThrow(username);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !Objects.equals(currentUser.getId(), composition.getUserId())) {
      log.warn("(detail) user id: {} is not authorized to view composition id: {}", currentUser.getId(), id);
      throw new UserUnauthorizedException();
    }

    return composition;
  }

  @Override
  public PageResponse<PoemCompositionResponse> listByUserId(Long userId, int size, int page) {
    log.info("(facade) list composition by user id = {}", userId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    boolean isOwner = false;
    boolean isAdmin = false;

    if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
      String username = authentication.getName();
      User currentUser = userService.getUsernameOrThrow(username);
      isOwner = Objects.equals(currentUser.getId(), userId);
      isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    boolean canViewAll = isAdmin || isOwner;

    return compositionService.listByUserId(userId, canViewAll, size, page);
  }
}
