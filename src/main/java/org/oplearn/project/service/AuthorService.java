package org.oplearn.project.service;

import org.oplearn.project.dto.request.AuthorRequest;
import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;

public interface AuthorService {
  AuthorResponse create(AuthorRequest request);

  AuthorResponse update(AuthorRequest request, Long id);

  void delete(Long id);

  AuthorResponse detail(Long id);

  PageResponse<AuthorResponse> list(String keyword, int size, int page, boolean isAll);

  PageResponse<PoemResponse> listPoemByAuthorId(Long id, int size, int page);
}
