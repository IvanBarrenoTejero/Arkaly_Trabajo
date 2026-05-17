package com.arkaly.arkalybackend.clients;

import com.arkaly.arkalybackend.dto.PuntoHistoricoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class YFinanceClient {

    private static final List<String> CAMPOS_PRECIO = List.of(
            "currentPrice", "regularMarketPrice", "price", "lastPrice", "navPrice");
    private static final List<String> CAMPOS_CLOSE = List.of("Close", "close", "adjClose");
    private static final List<String> CAMPOS_FECHA =
            List.of("Date", "Datetime", "date", "datetime", "Timestamp", "timestamp");

    @Value("${yfinance.base-url:http://localhost:8000/api/v1}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    // Formato de símbolo

    public String formatearSimbolo(String simbolo, String tipoActivo) {
        return "CRIPTO".equalsIgnoreCase(tipoActivo) && !simbolo.contains("-")
                ? simbolo.toUpperCase() + "-USD"
                : simbolo.toUpperCase();
    }

    public boolean esDivisaUSD(String tipoActivo) {
        return "CRIPTO".equalsIgnoreCase(tipoActivo);
    }

    // Precio actual

    public Optional<BigDecimal> getPrecioActual(String simbolo) {
        return fetchJson("/quote/" + simbolo)
                .flatMap(json -> extraerPrimerCampo(json, CAMPOS_PRECIO))
                .map(val -> val.setScale(2, RoundingMode.HALF_UP));
    }

    // Tipo de cambio

    public Optional<BigDecimal> getTipoCambioUSDtoEUR() {
        return fetchJson("/quote/EURUSD=X")
                .flatMap(json -> extraerPrimerCampo(json, CAMPOS_PRECIO.subList(0, 3)))
                .map(val -> BigDecimal.ONE.divide(val, 6, RoundingMode.HALF_UP))
                .map(tc -> { log.info("Tipo cambio 1 USD = {} EUR", tc); return tc; });
    }

    // Histórico

    public List<BigDecimal> getHistorico(String simbolo) {
        return getHistorico(simbolo, "1mo", "1d");
    }

    public List<PuntoHistoricoDTO> getHistoricoConFechas(String simbolo,
                                                         String period,
                                                         String interval) {
        String path = "/history/" + simbolo + "?period=" + period + "&interval=" + interval;

        return fetchJson(path)
                .map(json -> json.get("rows"))
                .filter(rows -> rows != null && rows.isArray())
                .map(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(row -> {
                            Optional<BigDecimal> precio = extraerPrimerCampo(row, CAMPOS_CLOSE);
                            if (precio.isEmpty()) return Optional.<PuntoHistoricoDTO>empty();

                            String fecha = CAMPOS_FECHA.stream()
                                    .filter(row::has)
                                    .findFirst()
                                    .map(f -> row.get(f).asText(""))
                                    .orElse("");

                            if (fecha.isEmpty()) return Optional.<PuntoHistoricoDTO>empty();

                            return Optional.of(new PuntoHistoricoDTO(
                                    fecha,
                                    precio.get().setScale(4, RoundingMode.HALF_UP)));
                        })
                        .flatMap(Optional::stream)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public List<BigDecimal> getHistorico(String simbolo, String period, String interval) {
        String path = "/history/" + simbolo + "?period=" + period + "&interval=" + interval;

        return fetchJson(path)
                .map(json -> json.get("rows"))
                .filter(rows -> rows != null && rows.isArray())
                .map(rows -> StreamSupport.stream(rows.spliterator(), false)
                        .map(row -> extraerPrimerCampo(row, CAMPOS_CLOSE))
                        .flatMap(Optional::stream)
                        .map(val -> val.setScale(4, RoundingMode.HALF_UP))
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Búsqueda

    public List<Map<String, String>> buscarActivos(String query) {
        return fetchJson("/search/" + query + "?limit=10")
                .map(json -> json.get("results"))
                .filter(results -> results != null && results.isArray())
                .map(results -> StreamSupport.stream(results.spliterator(), false)
                        .map(this::mapearResultado)
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    // Métodos privados reutilizables

    private Optional<JsonNode> fetchJson(String path) {
        String url = baseUrl + path;
        log.debug("GET → {}", url);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(request,
                    HttpResponse.BodyHandlers.ofString());

            log.debug("Status {}: {}", resp.statusCode(),
                    resp.body().substring(0, Math.min(200, resp.body().length())));

            return Optional.of(mapper.readTree(resp.body()));

        } catch (Exception e) {
            log.error("Error en {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> extraerPrimerCampo(JsonNode json, List<String> campos) {
        return campos.stream()
                .filter(c -> json.has(c) && !json.get(c).isNull())
                .findFirst()
                .map(c -> new BigDecimal(json.get(c).asText()));
    }

    private Map<String, String> mapearResultado(JsonNode r) {
        String tipoActivo = switch (r.path("quoteType").asText("")) {
            case "CRYPTOCURRENCY" -> "CRIPTO";
            case "ETF", "MUTUALFUND" -> "FONDO";
            default -> "ACCION";
        };
        return Map.of(
                "simbolo", r.path("symbol").asText(""),
                "nombre", r.path("shortname").asText(r.path("longname").asText("")),
                "tipoActivo", tipoActivo
        );
    }
}