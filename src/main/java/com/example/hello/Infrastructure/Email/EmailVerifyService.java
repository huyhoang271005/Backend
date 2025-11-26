package com.example.hello.Infrastructure.Email;

import com.example.hello.Middleware.StringApplication;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableAsync
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailVerifyService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;
    @Async
    public void sendEmail(String to, String title, String fullName, String verificationLink, String timeExpired) {
        try {
            Context context = new Context();
            context.setVariable("title", title);
            context.setVariable("fullName", fullName);
            context.setVariable("verificationLink", verificationLink);
            context.setVariable("timeExpired", timeExpired);
            String emailContent = templateEngine.process("verifyEmail", context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,  "UTF-8");
            helper.setTo(to);
            helper.setSubject(title);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent email verification email to {}", to);
        } catch (Exception e) {
            log.error("Sent email verification email to {}", to, e);
            throw new RuntimeException(StringApplication.ERROR.INTERNAL_SERVER_ERROR);
        }
    }

}
