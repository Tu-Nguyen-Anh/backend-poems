package org.oplearn.project.facade;

import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;

public interface FavoriteFacadeService {
  /** Thêm bài vào yêu thích của user hiện tại; trả về true (đã thích). */
  boolean add(Long poemId);

  /** Bỏ thích; trả về false (không còn thích). */
  boolean remove(Long poemId);

  /** Trạng thái thích của user hiện tại với 1 bài. */
  boolean status(Long poemId);

  /** Danh sách bài đã thích của user hiện tại. */
  PageResponse<PoemResponse> myFavorites(int size, int page);
}
