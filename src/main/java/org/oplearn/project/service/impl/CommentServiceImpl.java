package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.entity.Comment;
import org.oplearn.project.exception.CommentNotFoundException;
import org.oplearn.project.exception.base.BadRequestException;
import org.oplearn.project.repository.CommentRepository;
import org.oplearn.project.service.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final CommentRepository repository;

  private static final int MAX_REQUESTS = 5;
  private static final long WINDOW_1_MINUTE_MILLIS = 60 * 1000L;
  private final ConcurrentHashMap<Long, List<Long>> userRequestTimestamps = new ConcurrentHashMap<>();

  @Transactional
  public Comment create(Comment comment) {
    log.info("(service) create comment");

    Long userId = comment.getUserId();
    Long now = System.currentTimeMillis();

    userRequestTimestamps.compute(userId, (id , timestamps) -> {
      if(timestamps == null) {
        timestamps = new java.util.ArrayList<>();
      }

      timestamps.removeIf(t -> now - t > WINDOW_1_MINUTE_MILLIS);

      if(timestamps.size() >= MAX_REQUESTS) {
        log.warn("(create) user {} reached limit {} requests in 1 minute", userId, MAX_REQUESTS);
        throw new BadRequestException("Too many requests");
      }
      timestamps.add(now);
      return timestamps;
    });

    return repository.save(comment);
  }

  @Transactional
  public Comment update(String newContent, Long id) {
    log.info("(service) update comment with id = {}", id);

    Comment updatedComment = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(CommentNotFoundException::new);

    updatedComment.setContent(newContent);

    return repository.save(updatedComment);
  }

  @Transactional
  public void delete(Long id) {
    log.info("(service) delete with id = {}", id);

    repository.softDeleteById(id);
  }

  public CommentResponse detail(Long id) {
    log.info("(service) get comment with id = {}", id);

    return repository.findByIdAndReturnResponse(id)
      .orElseThrow(CommentNotFoundException::new);
  }

  public CursorPageResponse<CommentResponse> getCommentsByPoemId(Long poemId, Long cursor, int size) {
    Pageable pageable = PageRequest.of(0, size + 1);

    List<CommentResponse> comments = repository.findByPoemIdCursor(poemId, cursor, pageable);

    boolean hasNext = comments.size() > size;
    Long nextCursor = null;

    if (hasNext) {
      comments = comments.subList(0, size);
      nextCursor = comments.get(comments.size() - 1).getId();
    }

    Long totalElements = (cursor == null) ? repository.countByPoemIdAndIsDeletedFalse(poemId) : null;

    return CursorPageResponse.of(comments, nextCursor, hasNext, totalElements);
  }

   public CursorPageResponse<CommentResponse> getCommentsByPoemCompositionId(Long poemCompositionId, Long cursor, int size) {
    Pageable pageable = PageRequest.of(0, size + 1);

    List<CommentResponse> comments = repository.findByPoemCompositionIdCursor(poemCompositionId, cursor, pageable);

     boolean hasNext = comments.size() > size;
     Long nextCursor = null;

     if (hasNext) {
       comments = comments.subList(0, size);
       nextCursor = comments.get(comments.size() - 1).getId();
     }

     Long totalElements = (cursor == null) ? repository.countByPoemCompositionIdAndIsDeletedFalse(poemCompositionId) : null;

     return CursorPageResponse.of(comments, nextCursor, hasNext, totalElements);
   }

  public CursorPageResponse<CommentResponse> getCommentsByUserId(Long userId, Long cursor, int size) {

    Pageable pageable = PageRequest.of(0, size + 1);

    List<CommentResponse> comments = repository.findByUserIdCursor(userId, cursor, pageable);

    boolean hasNext = comments.size() > size;
    Long nextCursor = null;

    if (hasNext) {
      comments = comments.subList(0, size);
      nextCursor = comments.get(comments.size() - 1).getId();
    }

    Long totalElements = (cursor == null) ? repository.countByUserIdAndIsDeletedFalse(userId) : null;

    return CursorPageResponse.of(comments, nextCursor, hasNext, totalElements);
  }

  public Comment getAvailableCommentAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(CommentNotFoundException::new);
  }
}
