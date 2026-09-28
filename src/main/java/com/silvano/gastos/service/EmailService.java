package com.silvano.gastos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String appUrl;
    private final String remetente;

    public EmailService(JavaMailSender mailSender,
                         @Value("${app.url}") String appUrl,
                         @Value("${app.mail-remetente:}") String remetente) {
        this.mailSender = mailSender;
        this.appUrl = appUrl;
        this.remetente = remetente;
    }

    public void enviarResetSenha(String email, String nome, String token) {
        String link = appUrl + "/resetar-senha.html?token=" + token;
        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Redefinir sua senha - Meus Gastos");
        mensagem.setText(
                "Oi " + nome + ",\n\n" +
                "Clique no link abaixo para escolher uma nova senha. Ele é válido por 1 hora:\n" +
                link + "\n\n" +
                "Se você não pediu essa redefinição, pode ignorar este e-mail."
        );
        mailSender.send(mensagem);
    }
}
