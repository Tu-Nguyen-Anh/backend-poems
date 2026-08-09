package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Author;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuthorResponse {
  Long id;
  String name;
  Integer birthYear;
  String achievement;
  String hometown;

  public static AuthorResponse from(Author author) {
    return new AuthorResponse(
      author.getId(),
      author.getName(),
      author.getBirthYear(),
      author.getAchievement(),
      author.getHometown()
    );
  }
}
