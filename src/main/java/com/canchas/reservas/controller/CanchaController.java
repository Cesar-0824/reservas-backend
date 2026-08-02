package com.canchas.reservas.controller;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.canchas.reservas.model.Cancha;
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
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        try {
            String urlImagen = null;

            if (imagen != null && !imagen.isEmpty()) {
                urlImagen = subirACloudinary(imagen);
            }

            Cancha nueva = new Cancha();
            nueva.setNombre(nombre);
            nueva.setTipo(tipo);
            nueva.setPrecioHora(precioHora);
            nueva.setImagen(urlImagen);

            Cancha guardada = canchaRepository.save(nueva);
            return ResponseEntity.ok(guardada);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al guardar la imagen: " + e.getMessage());
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
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        try {
            String urlImagen = null;

            if (imagen != null && !imagen.isEmpty()) {
                urlImagen = subirACloudinary(imagen);
            }

            Cancha actualizada = canchaService.actualizarCancha(id, nombre, tipo, precioHora, urlImagen);
            return ResponseEntity.ok(actualizada);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar la imagen: " + e.getMessage());
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

    // Sube el archivo a Cloudinary (carpeta "canchas") y devuelve la URL publica (https, con CDN)
    private String subirACloudinary(MultipartFile imagen) throws java.io.IOException {
        Map<?, ?> resultado = cloudinary.uploader().upload(
                imagen.getBytes(),
                ObjectUtils.asMap("folder", "canchas")
        );
        return (String) resultado.get("secure_url");
    }
}