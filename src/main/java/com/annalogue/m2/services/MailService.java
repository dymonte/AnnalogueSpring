package com.annalogue.m2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;
    private final NotifyService notifyService;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            NotifyService notifyService,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.notifyService = notifyService;
        this.from = from;
    }

    public void sendCredentials(
            String email,
            String username,
            String temporaryPassword) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(email);
        // texts from messages.properties: {0} = username, {1} = temporary password
        message.setSubject(notifyService.getMessage("mail.credentials.subject"));
        message.setText(notifyService.getMessage("mail.credentials.body", username, temporaryPassword));

        mailSender.send(message);
    }
}