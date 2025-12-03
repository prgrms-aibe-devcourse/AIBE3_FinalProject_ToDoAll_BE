package com.server.interview.service;

import com.server.global.exception.ApplicationException;
import com.server.interview.exception.InterviewInviteMailErrorCase;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Year;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class InterviewInviteMailService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendInviteEmail(String to, String jdTitle, String applicantName, String link, int expiryMinutes) {
        validate(to, link, expiryMinutes);

        String safeTitle = (jdTitle == null || jdTitle.isBlank()) ? "jobda" : jdTitle;
        String safeApplicantName = applicantName == null ? "" : applicantName;

        String subject = sanitizeHeader("[%s] 면접 채팅 초대 링크".formatted(safeTitle));

        String html = renderHtml(safeApplicantName, safeTitle, link, expiryMinutes);

        send(to, subject, html);
    }

    private void validate(String to, String link, int expiryMinutes) {
        if (to == null || to.isBlank() || !EMAIL_PATTERN.matcher(to).matches()) {
            throw new ApplicationException(InterviewInviteMailErrorCase.INVALID_RECIPIENT_EMAIL);
        }

        if (link == null || link.isBlank()) {
            throw new ApplicationException(InterviewInviteMailErrorCase.INVALID_INVITE_URL);
        }

        try {
            URI uri = URI.create(link);
            if (uri.getScheme() == null || uri.getHost() == null) {
                throw new IllegalArgumentException();
            }
            String scheme = uri.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            throw new ApplicationException(InterviewInviteMailErrorCase.INVALID_INVITE_URL);
        }

        if (expiryMinutes <= 0 || expiryMinutes > 24 * 60) {
            throw new ApplicationException(InterviewInviteMailErrorCase.INVALID_EXPIRY_MINUTES);
        }
    }

    private String renderHtml(String applicantName, String jdTitle, String inviteUrl, int expiryMinutes) {
        try {
            Context ctx = new Context(Locale.KOREAN);
            ctx.setVariable("applicantName", applicantName);
            ctx.setVariable("jdTitle", jdTitle);
            ctx.setVariable("inviteUrl", inviteUrl);
            ctx.setVariable("expiryMinutes", expiryMinutes);
            ctx.setVariable("year", Year.now().getValue());

            return templateEngine.process("email/interview-invite", ctx);

        } catch (TemplateInputException e) {
            throw new ApplicationException(InterviewInviteMailErrorCase.MAIL_TEMPLATE_NOT_FOUND);
        } catch (RuntimeException e) {
            throw new ApplicationException(InterviewInviteMailErrorCase.MAIL_TEMPLATE_RENDER_FAILED);
        }
    }

    private void send(String to, String subject, String html) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_NO,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            throw new ApplicationException(InterviewInviteMailErrorCase.MAIL_COMPOSE_FAILED);
        } catch (MailException e) {
            throw new ApplicationException(InterviewInviteMailErrorCase.MAIL_SEND_FAILED);
        }
    }

    private String sanitizeHeader(String value) {
        if (value == null) return "";
        return value.replace("\r", "").replace("\n", "");
    }
}
