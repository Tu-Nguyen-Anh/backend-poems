package org.oplearn.project.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.oplearn.project.entity.base.BaseEntity;

@Entity
@Table(name = "authors")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Author extends BaseEntity {
  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @Column(name = "birth_year")
  private Integer birthYear;

  @Column(name = "hometown")
  private String hometown;

  @Column(name = "achievement")
  private String achievement;

  @Column(name = "avatar_url", length = 500)
  private String avatarUrl;

  /** Key ảnh tự crawl trên RustFS (bucket daithihao), dạng "<uuid>.<ext>".
   *  Link đầy đủ = <RUSTFS_PUBLIC_URL>/daithihao/<avatarLocal>. avatarUrl (gốc thivien) giữ nguyên. */
  @Column(name = "avatar_local", length = 80)
  private String avatarLocal;

  @Column(name = "bio", columnDefinition = "TEXT")
  private String bio;
}
