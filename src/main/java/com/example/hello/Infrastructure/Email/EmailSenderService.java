package com.example.hello.Infrastructure.Email;

import com.example.hello.Feature.User.dto.Address;
import com.example.hello.Infrastructure.Security.AppProperties;
import com.example.hello.Middleware.StringApplication;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailSenderService {
    final JavaMailSender mailSender;
    final TemplateEngine templateEngine;
    final AppProperties appProperties;
    @Value("${spring.mail.username}")
    String mailUsername;

    @Async
    public void sendEmail(String toEmail, String title, String templateName, Map<String, String> values) {
        try {
            Context context = new Context();
            values.forEach(context::setVariable);
            String emailContent = templateEngine.process(templateName, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,  "UTF-8");
            helper.setFrom(mailUsername, "Huy Hoàng Support");
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent email verification email to {}", toEmail);
        } catch (MessagingException e) {
            // Lỗi liên quan đến nội dung, format mail hoặc cấu hình SMTP
            log.error("SMTP/Messaging error when sending to {}: {}", toEmail, e.getMessage());
        } catch (MailException e) {
            // Lỗi kết nối server (Timeout, Connection refused)
            log.error("Network/Connection error to Mail Server when sending to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            // Các lỗi không xác định khác (Thymeleaf render lỗi...)
            log.error("Unexpected error during email processing for {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendEmailVerify(String toEmail, String title, String fullName, String activity,
                                Address address, String verificationLink, String timeExpired) {
        String addressSent = String.format("%s, %s, %s",
                address.getCity(), address.getRegion(), address.getCountry());
        sendEmail(toEmail, title, "verifyEmail", Map.of(
                "title", title,
                "fullName", fullName,
                "activity", activity,
                "address", addressSent,
                "verificationLink", verificationLink,
                "timeExpired", timeExpired
        ));
    }

    public void sendEmailWarningDevice(String toEmail, String fullName, Address address,
                                       String deviceName){
        String addressSent = String.format("%s, %s, %s",
                address.getCity(), address.getRegion(), address.getCountry());
        // Chỉ định múi giờ Việt Nam
        ZonedDateTime nowVietnam = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        // Format ra chuỗi
        String formattedNow = nowVietnam.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        sendEmail(toEmail, StringApplication.NOTIFICATION.WARNING_TITLE, "deviceWarningEmail", Map.of(
                "fullName", fullName,
                "address", addressSent,
                "deviceName", deviceName,
                "timeLogin", formattedNow,
                "frontendUrl", appProperties.getFrontendUrl()
        ));
    }

    public void sendEmailWelcome(String toEmail, String fullName){
        sendEmail(toEmail, StringApplication.NOTIFICATION.WELCOME_TITLE, "welcomeEmail", Map.of(
                "fullName", fullName,
                "frontendUrl", appProperties.getFrontendUrl(),
                "appName", StringApplication.FIELD.APP_NAME
        ));
    }
}
