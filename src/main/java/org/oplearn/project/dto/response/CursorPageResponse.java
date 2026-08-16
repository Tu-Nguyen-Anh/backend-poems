package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
public class CursorPageResponse<T> {
  private List<T> content;
  private Long nextCursor;
  private boolean hasNext;
  private Long totalElements;

  public static <T> CursorPageResponse<T> of(List<T> content, Long nextCursor, boolean hasNext) {
    return of(content, nextCursor, hasNext, null);
  }
}
