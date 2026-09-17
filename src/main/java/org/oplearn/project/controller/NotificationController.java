package org.oplearn.project.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.dto.response.NotificationResponse;
import org.oplearn.project.dto.response.PageResponse;
import org.oplearn.project.dto.response.ResponseGeneral;
import org.oplearn.project.facade.NotificationFacadeService;
import org.springframework.web.bind.annotation.*;

import static org.oplearn.project.constants.OpLearnConstants.CommonConstants.SUCCESS_MESSAGE;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.PAGE_DEFAULT;
import static org.oplearn.project.constants.OpLearnConstants.VariableConstant.SIZE_DEFAULT;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {

  private final NotificationFacadeService facade;

  @GetMapping("/unread-count")
  public ResponseGeneral<Long> getUnreadCount() {
    log.info("(getUnreadCount) request unread count");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getUnreadCount());
  }

  @GetMapping
  public ResponseGeneral<PageResponse<NotificationResponse>> getFeed(
      @RequestParam(defaultValue = PAGE_DEFAULT) int page,
      @RequestParam(defaultValue = SIZE_DEFAULT) int size
  ) {
    log.info("(getFeed) request notification feed: page = {}, size = {}", page, size);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.getFeed(page, size));
  }

  @PutMapping("/{id}/read")
  public ResponseGeneral<Boolean> markAsRead(@PathVariable Long id) {
    log.info("(markAsRead) notification id: {}", id);
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.markAsRead(id));
  }

  @PutMapping("/read-all")
  public ResponseGeneral<Integer> markAllAsRead() {
    log.info("(markAllAsRead) mark all notifications as read");
    return ResponseGeneral.ofSuccess(SUCCESS_MESSAGE, facade.markAllAsRead());
  }
}
