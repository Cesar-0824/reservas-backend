    package com.canchas.reservas.controller;

    import com.canchas.reservas.DTO.LoginRequest;
    import com.canchas.reservas.DTO.LoginResponse;
    import com.canchas.reservas.model.Usuario;
    import com.canchas.reservas.repository.UsuarioRepository;
    import com.canchas.reservas.security.JwtUtil;
    import com.canchas.reservas.service.UsuarioService;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.*;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.web.bind.annotation.*;

    import java.util.Optional;

    @RestController
    @RequestMapping("/api/auth")
    @CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
    public class AuthController {

        @Autowired
        private UsuarioRepository repo;

        @Autowired
        private JwtUtil jwtUtil;

        @Autowired
        private PasswordEncoder encoder;

        @Autowired
        private UsuarioService usuarioService;

        @PostMapping("/login")
        public ResponseEntity<?> login(@RequestBody LoginRequest req) {
            Optional<Usuario> userOpt = repo.findByEmail(req.getEmail());
            if (userOpt.isEmpty()) return ResponseEntity.status(401).body("Credenciales inválidas");

            Usuario u = userOpt.get();
            if (!encoder.matches(req.getPassword(), u.getContrasena()))
                return ResponseEntity.status(401).body("Credenciales inválidas");

            String token = jwtUtil.generateToken(u.getEmail(), u.getRol().name());
            return ResponseEntity.ok(new LoginResponse(token, u.getEmail(), u.getId(), u.getNombre(), u.getRol(), u.getFechaRegistro()));
        }
        @PostMapping("/forgot-password")
        public ResponseEntity<?> forgotPassword(@RequestParam String email) {
            try {
                Usuario usuario = usuarioService.generarTokenReset(email);
                // Aquí envías email con enlace: http://tu-frontend/reset-password?token=XXX
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


    }
