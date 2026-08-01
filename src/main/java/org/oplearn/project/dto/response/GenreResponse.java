package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Genre;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class GenreResponse {
  Long id;
  String name;

  public static GenreResponse from(Genre genre) {
    return new GenreResponse(genre.getId(), genre.getName());
  }
}
