package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.HighlightRequest;
import org.oplearn.project.dto.response.HighlightResponse;
import org.oplearn.project.dto.response.HighlightWithPoemResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.entity.PoemHighlight;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.facade.HighlightFacadeService;
import org.oplearn.project.service.HighlightService;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class HighlightFacadeServiceImpl implements HighlightFacadeService {
  private final HighlightService highlightService;
  private final UserService userService;
  private final PoemService poemService;

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return userService.getUsernameOrThrow(authentication.getName());
  }

  private void assertOwner(PoemHighlight highlight) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User currentUser = userService.getUsernameOrThrow(authentication.getName());
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (!isAdmin && !currentUser.getId().equals(highlight.getUserId())) {
      log.warn("(highlight) user not authorized for highlight id = {}", highlight.getId());
      throw new UserUnauthorizedException();
    }
  }

  public HighlightResponse create(HighlightRequest request) {
    log.info("(facade) create highlight for poem id = {}", request.getPoemId());
    User user = currentUser();
    Poem poem = poemService.getAvailablePoemAndThrow(request.getPoemId());

    PoemHighlight highlight = PoemHighlight.builder()
      .userId(user.getId())
      .poemId(poem.getId())
      .startOffset(request.getStartOffset())
      .endOffset(request.getEndOffset())
      .selectedText(request.getSelectedText())
      .note(request.getNote())
      .build();

    return HighlightResponse.from(highlightService.create(highlight));
  }

  public HighlightResponse updateNote(Long id, String note) {
    log.info("(facade) update highlight note id = {}", id);
    assertOwner(highlightService.getAvailableAndThrow(id));
    return HighlightResponse.from(highlightService.updateNote(id, note));
  }

  public void delete(Long id) {
    log.info("(facade) delete highlight id = {}", id);
    assertOwner(highlightService.getAvailableAndThrow(id));
    highlightService.delete(id);
  }

  public List<HighlightResponse> listByPoem(Long poemId) {
    User user = currentUser();
    return highlightService.listByUserAndPoem(user.getId(), poemId);
  }

  public PageResponse<HighlightWithPoemResponse> myHighlights(int size, int page) {
    User user = currentUser();
    return highlightService.listByUser(user.getId(), size, page);
  }
}
