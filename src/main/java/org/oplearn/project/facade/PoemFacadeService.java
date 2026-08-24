package org.oplearn.project.facade;

import jakarta.servlet.http.HttpServletRequest;
import org.oplearn.project.dto.request.PoemRequest;
import org.oplearn.project.dto.response.PoemResponse;

public interface PoemFacadeService {
  PoemResponse create(PoemRequest request);

  PoemResponse update(PoemRequest request, Long id);

  PoemResponse detail(Long id, HttpServletRequest request);

  void share(Long id);
}
