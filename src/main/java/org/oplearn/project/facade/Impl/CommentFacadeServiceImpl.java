package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.CommentRequest;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Comment;
import org.oplearn.project.entity.Poem;
import org.oplearn.project.entity.PoemComposition;
import org.oplearn.project.entity.User;
import org.oplearn.project.enums.PoemCompositionStatus;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.exception.base.BadRequestException;
import org.oplearn.project.facade.CommentFacadeService;
import org.oplearn.project.service.CommentService;
import org.oplearn.project.service.CompositionService;
import org.oplearn.project.service.PoemService;
import org.oplearn.project.service.StatisticService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class CommentFacadeServiceImpl implements CommentFacadeService {
  private final CommentService commentService;
  private final UserService userService;
  private final PoemService poemService;
  private final CompositionService compositionService;
  private final StatisticService statisticService;

  public CommentResponse create(CommentRequest request) {
    log.info("(facade) create comment");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    boolean hasPoem = request.getPoemId() != null;
    boolean hasComposition = request.getPoemCompositionId() != null;

    if ((hasPoem && hasComposition) || (!hasPoem && !hasComposition)) {
      log.warn("(create) comment must target either poem or composition");
      throw new BadRequestException();
    }

    Long poemId = null;
    Long poemCompositionId = null;

    if (hasPoem) {
      Poem poem = poemService.getAvailablePoemAndThrow(request.getPoemId());
      poemId = poem.getId();
    } else {
      PoemComposition composition = compositionService.getAvailableCompositionAndThrow(request.getPoemCompositionId());
      if(composition.getStatus() != PoemCompositionStatus.PUBLISHED) {
        log.warn("(create) composition must be published");
        throw new BadRequestException();
      }
      poemCompositionId = composition.getId();
    }

    Comment comment = Comment.builder()
      .content(request.getContent())
      .userId(currentUser.getId())
      .poemId(poemId)
      .poemCompositionId(poemCompositionId)
      .build();

    Comment savedComment = commentService.create(comment);

    if (savedComment.getPoemId() != null) {
      statisticService.increaseComment(savedComment.getPoemId());
    }

    return CommentResponse.from(savedComment, currentUser.getUsername());
  }

  public CommentResponse update(CommentRequest request, Long id) {
    log.info("(facade) update comment id = {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    CommentResponse existingComment = commentService.detail(id);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(existingComment.getUserId())) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    Comment updatedComment = commentService.update(request.getContent(), id);

    return CommentResponse.from(updatedComment, existingComment.getUsername());
  }

  public CursorPageResponse<CommentResponse> getCommentsByUserId(Long userId, Long cursor, int size) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(userId)) {
      log.warn("(getCommentsByUserId) user not authorized");
      throw new UserUnauthorizedException();
    }

    return commentService.getCommentsByUserId(userId, cursor, size);
  }

  public void delete(Long id) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    CommentResponse comment = commentService.detail(id);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(comment.getUserId())) {
      log.warn("(soft delete) user not authorized");
      throw new UserUnauthorizedException();
    }

    commentService.delete(id);

    if (comment.getPoemId() != null) {
      statisticService.decreaseComment(comment.getPoemId());
    }
  }
}
