package com.canchas.reservas.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.stereotype.Service;

@Service
public class FirebaseService {

    public boolean existeCorreo(String correo) {
        try {
            UserRecord user = FirebaseAuth.getInstance().getUserByEmail(correo);
            return user != null; // Si lo encuentra, existe
        } catch (FirebaseAuthException e) {
            if ("USER_NOT_FOUND".equals(e.getAuthErrorCode().name())) {
                return false; // No existe en Firebase
            }
            throw new RuntimeException("Error consultando Firebase", e);
        }
    }
}
