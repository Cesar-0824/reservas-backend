package com.canchas.reservas.controller;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/api/sunat")
public class SunatController {

    private static final Logger logger = LoggerFactory.getLogger(SunatController.class);

    @Value("${apis.net.pe.token}")
    private String token;

    private final RestTemplate restTemplate = new RestTemplate();

    @PostConstruct
    public void validarToken() {
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("apis.net.pe.token no está configurado");
        }
        token = token.trim();
        logger.info("Token cargado - longitud: {}, primeros 10 caracteres: {}",
                token.length(), token.substring(0, Math.min(10, token.length())));
    }

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
                    "https://api.decolecta.com/v1/sunat/ruc?numero=" + numero,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            logger.error("Error del proveedor SUNAT para RUC {}: {} - {}", numero, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity
                    .status(HttpStatus.BAD_GATEWAY) // 502, no e.getStatusCode()
                    .body(Map.of(
                            "error", "No se pudo consultar el RUC en el proveedor externo",
                            "detalle", e.getResponseBodyAsString()
                    ));
        }
    }
}