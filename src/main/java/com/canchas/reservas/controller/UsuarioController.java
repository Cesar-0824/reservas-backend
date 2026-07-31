package com.canchas.reservas.controller;

import com.canchas.reservas.DTO.UsuarioDTO;
import com.canchas.reservas.model.Rol;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.UsuarioRepository;
import com.canchas.reservas.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.canchas.reservas.service.FirebaseService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/api/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private FirebaseService firebaseService;

    // 🔐 Solo ADMIN puede registrar usuarios

    @PostMapping("/registrar")
    public ResponseEntity<?> registrarUsuario(@RequestBody UsuarioDTO usuarioDTO) {
        try {
            if (usuarioService.existsByEmail(usuarioDTO.getEmail())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El correo ya está registrado"));
            }

            Usuario usuario = new Usuario();
            usuario.setNombre(usuarioDTO.getNombre());
            usuario.setEmail(usuarioDTO.getEmail());
            usuario.setContrasena(usuarioDTO.getContrasena()); // dejar en claro, se encripta en el Service
            usuario.setRol(Rol.usuario);

            Usuario usuarioGuardado = usuarioService.guardarUsuario(usuario);

            return ResponseEntity.ok(Map.of(
                    "message", "Usuario registrado correctamente",
                    "usuario", usuarioGuardado
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al registrar usuario: " + e.getMessage()));
        }
    }

    // 🔐 Listar usuarios solo para ADMIN
    @PreAuthorize("hasRole('admin')")
    @GetMapping
    public ResponseEntity<?> listarUsuarios() {
        try {
            return ResponseEntity.ok(usuarioService.listarUsuarios());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 🔐 Actualizar usuario por ID (solo ADMIN)
    @PreAuthorize("hasRole('admin')")
    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Integer id, @RequestBody Usuario usuarioActualizado) {
        try {
            Usuario usuario = usuarioService.actualizarUsuario(id, usuarioActualizado);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // 🔐 Eliminar usuario por ID (solo ADMIN)
    @PreAuthorize("hasRole('admin')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Integer id) {
        try {
            usuarioService.eliminarUsuario(id);
            return ResponseEntity.noContent().build(); // 204
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/validarCorreo")
    @CrossOrigin(origins = "*") // permite que se llame desde React
    public ResponseEntity<Map<String, Object>> validarCorreo(@RequestBody Map<String, String> request) {
        String correo = request.get("correo");
        Map<String, Object> response = new HashMap<>();

        try {
            // 1️⃣ Verificar en BD
            boolean existeBD = usuarioService.existsByEmail(correo);
            if (!existeBD) {
                response.put("estado", false);
                response.put("mensaje", "El correo no está registrado o credenciales invalidas");
                return ResponseEntity.ok(response);
            }

            // 2️⃣ Verificar en Firebase
            boolean existeFirebase = firebaseService.existeCorreo(correo);
            if (!existeFirebase) {
                response.put("estado", false);
                response.put("mensaje", "El correo no está registrado o credenciales invalidas");
                return ResponseEntity.ok(response);
            }

            // ✅ Si pasa ambas validaciones
            response.put("estado", true);
            response.put("mensaje", "Correo válido");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("estado", false);
            response.put("mensaje", "Error en la validación: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

}
