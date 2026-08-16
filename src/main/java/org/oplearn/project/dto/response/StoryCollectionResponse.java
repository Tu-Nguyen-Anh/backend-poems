package org.oplearn.project.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Một thể loại văn xuôi + số tác phẩm (cho dropdown lọc). */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StoryCollectionResponse {
  private String collection;
  private Long count;
}
