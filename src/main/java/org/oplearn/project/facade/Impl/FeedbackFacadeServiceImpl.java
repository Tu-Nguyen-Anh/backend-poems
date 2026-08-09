package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.FeedbackRequest;
import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Feedback;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.facade.FeedbackFacadeService;
import org.oplearn.project.service.FeedbackService;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackFacadeServiceImpl implements FeedbackFacadeService {
  private final FeedbackService feedbackService;
  private final UserService userService;
  private final PoemService poemService;

  public FeedbackResponse create(FeedbackRequest request) {
    log.info("(facade) create feedback)");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    Poem poem = poemService.getAvailablePoemAndThrow(request.getPoemId());

    Feedback feedback = Feedback.builder()
      .content(request.getContent())
      .userId(currentUser.getId())
      .poemId(poem.getId())
      .build();

    Feedback savedFeedback = feedbackService.create(feedback);

    return FeedbackResponse.from(savedFeedback, currentUser.getUsername());
  }

  public FeedbackResponse update(FeedbackRequest request, Long id) {
    log.info("(facade) update feedback id = {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    FeedbackResponse existingFeedback = feedbackService.detail(id);

    boolean isAdmin =authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if(!isAdmin && !currentUser.getId().equals(existingFeedback.getUserId())) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    Feedback updatedFeedback = feedbackService.update(id , request.getContent());

    return FeedbackResponse.from(updatedFeedback, existingFeedback.getUsername());
  }

  public void delete(Long id) {
    log.info("(facade) delete feedback id = {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    FeedbackResponse existingFeedback = feedbackService.detail(id);

    boolean isAdmin =authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if(!isAdmin && !currentUser.getId().equals(existingFeedback.getUserId())) {
      log.warn("(delete) user not authorized");
      throw new UserUnauthorizedException();
    }

    feedbackService.delete(id);
  }

  public PageResponse<FeedbackResponse> getFeedbackByUserId(Long userId, int size, int page) {
    log.info("(facade) get feedback by user id = {})" , userId);

    userService.getAvailableUserAndThrow(userId);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String username = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(username);

    boolean isAdmin =authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if(!isAdmin && !currentUser.getId().equals(userId)) {
      log.warn("(getFeedbackByUserId) user not authorized");
      throw new UserUnauthorizedException();
    }

    return feedbackService.listByUserId(userId, size, page);
  }

  public PageResponse<FeedbackResponse> getFeedbackByPoemId(Long poemId, int size, int page) {
    log.info("(facade) get feedback by poem id = {})" , poemId);

    poemService.getAvailablePoemAndThrow(poemId);

    return feedbackService.listByPoemId(poemId, size, page);
  }
}
