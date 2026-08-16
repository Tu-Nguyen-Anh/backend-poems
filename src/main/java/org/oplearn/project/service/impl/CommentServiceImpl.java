package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.entity.Comment;
import org.oplearn.project.exception.CommentNotFoundException;
import org.oplearn.project.repository.CommentRepository;
import org.oplearn.project.service.CommentService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
  private final CommentRepository repository;

  @Transactional
  public Comment create(Comment comment) {
    log.info("(service) create comment");

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
