package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.CommentResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Comment;
import org.oplearn.project.exception.CommentNotFoundException;
import org.oplearn.project.repository.CommentRepository;
import org.oplearn.project.service.CommentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

  public PageResponse<CommentResponse> getCommentsByPoemId(Long poemId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);

    Page<CommentResponse> comments = repository.findByPoemId(poemId, pageable);

    return PageResponse.of(
      comments.getContent(),
      (int) comments.getTotalElements()
    );
  }

  public PageResponse<CommentResponse> getCommentsByUserId(Long userId, int size, int page) {

    Pageable pageable = PageRequest.of(page, size);

    Page<CommentResponse> comments = repository.findByUserId(userId, pageable);

    return PageResponse.of(
      comments.getContent(),
      (int) comments.getTotalElements()
    );
  }

  public Comment getAvailableCommentAndThrow(Long id) {
    return repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(CommentNotFoundException::new);
  }
}
