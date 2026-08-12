package org.oplearn.project.service;

import org.oplearn.project.dto.request.AuthorRequest;
import org.oplearn.project.dto.response.AuthorResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.PoemResponse;
import org.oplearn.project.entity.Author;

public interface AuthorService {
  AuthorResponse create(AuthorRequest request);

  AuthorResponse update(AuthorRequest request, Long id);

  void delete(Long id);

  AuthorResponse detail(Long id);

  PageResponse<AuthorResponse> list(String keyword, int size, int page, boolean isAll);

  PageResponse<AuthorResponse> listTopByPoemCount(int size, int page);

  PageResponse<PoemResponse> listPoemByAuthorId(Long id, int size, int page);

  Author getAvailableAuthorAndThrow(Long id);
}
