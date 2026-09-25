package com.canchas.reservas.controller;

import com.canchas.reservas.DTO.LoginRequest;
import com.canchas.reservas.DTO.LoginResponse;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.UsuarioRepository;
import com.canchas.reservas.security.JwtUtil;
import com.canchas.reservas.service.EmailService;
import com.canchas.reservas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(
        origins = {
                "http://localhost:3000",
                "https://reservas-frontend-git-main-cesar-bdca.vercel.app",
                "https://reservas-frontend-75fwwl4xm-cesar-bdca.vercel.app",
                "https://reservas-frontend-seven.vercel.app"
        },
        allowCredentials = "true"
)
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private EmailService emailService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        Optional<Usuario> userOpt = repo.findByEmail(req.getEmail());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "codigo", "CORREO_NO_REGISTRADO",
                    "mensaje", "El correo electrónico que has introducido no está asociado a ninguna cuenta."
            ));
        }

        Usuario u = userOpt.get();

        if (u.getHabilitado() != null && !u.getHabilitado()) {
            return ResponseEntity.status(403).body(Map.of(
                    "codigo", "USUARIO_DESHABILITADO",
                    "mensaje", "Usuario deshabilitado. Comunícate con el administrador."
            ));
        }

        if (!encoder.matches(req.getPassword(), u.getContrasena()))
            return ResponseEntity.status(401).body(Map.of(
                    "codigo", "CREDENCIALES_INVALIDAS",
                    "mensaje", "Credenciales inválidas"
            ));

        String token = jwtUtil.generateToken(u.getEmail(), u.getRol().name());
        return ResponseEntity.ok(new LoginResponse(token, u.getEmail(), u.getId(), u.getNombre(), u.getRol(), u.getFechaRegistro()));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestParam String email) {
        try {
            Usuario usuario = usuarioService.generarTokenReset(email);
            emailService.enviarCorreoRecuperacion(usuario.getEmail(), usuario.getResetToken());
            return ResponseEntity.ok("Se envió un correo para restablecer la contraseña");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestParam String nuevaContraseña) {
        try {
            usuarioService.resetPassword(token, nuevaContraseña);
            return ResponseEntity.ok("Contraseña actualizada con éxito");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
    // <<<<<<< NUEVO: endpoint para validar sesión desde el frontend
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of(
                    "codigo", "TOKEN_FALTANTE",
                    "mensaje", "No se envió token de autenticación"
            ));
        }

        String token = authHeader.substring(7);

        String email;
        try {
            email = jwtUtil.extractUsername(token);
            if (email == null) {
                throw new RuntimeException("Token sin username");
            }
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                    "codigo", "TOKEN_INVALIDO",
                    "mensaje", "Token inválido o expirado"
            ));
        }

        Optional<Usuario> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of(
                    "codigo", "USUARIO_NO_ENCONTRADO",
                    "mensaje", "Usuario no encontrado"
            ));
        }

        Usuario u = userOpt.get();
        return ResponseEntity.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "nombre", u.getNombre(),
                "rol", u.getRol()
        ));
    }
// >>>>>>> FIN NUEVO

}
