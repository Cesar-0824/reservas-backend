package com.canchas.reservas.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    private static final String FILENAME = "reservas-front-firebase-adminsdk-fbsvc-dd270df40d.json";

    @PostConstruct
    public void init() {
        try {
            InputStream serviceAccount;

            // En Render (Docker), los Secret Files se montan en /etc/secrets/<archivo>
            File secretFile = new File("/etc/secrets/" + FILENAME);

            if (secretFile.exists()) {
                // Produccion: lee el Secret File configurado en Render
                serviceAccount = new FileInputStream(secretFile);
                System.out.println("Firebase: usando credencial desde /etc/secrets/");
            } else {
                // Local: lee el archivo desde src/main/resources (como antes)
                serviceAccount = new ClassPathResource(FILENAME).getInputStream();
                System.out.println("Firebase: usando credencial desde el classpath (local)");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase Admin inicializado correctamente");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}