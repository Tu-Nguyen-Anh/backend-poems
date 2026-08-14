package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.entity.User;
import org.oplearn.project.facade.FavoriteFacadeService;
import org.oplearn.project.service.FavoriteService;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FavoriteFacadeServiceImpl implements FavoriteFacadeService {
  private final FavoriteService favoriteService;
  private final UserService userService;
  private final PoemService poemService;

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.getUsernameOrThrow(authentication.getName());
  }

  public boolean add(Long poemId) {
    log.info("(facade) add favorite poem id = {}", poemId);
    User user = currentUser();
    Poem poem = poemService.getAvailablePoemAndThrow(poemId);
    favoriteService.add(user.getId(), poem.getId());
    return true;
  }

  public boolean remove(Long poemId) {
    log.info("(facade) remove favorite poem id = {}", poemId);
    User user = currentUser();
    favoriteService.remove(user.getId(), poemId);
    return false;
  }

  public boolean status(Long poemId) {
    User user = currentUser();
    return favoriteService.isFavorited(user.getId(), poemId);
  }

  public PageResponse<PoemResponse> myFavorites(int size, int page) {
    User user = currentUser();
    return favoriteService.listByUser(user.getId(), size, page);
  }
}
