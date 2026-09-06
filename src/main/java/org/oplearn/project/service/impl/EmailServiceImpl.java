package org.oplearn.project.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oplearn.project.service.EmailService;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import org.springframework.beans.factory.annotation.Value;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    @Override
    @Async("mailTaskExecutor")
    public void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables) {
        log.info("Bắt đầu gửi email ngầm tới [{}] với tiêu đề [{}]", to, subject);

        try {
            // 1. Tạo MimeMessage hỗ trợ HTML và mã hoá UTF-8
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            // 2. Nạp dữ liệu biến vào Thymeleaf Context
            Context context = new Context();
            if (variables != null && !variables.isEmpty()) {
                context.setVariables(variables);
            }

            // 3. Render HTML từ file template
            String htmlContent = templateEngine.process(templateName, context);

            // 4. Thiết lập người gửi, người nhận, tiêu đề và nội dung HTML
            if (fromEmail != null && !fromEmail.isBlank()) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = định dạng HTML

            // 5. Gửi thư qua SMTP
            mailSender.send(message);
            log.info("Gửi email thành công tới [{}]", to);

        } catch (MessagingException e) {
            log.error("Gửi email tới [{}] thất bại do lỗi giao thức Mail: {}", to, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Xảy ra lỗi không mong muốn khi gửi email tới [{}]: {}", to, e.getMessage(), e);
        }
    }
}
