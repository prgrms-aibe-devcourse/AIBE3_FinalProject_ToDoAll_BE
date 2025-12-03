package com.server.interview.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewInviteMailService {

    private final JavaMailSender mailSender;

    public void sendInviteEmail(String to, String subject, String applicantName, String link, int expiryMinutes) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[%s] 면접 링크 초대".formatted(subject));
        msg.setText("""
                %s 님 안녕하세요.
                %s 면접에 초대되었습니다.
                (유효시간: %d분)
                
                %s
                """.formatted(applicantName, subject, expiryMinutes, link));
        mailSender.send(msg);
    }


}
