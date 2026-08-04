package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.PoemRequest;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Author;
import org.oplearn.project.entity.Genre;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.facade.PoemFacadeService;
import org.oplearn.project.service.AuthorService;
import org.oplearn.project.service.GenreService;
import org.oplearn.project.service.PoemService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PoemFacadeServiceImpl implements PoemFacadeService {
  private final PoemService poemService;
  private final AuthorService authorService;
  private final GenreService genreService;

  public PoemResponse create(PoemRequest request) {
    log.info("(facade) create poem");

    Genre genre = genreService.getAvailableGenreAndThrow(request.getGenreId());

    Author author = authorService.getAvailableAuthorAndThrow(request.getAuthorId());

    Poem poem = Poem.builder()
      .name(request.getName())
      .description(request.getDescription())
      .year(request.getYear() != null ? request.getYear() : 0)
      .content(request.getContent())
      .transliteration(request.getTransliteration())
      .translation(request.getTranslation())
      .language(request.getLanguage())
      .genreId(genre.getId())
      .authorId(author.getId())
      .build();

    Poem savedPoem = poemService.create(poem);

    return PoemResponse.from(savedPoem, genre.getName(), author.getName());
  }

  public PoemResponse update(PoemRequest request, Long id) {
    log.info("(facade) update poem");

    Genre genre = genreService.getAvailableGenreAndThrow(request.getGenreId());

    Author author = authorService.getAvailableAuthorAndThrow(request.getAuthorId());

    Poem poem = Poem.builder()
      .name(request.getName())
      .description(request.getDescription())
      .year(request.getYear() != null ? request.getYear() : 0)
      .content(request.getContent())
      .transliteration(request.getTransliteration())
      .translation(request.getTranslation())
      .language(request.getLanguage())
      .genreId(genre.getId())
      .authorId(author.getId())
      .build();

    Poem updatedPoem = poemService.update(id, poem);

    return PoemResponse.from(updatedPoem, genre.getName(), author.getName());
  }
}
