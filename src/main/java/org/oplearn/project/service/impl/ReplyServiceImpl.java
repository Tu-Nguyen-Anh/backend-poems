package org.oplearn.project.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.CursorPageResponse;
import org.oplearn.project.dto.response.ReplyItemResponse;
import org.oplearn.project.dto.response.ReplyResponse;
import org.oplearn.project.entity.Reply;
import org.oplearn.project.exception.ReplyNotFoundException;
import org.oplearn.project.repository.ReplyRepository;
import org.oplearn.project.service.ReplyService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

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

  public CursorPageResponse<ReplyItemResponse> getReplyByCommentId(Long commentId, Long cursor, int size) {
    Pageable pageable = PageRequest.of(0, size + 1);

    List<ReplyItemResponse> replies = repository.findByCommentId(commentId, cursor, pageable);

    boolean hasNext = replies.size() > size;
    Long nextCursor = null;

    if(hasNext) {
      replies = replies.subList(0, size);
      nextCursor = replies.get(replies.size() - 1).getId();
    }

    Long totalElements = (nextCursor == null) ? repository.countByCommentIdAndIsDeletedFalse(commentId) : null;

    return CursorPageResponse.of(replies, nextCursor, hasNext, totalElements);
  }

  public CursorPageResponse<ReplyResponse> getReplyByUserId(Long userId, Long cursor, int size) {
    Pageable pageable = PageRequest.of(0, size + 1);

    List<ReplyResponse> replies = repository.findByUserId(userId, cursor, pageable);

    boolean hasNext = replies.size() > size;
    Long nextCursor = null;

    if(hasNext) {
      replies = replies.subList(0, size);
      nextCursor = replies.get(replies.size() - 1).getId();
    }

    Long totalElements = (nextCursor == null) ? repository.countByUserIdAndIsDeletedFalse(userId) : null;

    return CursorPageResponse.of(replies, nextCursor, hasNext, totalElements);
  }
}
