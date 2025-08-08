package com.example.bankdb.service;

import com.example.bankdb.exception.EmailSendingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailSenderService {

    private final JavaMailSender mailSender;

    public void sendEmail(String toEmail,
                          String subject,
                          String body) {

        if (toEmail == null || toEmail.isBlank()) {
            log.warn("Attempted to send email, but 'Email' was null or blank.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("abdullayevmuxtar22@gmail.com");
        message.setTo(toEmail);
        message.setText(body);
        message.setSubject(subject);

        try {
            mailSender.send(message);
            log.info("Mail sent successfully to {}", toEmail);
        } catch (MailException e) {
            log.error("Error sending mail to {}: {}", toEmail, e.getMessage());
            throw new EmailSendingException("Failed to send email to " + toEmail);
        }
    }
}
