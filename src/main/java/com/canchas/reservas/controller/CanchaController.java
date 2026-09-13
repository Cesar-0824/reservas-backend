package com.canchas.reservas.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.canchas.reservas.model.Cancha;
import com.canchas.reservas.model.EstadoCancha;
import com.canchas.reservas.repository.CanchaRepository;
import com.canchas.reservas.service.CanchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/canchas")
@CrossOrigin(origins = "*")
public class CanchaController {

    @Autowired
    private CanchaService canchaService;

    @Autowired
    private CanchaRepository canchaRepository;

    @Autowired
    private Cloudinary cloudinary;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(
            @RequestParam("nombre") String nombre,
            @RequestParam("tipo") String tipo,
            @RequestParam("precio") Double precioHora,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "modalidad", required = false) String modalidad,
            @RequestParam(value = "dimensiones", required = false) String dimensiones,
            @RequestParam(value = "tipoSuperficie", required = false) String tipoSuperficie,
            @RequestParam(value = "iluminacion", required = false) String iluminacion,
            @RequestParam(value = "caracteristicas", required = false) String caracteristicas,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) {
        try {
            String urlImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                urlImagen = subirACloudinary(imagen);
            }

            Cancha guardada = canchaService.registrarCancha(
                    nombre, tipo, precioHora, urlImagen,
                    modalidad, dimensiones, tipoSuperficie, iluminacion, caracteristicas, descripcion
            );
            return ResponseEntity.ok(guardada);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la cancha: " + e.getMessage());
        }
    }

    @GetMapping
    public List<Cancha> listar() {
        return canchaService.listarCanchas();
    }

    @PutMapping("/actualizar/{id}")
    public ResponseEntity<?> actualizarCancha(
            @PathVariable Integer id,
            @RequestParam("nombre") String nombre,
            @RequestParam("tipo") String tipo,
            @RequestParam("precio") Double precioHora,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen,
            @RequestParam(value = "modalidad", required = false) String modalidad,
            @RequestParam(value = "dimensiones", required = false) String dimensiones,
            @RequestParam(value = "tipoSuperficie", required = false) String tipoSuperficie,
            @RequestParam(value = "iluminacion", required = false) String iluminacion,
            @RequestParam(value = "caracteristicas", required = false) String caracteristicas,
            @RequestParam(value = "descripcion", required = false) String descripcion
    ) {
        try {
            String urlImagen = null;
            if (imagen != null && !imagen.isEmpty()) {
                urlImagen = subirACloudinary(imagen);
            }

            Cancha actualizada = canchaService.actualizarCancha(
                    id, nombre, tipo, precioHora, urlImagen,
                    modalidad, dimensiones, tipoSuperficie, iluminacion, caracteristicas, descripcion
            );
            return ResponseEntity.ok(actualizada);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la cancha: " + e.getMessage());
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> cambiarEstado(
            @PathVariable Integer id,
            @RequestParam EstadoCancha estado,
            @RequestParam(required = false) String motivo,
            @RequestParam(required = false) String observacion
    ) {
        try {
            Cancha actualizada = canchaService.cambiarEstado(id, estado, motivo, observacion);
            return ResponseEntity.ok(actualizada);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al cambiar el estado: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cancha> obtenerCanchaPorId(@PathVariable Integer id) {
        return canchaRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable Integer id) {
        canchaService.eliminarCancha(id);
    }

    private String subirACloudinary(MultipartFile imagen) throws java.io.IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(
                imagen.getBytes(),
                ObjectUtils.asMap("folder", "canchas")
        );
        return (String) resultado.get("secure_url");
    }
}