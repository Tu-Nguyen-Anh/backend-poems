package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PoemRequest;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Author;
import org.oplearn.project.entity.Genre;
import org.oplearn.project.entity.Poem;
import jakarta.servlet.http.HttpServletRequest;
import org.oplearn.project.entity.User;
import org.oplearn.project.facade.PoemFacadeService;
import org.oplearn.project.service.*;
import org.oplearn.project.utils.ClientIpUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoemFacadeServiceImpl implements PoemFacadeService {
  private final PoemService poemService;
  private final AuthorService authorService;
  private final GenreService genreService;
  private final StatisticService statisticService;
  private final UserService userService;
  private final NotificationService notificationService;

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.getUsernameOrThrow(authentication.getName());
  }

  public PoemResponse create(PoemRequest request) {
    log.info("(facade) create poem");

    Genre genre = request.getGenreId() != null
      ? genreService.getAvailableGenreAndThrow(request.getGenreId())
      : null;

    Author author = request.getAuthorId() != null
      ? authorService.getAvailableAuthorAndThrow(request.getAuthorId())
      : null;

    Poem poem = Poem.builder()
      .name(request.getName())
      .description(request.getDescription())
      .year(request.getYear())
      .content(request.getContent())
      .transliteration(request.getTransliteration())
      .translation(request.getTranslation())
      .language(request.getLanguage())
      .era(request.getEra())
      .genreId(genre != null ? genre.getId() : null)
      .authorId(author != null ? author.getId() : null)
      .build();

    Poem savedPoem = poemService.create(poem);

    User user = currentUser();

    notificationService.createAndSendCreatePoemNotification(savedPoem, user);

    return PoemResponse.from(
      savedPoem,
      genre != null ? genre.getName() : null,
      author != null ? author.getName() : null
    );
  }


  public PoemResponse update(PoemRequest request, Long id) {
    log.info("(facade) update poem");

    Genre genre = request.getGenreId() != null
      ? genreService.getAvailableGenreAndThrow(request.getGenreId())
      : null;

    Author author = request.getAuthorId() != null
      ? authorService.getAvailableAuthorAndThrow(request.getAuthorId())
      : null;

    Poem poem = Poem.builder()
      .name(request.getName())
      .description(request.getDescription())
      .year(request.getYear())
      .content(request.getContent())
      .transliteration(request.getTransliteration())
      .translation(request.getTranslation())
      .language(request.getLanguage())
      .era(request.getEra())
      .genreId(genre != null ? genre.getId() : null)
      .authorId(author != null ? author.getId() : null)
      .build();

    Poem updatedPoem = poemService.update(id, poem);

    User user = currentUser();

    notificationService.createAndUpdatePoemNotification(updatedPoem, user);

    return PoemResponse.from(
      updatedPoem,
      genre != null ? genre.getName() : null,
      author != null ? author.getName() : null
    );
  }

  @Override
  public PoemResponse detail(Long id, HttpServletRequest request) {
    log.debug("(facade) detail poem id = {}", id);
    PoemResponse response = poemService.detail(id);
    statisticService.increaseView(response.getId(), ClientIpUtils.getUserIdentifier(request));
    return response;
  }

  @Override
  public void share(Long id) {
    log.info("(facade) share poem id = {}", id);
    Poem poem = poemService.getAvailablePoemAndThrow(id);
    statisticService.increaseShare(poem.getId());
  }

  public void delete(Long id) {
    log.info("(facade) delete poem id = {}", id);

    poemService.delete(id);
  }
}
