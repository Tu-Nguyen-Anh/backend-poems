package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Reply;
import org.oplearn.project.exception.ReplyNotFoundException;
import org.oplearn.project.repository.ReplyRepository;
import org.oplearn.project.service.ReplyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
  private final ReplyRepository repository;

  public Reply create(Reply reply) {
    log.info("(service) create reply");
    return repository.save(reply);
  }

  public Reply update(Long id, String newContent) {
    log.info("(service) update reply");

    Reply updatedReply = repository.findByIdAndIsDeletedFalse(id)
      .orElseThrow(ReplyNotFoundException::new);

    updatedReply.setContent(newContent);

    return repository.save(updatedReply);
  }

  public void delete(Long id) {
    log.info("(service) delete reply");

    repository.softDeleteById(id);
  }

  public ReplyResponse detail(Long id) {
    log.info("(service) get reply with id = {}", id);

    return repository.findByIdAndReturnResponse(id)
      .orElseThrow(ReplyNotFoundException::new);
  }

  public PageResponse<ReplyItemResponse> getReplyByCommentId(Long commentId, int size, int page) {
    Pageable pageable = PageRequest.of(page, size);

    Page<ReplyItemResponse> replies = repository.findByCommentId(commentId, pageable);

    return PageResponse.of(
      replies.getContent(),
      (int) replies.getTotalElements()
    );
  }
}
