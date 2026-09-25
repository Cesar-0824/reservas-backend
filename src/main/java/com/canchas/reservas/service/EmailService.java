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

    @Value("${club.email.institucional:}")
    private String emailInstitucionalClub;

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
    public void enviarCorreoRecordatorio(String destinatario, String nombreUsuario, String cancha, String fecha, String horaInicio, String horaFin, String horasRestantes) {
        String htmlContent =
                "<p>Hola " + nombreUsuario + ",</p>" +
                        "<p>Te recordamos que tienes una reserva próxima:</p>" +
                        "<p><strong>Cancha:</strong> " + cancha + "<br>" +
                        "<strong>Fecha:</strong> " + fecha + "<br>" +
                        "<strong>Horario:</strong> " + horaInicio + " - " + horaFin + "</p>" +
                        "<p>Tu reserva es en aproximadamente " + horasRestantes + ".</p>" +
                        "<p>Saludos,<br>Equipo SportsMatch</p>";

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);
        body.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", destinatario);
        body.put("to", List.of(to));

        body.put("subject", "Recordatorio de tu reserva - SportsMatch");
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

    public void enviarCorreoConfirmacionPendientePago(String destinatario, String nombreUsuario, String cancha, String fecha, String horaInicio, String horaFin) {
        String htmlContent =
                "<p>Hola " + nombreUsuario + ",</p>" +
                        "<p>Tu reserva fue <strong>confirmada</strong>:</p>" +
                        "<p><strong>Cancha:</strong> " + cancha + "<br>" +
                        "<strong>Fecha:</strong> " + fecha + "<br>" +
                        "<strong>Horario:</strong> " + horaInicio + " - " + horaFin + "</p>" +
                        "<p>Tienes <strong>15 minutos</strong> para realizar el pago, de lo contrario la reserva se cancelará automáticamente.</p>" +
                        "<p>Saludos,<br>Equipo SportsMatch</p>";
        enviarCorreoGenerico(destinatario, "Reserva confirmada - Pendiente de pago - SportsMatch", htmlContent);
    }

    public void enviarCorreoPagoExitoso(String destinatario, String nombreUsuario, String cancha, String fecha, String horaInicio, String horaFin) {
        String htmlContent =
                "<p>Hola " + nombreUsuario + ",</p>" +
                        "<p>Confirmamos que tu pago fue realizado correctamente. Tu reserva quedó registrada:</p>" +
                        "<p><strong>Cancha:</strong> " + cancha + "<br>" +
                        "<strong>Fecha:</strong> " + fecha + "<br>" +
                        "<strong>Horario:</strong> " + horaInicio + " - " + horaFin + "</p>" +
                        "<p>Saludos,<br>Equipo SportsMatch</p>";
        enviarCorreoGenerico(destinatario, "Pago confirmado - SportsMatch", htmlContent);
    }

    public void enviarCorreoCancelacion(String destinatario, String nombreUsuario, String cancha, String fecha, String horaInicio, String horaFin, String motivo, boolean porSistema) {
        String intro = porSistema
                ? "<p>Tu reserva fue <strong>cancelada automáticamente</strong> por el sistema.</p>"
                : "<p>Lamentamos informarte que el administrador <strong>canceló tu reserva</strong>.</p>";

        String htmlContent =
                "<p>Hola " + nombreUsuario + ",</p>" +
                        intro +
                        "<p><strong>Cancha:</strong> " + cancha + "<br>" +
                        "<strong>Fecha:</strong> " + fecha + "<br>" +
                        "<strong>Horario:</strong> " + horaInicio + " - " + horaFin + "</p>" +
                        "<p><strong>Motivo:</strong> " + motivo + "</p>" +
                        "<p>Saludos,<br>Equipo SportsMatch</p>";
        enviarCorreoGenerico(destinatario, "Reserva cancelada - SportsMatch", htmlContent);
    }

    // Helper privado para no repetir el bloque de armado/envío en cada método
    private void enviarCorreoGenerico(String destinatario, String subject, String htmlContent) {
        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);
        body.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", destinatario);
        body.put("to", List.of(to));

        body.put("subject", subject);
        body.put("htmlContent", htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
    }

    // Preparado para notificaciones importantes al admin. Mientras no se configure
// club.email.institucional en application.properties, solo se registra en log
// y no se envía correo (evita usar el correo personal del administrador).
    public void enviarCorreoInstitucionalAdmin(String asunto, String mensajeHtml) {
        if (emailInstitucionalClub == null || emailInstitucionalClub.isBlank()) {
            System.out.println("[INFO] Correo institucional no configurado. Asunto pendiente: " + asunto);
            return;
        }

        Map<String, Object> body = new HashMap<>();

        Map<String, String> sender = new HashMap<>();
        sender.put("name", senderName);
        sender.put("email", senderEmail);
        body.put("sender", sender);

        Map<String, String> to = new HashMap<>();
        to.put("email", emailInstitucionalClub);
        body.put("to", List.of(to));

        body.put("subject", asunto);
        body.put("htmlContent", mensajeHtml);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", brevoApiKey);
        headers.set("accept", "application/json");

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        restTemplate.postForEntity("https://api.brevo.com/v3/smtp/email", request, String.class);
    }

}
