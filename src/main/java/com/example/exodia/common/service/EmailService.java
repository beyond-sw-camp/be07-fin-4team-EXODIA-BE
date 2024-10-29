package com.example.exodia.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmailService {
    @Value("${spring.mail.username}")
    private String serviceEmail;


    private final JavaMailSender mailSender;

    public void sendCourseTransmissionEmail(List<String> recipients, String courseName) {
        System.out.println("sendCourseTransmissionEmail 메서드가 호출되었습니다.");
        for (String recipient : recipients) {
            try {
                SimpleMailMessage mailMessage = new SimpleMailMessage();
                mailMessage.setFrom(serviceEmail);
                mailMessage.setTo(recipient);
                mailMessage.setSubject("강좌 배정 완료: " + courseName);
                mailMessage.setText("안녕하세요,\n\n" + courseName + " 강좌 배정이 완료되었습니다. 확인 부탁드립니다.");

                mailSender.send(mailMessage);
                System.out.println("메일 전송 성공: " + recipient);
            } catch (MailException e) {
                System.err.println("메일 전송 실패: " + recipient);
                e.printStackTrace(); // 상세한 예외 로그 출력
            }
        }
    }
}
