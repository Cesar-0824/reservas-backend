package com.canchas.reservas.controller;

import com.canchas.reservas.model.Cancha;
import com.canchas.reservas.repository.CanchaRepository;
import com.canchas.reservas.service.CanchaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/canchas")
@CrossOrigin(origins = "*")
public class CanchaController {

    @Autowired
    private CanchaService canchaService;

    @Autowired
    private CanchaRepository canchaRepository;

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + File.separator + "uploads" + File.separator + "canchas" + File.separator;

    @PostMapping("/registrar")
    public ResponseEntity<?> registrar(
            @RequestParam("nombre") String nombre,
            @RequestParam("tipo") String tipo,
            @RequestParam("precio") Double precioHora,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        try {
            String rutaImagen = null;

            if (imagen != null && !imagen.isEmpty()) {
                String nombreArchivo = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();

                File carpeta = new File(UPLOAD_DIR);
                if (!carpeta.exists()) {
                    carpeta.mkdirs(); // Crear carpeta si no existe
                }

                Path rutaCompleta = Paths.get(carpeta.getAbsolutePath(), nombreArchivo);
                imagen.transferTo(rutaCompleta.toFile());

                // Ruta accesible desde el navegador si se configura correctamente
                rutaImagen = "/uploads/canchas/" + nombreArchivo;
            }

            Cancha nueva = new Cancha();
            nueva.setNombre(nombre);
            nueva.setTipo(tipo);
            nueva.setPrecioHora(precioHora);
            nueva.setImagen(rutaImagen);

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
    public Cancha actualizarCancha(
            @PathVariable Integer id,
            @RequestParam("nombre") String nombre,
            @RequestParam("tipo") String tipo,
            @RequestParam("precio") Double precioHora,
            @RequestParam(value = "imagen", required = false) MultipartFile imagen
    ) {
        String imagenUrl = null;

        if (imagen != null && !imagen.isEmpty()) {
            try {
                File carpeta = new File(UPLOAD_DIR);
                if (!carpeta.exists()) {
                    boolean creada = carpeta.mkdirs();
                    if (!creada) {
                        throw new IOException("No se pudo crear el directorio: " + carpeta.getAbsolutePath());
                    }
                }

                String nombreArchivo = UUID.randomUUID().toString() + "_" + imagen.getOriginalFilename();
                Path rutaCompleta = Paths.get(carpeta.getAbsolutePath(), nombreArchivo);
                imagen.transferTo(rutaCompleta.toFile());

                // ✅ URL pública siempre relativa y fija, sin importar la ruta física real
                imagenUrl = "/uploads/canchas/" + nombreArchivo;

            } catch (IOException e) {
                throw new RuntimeException("Error al actualizar imagen: " + e.getMessage());
            }
        }

        return canchaService.actualizarCancha(id, nombre, tipo, precioHora, imagenUrl);
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
}
