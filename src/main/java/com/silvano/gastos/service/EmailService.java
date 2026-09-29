package com.silvano.gastos.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/** Envia e-mails pela API HTTP do Brevo (https://api.brevo.com/v3/smtp/email). */
@Service
public class EmailService {

    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    private final String apiKey;
    private final String remetente;
    private final String appUrl;

    public EmailService(@Value("${app.mail-api-key:}") String apiKey,
                         @Value("${app.mail-remetente:}") String remetente,
                         @Value("${app.url}") String appUrl) {
        this.apiKey = apiKey;
        this.remetente = remetente;
        this.appUrl = appUrl;
    }

    public void enviarResetSenha(String email, String nome, String token) {
        String link = appUrl + "/resetar-senha.html?token=" + token;
        String texto =
                "Oi " + nome + ",\n\n" +
                "Clique no link abaixo para escolher uma nova senha. Ele é válido por 1 hora:\n" +
                link + "\n\n" +
                "Se você não pediu essa redefinição, pode ignorar este e-mail.";

        Map<String, Object> payload = Map.of(
                "sender", Map.of("email", remetente, "name", "Meus Gastos"),
                "to", List.of(Map.of("email", email, "name", nome)),
                "subject", "Redefinir sua senha - Meus Gastos",
                "textContent", texto
        );

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(10))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(payload)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException("Brevo recusou o envio (" + response.statusCode() + "): " + response.body());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Falha ao enviar e-mail", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Falha ao enviar e-mail", e);
        }
    }
}
