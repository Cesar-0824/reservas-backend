package com.canchas.reservas.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmailService {

    @Value("${brevo.api.key}")
    private String brevoApiKey;

    @Value("${brevo.sender.email}")
    private String senderEmail;

    @Value("${brevo.sender.name:SportsMatch}")
    private String senderName;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarCorreoRecuperacion(String destinatario, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        String htmlContent =
                "<p>Hola,</p>" +
                        "<p>Recibimos una solicitud para restablecer tu contrase\u00f1a.</p>" +
                        "<p><a href=\"" + link + "\">Haz clic aqu\u00ed para crear una nueva contrase\u00f1a</a></p>" +
                        "<p>Este enlace es v\u00e1lido por 1 hora. Si no solicitaste este cambio, ignora este correo.</p>" +
                        "<p>Saludos,<br>Equipo SportsMatch</p>";

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);
        body.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", destinatario);
        body.put("to", List.of(to));

        body.put("subject", "Recupera tu contrase\u00f1a - SportsMatch");
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity(
                "https://api.brevo.com/v3/smtp/email",
                request,
                String.class
        );
    }
}
