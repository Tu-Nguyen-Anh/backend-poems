package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.FeedbackResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.entity.Feedback;
import org.oplearn.project.entity.FeedbackStatus;
import org.oplearn.project.exception.FeedbackNotFoundException;
import org.oplearn.project.repository.FeedbackRepository;
import org.oplearn.project.service.FeedbackService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@Slf4j
@RequiredArgsConstructor
public class FeedbackServiceImpl implements FeedbackService {
  private final FeedbackRepository repository;

  public Feedback create(Feedback feedback) {
    log.info("(service) create feedback");

    return repository.save(feedback);
  }

  public void delete(Long id) {
    log.info("(service) delete feedback");

    repository.softDeleteById(id);
  }

  public Feedback update(Long id, String newContent) {
    log.info("(service) update with id = {}", id);

    Feedback updatedFeedback = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(FeedbackNotFoundException::new);

    updatedFeedback.setContent(newContent);

    return repository.save(updatedFeedback);
  }

  public FeedbackResponse detail(Long id) {
    log.info("(service) get feedback with id = {}", id);

    return repository.findByIdAndReturnResponse(id)
      .orElseThrow(FeedbackNotFoundException::new);
  }

  public PageResponse<FeedbackResponse> list(FeedbackStatus status, int size, int page, boolean isAll) {
    log.info("(service) list feedback");

    Pageable pageable = isAll ? Pageable.unpaged() : PageRequest.of(page, size);

    Page<FeedbackResponse> feedbacks = Objects.nonNull(status)
      ? repository.findByStatus(status, pageable)
      : repository.findAllByIsDeletedFalse(pageable);

    return PageResponse.of(
      feedbacks.getContent(),
      (int) feedbacks.getTotalElements()
    );
  }

  public PageResponse<FeedbackResponse> listByUserId(Long userId, int size, int page) {
    log.info("(service) list feedback by user id");

    Pageable pageable = PageRequest.of(page, size);

    Page<FeedbackResponse> feedbacks = repository.findFeedbackByUserId(userId, pageable);

    return PageResponse.of(
      feedbacks.getContent(),
      (int) feedbacks.getTotalElements()
    );
  }

  public PageResponse<FeedbackResponse> listByPoemId(Long poemId, int size, int page) {
    log.info("(service) list feedback by poem id");

    Pageable pageable = PageRequest.of(page, size);

    Page<FeedbackResponse> feedbacks = repository.findFeedbackByPoemId(poemId, pageable);

    return PageResponse.of(
      feedbacks.getContent(),
      (int) feedbacks.getTotalElements()
    );
  }

  public void updateStatus(Long id, FeedbackStatus status) {
    log.info("(service) update status feedback with id = {}", id);

    Feedback feedback = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(FeedbackNotFoundException::new);

    feedback.setStatus(status);

    repository.save(feedback);
  }
}
