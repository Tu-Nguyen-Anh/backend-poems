package org.oplearn.project.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.constants.OpLearnConstants;
import org.oplearn.project.event.ForgotPasswordEvent;
import org.oplearn.project.service.EmailService;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ForgotPasswordConsumer {

  private final EmailService emailService;

  @RetryableTopic(
    attempts = "3",
    backoff = @Backoff(delay = 2000, multiplier = 2.0),
    dltStrategy = DltStrategy.FAIL_ON_ERROR,
    include = {Exception.class}
  )
  @KafkaListener(
    topics = OpLearnConstants.KafkaConstant.TOPIC_AUTH_FORGOT_PASSWORD_OTP,
    groupId = "${spring.kafka.consumer.group-id:poems-notification-group}"
  )
  public void consumerForgotPassword(
    @Payload ForgotPasswordEvent event,
    @Header(name = KafkaHeaders.RECEIVED_KEY, required = false) String key,
    @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
    @Header(KafkaHeaders.OFFSET) long offset
  ) {
    log.info("(consumeOtpEmail) Nhận sự kiện từ topic {} [partition: {}, offset: {}] cho email: {}",
      OpLearnConstants.KafkaConstant.TOPIC_AUTH_FORGOT_PASSWORD_OTP, partition, offset, key);

    try {
      emailService.sendHtmlEmail(
        event.getTo(),
        event.getSubject(),
        event.getTemplateName(),
        event.getVariables()
      );
    } catch (Exception ex) {
      log.error("(consumeOtpEmail) Gửi xác thực thất bại cho {}: {}", event.getTo(), ex.getMessage(), ex);
      throw ex;
    }
  }

  @DltHandler
  public void handleDlt(
    @Payload ForgotPasswordEvent event,
    @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
    @Header(KafkaHeaders.OFFSET) long offset
  ) {
    log.error("(handleDlt) CRITICAL: Gửi FotgotPassword tới email [{}] thất bại sau 3 lần retry. Message đã được chuyển vào DLT [{}] tại offset [{}]",
      event.getTo(), topic, offset);
  }
}
