
package com.annalogue.m2.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService implements IMailService {

    private final JavaMailSender mailSender;
    private final String from;

    public MailService(
            JavaMailSender mailSender,
            @Value("${spring.mail.username}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendCredentials(
            String email,
            String username,
            String temporaryPassword) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(email);
        message.setSubject("Bienvenue sur Annalogue - Vos identifiants");

        message.setText(
                "Bonjour,\n\n" +
                        "Votre compte Annalogue a été créé par un administrateur.\n\n" +
                        "Voici vos identifiants de connexion :\n\n" +
                        "Nom d'utilisateur : " + username + "\n" +
                        "Mot de passe temporaire : " + temporaryPassword + "\n\n" +
                        "Connectez-vous à Annalogue avec ces identifiants.\n" +
                        "Nous vous recommandons de modifier votre mot de passe " +
                        "dès votre première connexion.\n\n" +
                        "Cordialement,\n" +
                        "L'équipe Annalogue");

        mailSender.send(message);
    }
}