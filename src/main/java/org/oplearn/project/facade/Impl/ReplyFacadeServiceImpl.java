package org.oplearn.project.facade.Impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.request.ReplyRequest;
import org.oplearn.project.dto.response.CommentWithRepliesResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Comment;
import org.oplearn.project.entity.Reply;
import org.oplearn.project.entity.User;
import org.oplearn.project.exception.UserUnauthorizedException;
import org.oplearn.project.facade.ReplyFacadeService;
import org.oplearn.project.service.CommentService;
import org.oplearn.project.service.ReplyService;
import org.oplearn.project.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
@Slf4j
public class ReplyFacadeServiceImpl implements ReplyFacadeService {
  private final ReplyService replyService;
  private final UserService userService;
  private final CommentService commentService;

  public ReplyResponse create(ReplyRequest request) {
    log.info("(facade) create reply)");

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    Comment comment = commentService.getAvailableCommentAndThrow(request.getCommentId());

    Reply reply = Reply.builder()
      .content(request.getContent())
      .userId(currentUser.getId())
      .commentId(comment.getId())
      .build();

    Reply savedReply = replyService.create(reply);

    return ReplyResponse.from(
      savedReply,
      currentUser.getUsername(),
      comment.getContent());
  }

  public ReplyResponse update(ReplyRequest request, Long id) {
    log.info("(facade) update reply id = {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    ReplyResponse existingReply = replyService.detail(id);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(existingReply.getUserId())) {
      log.warn("(update) user not authorized");
      throw new UserUnauthorizedException();
    }

    Reply updatedReply = replyService.update(id, request.getContent());

    return ReplyResponse.from(
      updatedReply,
      existingReply.getUsername(),
      existingReply.getContentComment()
    );
  }

  public void delete(Long id) {
    log.info("(facade) delete reply id = {}", id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    String currentUsername = authentication.getName();

    User currentUser = userService.getUsernameOrThrow(currentUsername);

    ReplyResponse existingReply = replyService.detail(id);

    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!isAdmin && !currentUser.getId().equals(existingReply.getUserId())) {
      log.warn("(delete) user not authorized");
      throw new UserUnauthorizedException();
    }

    replyService.delete(id);
  }

  public CommentWithRepliesResponse getReplyByCommentId(Long commentId, Long cursor, int size) {
    log.info("(facade) get reply by comment id = {}", commentId);

    Comment comment = commentService.getAvailableCommentAndThrow(commentId);

    CursorPageResponse<ReplyItemResponse> repliesResponse = replyService.getReplyByCommentId(commentId, cursor, size);

    return CommentWithRepliesResponse.builder()
      .commentId(comment.getId())
      .contentComment(comment.getContent())
      .replies(repliesResponse)
      .build();
  }

  public CursorPageResponse<ReplyResponse> getReplyByUserId(Long userId, Long cursor, int size) {
    log.info("(facade) get reply by user id = {}", userId);

    userService.getAvailableUserAndThrow(userId);

    return replyService.getReplyByUserId(userId, cursor, size);
  }
}
