package org.oplearn.project.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oplearn.project.entity.Author;

@Getter
@Setter
@NoArgsConstructor
public class AuthorResponse {
  Long id;
  String name;
  Integer birthYear;
  String achievement;
  String hometown;
  String avatarUrl;
  /** Key ảnh tự crawl trên RustFS (dạng "<uuid>.<ext>"); client build link = <RUSTFS_PUBLIC_URL>/daithihao/<avatarLocal>. */
  String avatarLocal;
  String bio;
  /** Quốc gia tác giả (crawl thivien): tên nước + id phân loại thivien. */
  String country;
  Integer countryId;
  Long poemCount;

  /** Constructor lõi 6 tham số — các constructor khác đều gọi lại constructor này. */
  public AuthorResponse(Long id, String name, Integer birthYear, String achievement, String hometown, Long poemCount) {
    this.id = id;
    this.name = name;
    this.birthYear = birthYear;
    this.achievement = achievement;
    this.hometown = hometown;
    this.poemCount = poemCount;
  }

  public AuthorResponse(Long id, String name, Integer birthYear, String achievement, String hometown) {
    this(id, name, birthYear, achievement, hometown, null);
  }

  /** 8 tham số — projection findTopByPoemCount kèm avatar (cho trang chủ hiện ảnh). */
  public AuthorResponse(Long id, String name, Integer birthYear, String achievement, String hometown,
                        Long poemCount, String avatarUrl, String avatarLocal) {
    this(id, name, birthYear, achievement, hometown, poemCount);
    this.avatarUrl = avatarUrl;
    this.avatarLocal = avatarLocal;
  }

  public static AuthorResponse from(Author author) {
    AuthorResponse res = new AuthorResponse(
      author.getId(),
      author.getName(),
      author.getBirthYear(),
      author.getAchievement(),
      author.getHometown()
    );
    res.setAvatarUrl(author.getAvatarUrl());
    res.setAvatarLocal(author.getAvatarLocal());
    res.setBio(author.getBio());
    res.setCountry(author.getCountry());
    res.setCountryId(author.getCountryId());
    return res;
  }
}
