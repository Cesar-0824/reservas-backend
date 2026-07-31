package com.canchas.reservas.service;

import com.canchas.reservas.model.Rol;
import com.canchas.reservas.model.Usuario;
import com.canchas.reservas.repository.UsuarioRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UsuarioService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- Autenticación Spring Security ---
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con email: " + email));

        return User.builder()
                .username(usuario.getEmail())
                .password(usuario.getContrasena())
                .roles(usuario.getRol().name()) // ADMIN o USER
                .build();
    }

    // --- Guardar usuario en BD y Firebase ---
    public Usuario guardarUsuario(Usuario usuario) throws Exception {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        if (usuario.getRol() == null) {
            usuario.setRol(Rol.usuario);
        }

        // Guardar contraseña en claro para Firebase
        String contraseñaOriginal = usuario.getContrasena();

        // Encriptar para BD
        usuario.setContrasena(passwordEncoder.encode(contraseñaOriginal));

        // Guardar en BD
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        try {
            // Crear usuario en Firebase con contraseña en claro
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(usuario.getEmail())
                    .setPassword(contraseñaOriginal);
            UserRecord firebaseUser = FirebaseAuth.getInstance().createUser(request);
            System.out.println("Usuario creado en Firebase: " + firebaseUser.getUid());
        } catch (Exception e) {
            e.printStackTrace();
            // SOLO log, NO borrar BD
            System.err.println("No se pudo crear el usuario en Firebase: " + e.getMessage());
        }

        return usuarioGuardado;
    }

    // --- Listar todos ---
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    // --- Buscar por email ---
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // --- Actualizar usuario ---
    public Usuario actualizarUsuario(Integer id, Usuario datosActualizados) {
        return usuarioRepository.findById(id).map(usuario -> {
            usuario.setNombre(datosActualizados.getNombre());
            usuario.setEmail(datosActualizados.getEmail());
            if (datosActualizados.getContrasena() != null && !datosActualizados.getContrasena().isBlank()) {
                usuario.setContrasena(passwordEncoder.encode(datosActualizados.getContrasena()));
            }
            usuario.setRol(datosActualizados.getRol());
            return usuarioRepository.save(usuario);
        }).orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + id));
    }

    // --- Eliminar usuario ---
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new IllegalArgumentException("No existe el usuario con ID: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    // --- Generar token de reseteo ---
    public Usuario generarTokenReset(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró usuario con ese correo"));

        String token = UUID.randomUUID().toString();
        usuario.setResetToken(token);
        usuario.setResetTokenExpiration(LocalDateTime.now().plusHours(1)); // token válido 1 hora

        return usuarioRepository.save(usuario);
    }

    // --- Resetear contraseña ---
    public Usuario resetPassword(String token, String nuevaContraseña) {
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token inválido"));

        if (usuario.getResetTokenExpiration() == null || usuario.getResetTokenExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }

        usuario.setContrasena(passwordEncoder.encode(nuevaContraseña));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiration(null);

        return usuarioRepository.save(usuario);
    }

    // --- Métodos auxiliares ---
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }

    public String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
