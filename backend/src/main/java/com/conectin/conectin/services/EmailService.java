package com.conectin.conectin.services; // ou .notifications

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromEmail;

    @Async // Para não bloquear a thread principal
    public void enviarEmailSimples(String para, String assunto, String texto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(para);
            message.setSubject(assunto);
            message.setText(texto);
            mailSender.send(message);
            logger.info("Email enviado com sucesso para {}", para);
        } catch (Exception e) {
            logger.error("Erro ao enviar email para {}: {}", para, e.getMessage());
            // Adicionar lógica de retry ou notificar administradores se necessário
        }
    }

    // Você pode adicionar métodos para enviar e-mails com HTML usando MimeMessage e MimeMessageHelper
}