package com.canchas.reservas.service;

import com.canchas.reservas.model.Pago;
import com.canchas.reservas.model.Reserva;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;

@Service
public class MercadoPagoService {

    // Access Token Sandbox (modo prueba)
    private static final String ACCESS_TOKEN = "TEST-320296471233317-081122-b4ad40583bb1564c09da42049eb68675-1169757677";
    private static final String MP_API_BASE = "https://api.mercadopago.com";

    private final RestTemplate restTemplate;

    public MercadoPagoService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Crea una preferencia de pago en MercadoPago y retorna el link para el checkout.
     *
     * @param titulo       Descripción del pago (ej. "Reserva Cancha #123")
     * @param cantidad     Cantidad de items (normalmente 1)
     * @param precio       Precio unitario
     * @param backUrl      URL a la que MercadoPago redirige tras el pago (success, failure, pending)
     * @param reservaId    Id de la reserva para trackear con external_reference
     * @return             URL para realizar el pago (init_point)
     * @throws Exception   Si hay error al crear preferencia
     */
    public String crearPreferencia(String titulo, int cantidad, double precio, String backUrl, Integer reservaId) throws Exception {
        String url = MP_API_BASE + "/checkout/preferences";

        JSONObject body = new JSONObject();

        // Item o producto a pagar
        JSONObject item = new JSONObject();
        item.put("title", titulo);
        item.put("quantity", cantidad);
        item.put("unit_price", precio);

        body.put("items", new org.json.JSONArray().put(item));

        // URLs de retorno tras el pago
        JSONObject backUrls = new JSONObject();
        backUrls.put("success", backUrl);
        backUrls.put("failure", backUrl);
        backUrls.put("pending", backUrl);
        body.put("back_urls", backUrls);

        // Para redireccionar automáticamente si el pago es aprobado
        body.put("auto_return", "approved");

        // Referencia externa para identificar la reserva
        body.put("external_reference", reservaId.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(ACCESS_TOKEN);

        HttpEntity<String> entity = new HttpEntity<>(body.toString(), headers);

        ResponseEntity<String> response = restTemplate.postForEntity(url, entity, String.class);

        if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
            JSONObject responseBody = new JSONObject(response.getBody());
            return responseBody.getString("init_point");  // Link de pago para abrir en frontend
        } else {
            throw new Exception("Error creando preferencia MercadoPago: " + response.getBody());
        }
    }

    /**
     * Consulta detalles de un pago por su ID en MercadoPago.
     * Convierte la respuesta a objeto Pago, con estado y monto.
     *
     * @param paymentId    ID del pago MercadoPago
     * @return             Pago con estado y monto, asociado a reserva
     * @throws Exception   Si ocurre error en la consulta
     */
    public Pago obtenerPagoPorId(String paymentId) throws Exception {
        // Para testing local, puedes simular pagos ficticios aquí
        if ("fake-payment-id-1".equals(paymentId)) {
            Pago pagoFicticio = new Pago();
            pagoFicticio.setId(999999);
            pagoFicticio.setEstado(Pago.EstadoPago.exitoso);

            Reserva reserva = new Reserva();
            reserva.setId(5);
            pagoFicticio.setReserva(reserva);
            pagoFicticio.setMonto(100.0);

            return pagoFicticio;
        }

        String url = MP_API_BASE + "/v1/payments/" + paymentId;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(ACCESS_TOKEN);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            JSONObject payment = new JSONObject(response.getBody());

            Pago pago = new Pago();
            pago.setId(Integer.parseInt(payment.getString("id")));

            String statusStr = payment.getString("status").toLowerCase();
            Pago.EstadoPago estadoPago;
            switch (statusStr) {
                case "approved":
                    estadoPago = Pago.EstadoPago.exitoso;
                    break;
                case "pending":
                    estadoPago = Pago.EstadoPago.pendiente;
                    break;
                case "rejected":
                case "cancelled":
                    estadoPago = Pago.EstadoPago.fallido;
                    break;
                default:
                    estadoPago = Pago.EstadoPago.fallido;
            }
            pago.setEstado(estadoPago);

            pago.setMonto(payment.getDouble("transaction_amount"));

            String externalReference = payment.optString("external_reference", null);
            if (externalReference != null && !externalReference.isEmpty()) {
                try {
                    Reserva reserva = new Reserva();
                    reserva.setId(Integer.parseInt(externalReference));
                    pago.setReserva(reserva);
                } catch (NumberFormatException ignored) {
                }
            }

            return pago;

        } else {
            throw new Exception("Error obteniendo pago MercadoPago: " + response.getBody());
        }
    }
}
