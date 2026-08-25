package org.oplearn.project.service;

import java.util.Map;

public interface EmailService {

    /**
     * Gửi email HTML bất đồng bộ (Async).
     *
     * @param to           Địa chỉ email người nhận
     * @param subject      Tiêu đề email
     * @param templateName Đường dẫn template Thymeleaf (ví dụ: "mail/welcome-email")
     * @param variables    Dữ liệu truyền vào template HTML
     */
    void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> variables);
}
