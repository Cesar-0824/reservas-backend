package com.canchas.reservas.config;

import com.cloudinary.Cloudinary;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class    CloudinaryConfig {

    // Se lee automaticamente de la variable de entorno CLOUDINARY_URL
    // (en Render la configuras en Environment Variables; en local, si no
    // existe, el bean queda vacio y simplemente no podras subir imagenes
    // hasta que la definas tambien en tu compu si quieres probarlo local).
    @Value("${CLOUDINARY_URL:}")
    private String cloudinaryUrl;

    @Bean
    public Cloudinary cloudinary() {
        if (cloudinaryUrl != null && !cloudinaryUrl.isBlank()) {
            return new Cloudinary(cloudinaryUrl);
        }
        System.out.println("⚠️ CLOUDINARY_URL no configurada: la subida de imagenes no funcionara hasta que la definas.");
        return new Cloudinary();
    }
}
