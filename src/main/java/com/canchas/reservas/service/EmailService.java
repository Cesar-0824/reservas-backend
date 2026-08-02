package com.canchas.reservas.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void enviarCorreoRecuperacion(String destinatario, String token) {
        String link = frontendUrl + "/reset-password?token=" + token;

        SimpleMailMessage mensaje = new SimpleMailMessage();
        mensaje.setTo(destinatario);
        mensaje.setSubject("Recupera tu contraseña - SportsMatch");
        mensaje.setText(
                "Hola,\n\n" +
                        "Recibimos una solicitud para restablecer tu contraseña.\n" +
                        "Haz clic en el siguiente enlace para crear una nueva contraseña:\n\n" +
                        link + "\n\n" +
                        "Este enlace es válido por 1 hora. Si no solicitaste este cambio, ignora este correo.\n\n" +
                        "Saludos,\nEquipo SportsMatch"
        );

        mailSender.send(mensaje);
    }
}