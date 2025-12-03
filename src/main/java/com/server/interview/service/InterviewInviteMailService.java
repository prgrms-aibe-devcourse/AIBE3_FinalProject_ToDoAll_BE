package com.server.interview.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Year;

@Service
@RequiredArgsConstructor
public class InterviewInviteMailService {

    private final JavaMailSender mailSender;

    public void sendInviteEmail(String to, String jdTitle, String applicantName, String link, int expiryMinutes) {
        String safeTitle = (jdTitle == null || jdTitle.isBlank()) ? "jobda" : jdTitle;

        String subject = "[%s] 면접 채팅 초대 링크".formatted(safeTitle);
        String html = buildHtml(applicantName, safeTitle, link, expiryMinutes);

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
            throw new IllegalStateException("Failed to send invite email", e);
        }
    }

    private String buildHtml(String applicantName, String jdTitle, String inviteUrl, int expiryMinutes) {
        int year = Year.now().getValue();

        String template = """
                <!doctype html>
                <html lang="ko">
                <head>
                    <meta charset="utf-8" />
                    <title>면접 채팅 초대 메일</title>
                    <meta name="viewport" content="width=device-width,initial-scale=1" />
                    <style>
                        body { margin:0; padding:0; background:#f5f5f5; -webkit-text-size-adjust:100%; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif; }
                        a { text-decoration:none; }
                        img { border:0; line-height:100%; outline:none; text-decoration:none; }
                        table { border-collapse:collapse; }
                        .text { color:#333 !important; }
                    </style>
                </head>
                <body>
                <table role="presentation" width="100%" bgcolor="#f5f5f5" style="padding:40px 0;">
                    <tr>
                        <td align="center">
                            <table role="presentation" width="600" bgcolor="#FFFFFF" style="width:600px; max-width:600px; background:#FFFFFF; border-radius:8px; overflow:hidden; box-shadow: 0 2px 8px rgba(0,0,0,0.1);">

                                <tr>
                                    <td style="padding:60px 40px; text-align:center;">

                                        <h1 style="font-size:24px; font-weight:600; margin:0 0 24px; color:#333; letter-spacing:-0.5px;">
                                            면접 채팅 초대 링크
                                        </h1>

                                        <p style="font-size:14px; line-height:1.8; margin:0 0 40px; color:#666;">
                                            {{applicantName}} 님 안녕하세요.<br/>
                                            <strong style="color:#752F6D;">[{{jdTitle}}]</strong> 면접 채팅방에 초대되었습니다.<br/>
                                            아래 <strong style="color:#752F6D;">[채팅방 입장하기]</strong> 버튼을 클릭해 입장해 주세요.<br/>
                                            (유효시간: {{expiryMinutes}}분)
                                        </p>

                                        <div style="margin-bottom:40px;">
                                            <a href="{{inviteUrl}}"
                                               style="display:inline-block; padding:16px 48px; background:#752F6D; color:#ffffff; border-radius:8px; font-weight:600; font-size:15px;">
                                                채팅방 입장하기
                                            </a>
                                        </div>

                                        <p style="font-size:12px; line-height:1.6; margin:0; color:#999;">
                                            버튼이 동작하지 않으면 아래 주소를 브라우저에 붙여넣으세요.<br />
                                            <span style="word-break:break-all; color:#666;">{{inviteUrl}}</span>
                                        </p>

                                    </td>
                                </tr>

                                <tr>
                                    <td style="padding:24px; text-align:center; border-top:1px solid #eee;">
                                        <p style="font-size:11px; margin:0; color:#999; line-height:1.6;">
                                            본 메일은 발신 전용입니다.<br/>
                                            © {{year}} jobda. All rights reserved.
                                        </p>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                </table>
                </body>
                </html>
                """;


        String safeInviteUrl = escapeHtml(inviteUrl);


        return template
                .replace("{{applicantName}}", escapeHtml(applicantName))
                .replace("{{jdTitle}}", escapeHtml(jdTitle))
                .replace("{{expiryMinutes}}", String.valueOf(expiryMinutes))
                .replace("{{inviteUrl}}", inviteUrl) // URL은 그대로(escape하면 깨질 수 있음)
                .replace("{{year}}", String.valueOf(year));
    }


    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
