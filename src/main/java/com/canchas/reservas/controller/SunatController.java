package com.canchas.reservas.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/sunat")
public class SunatController {

    @Value("${apis.net.pe.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/ruc/{numero}")
    public ResponseEntity<?> consultarRuc(@PathVariable String numero) {
        if (numero == null || !numero.matches("\\d{11}")) {
            return ResponseEntity.badRequest().body("RUC inválido");
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.apis.net.pe/v2/sunat/ruc?numero=" + numero,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No se encontraron datos para ese RUC.");
        }
    }
}