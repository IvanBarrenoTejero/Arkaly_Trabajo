package com.arkaly.desktop.services;

import com.arkaly.desktop.utils.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InformeService {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ── TRANSACCIONES ────────────────────────────────────────────

    public static JsonNode getTransacciones(String desde, String hasta) throws Exception {
        return mapper.readTree(ApiClient.getAuth(
                "/transacciones?desde=" + desde + "&hasta=" + hasta));
    }

    public static void guardarTransaccion(String tipo, Integer idCategoria,
                                          String cantidad, String descripcion,
                                          LocalDate fecha) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("tipo",        tipo);
        body.put("idCategoria", idCategoria);
        body.put("cantidad",    new BigDecimal(cantidad.replace(",", ".")));
        body.put("descripcion", descripcion);
        body.put("fecha",       fecha.toString());
        ApiClient.postAuth("/transacciones", mapper.writeValueAsString(body));
    }

    public static void eliminarTransaccion(int id) throws Exception {
        ApiClient.deleteAuth("/transacciones/" + id);
    }

    // ── CATEGORÍAS ───────────────────────────────────────────────

    public static JsonNode getCategorias() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/transacciones/categorias"));
    }

    public static void crearCategoria(String nombre) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("nombre", nombre);
        ApiClient.postAuth("/transacciones/categorias", mapper.writeValueAsString(body));
    }

    public static void eliminarCategoria(int id) throws Exception {
        ApiClient.deleteAuth("/transacciones/categorias/" + id);
    }

    // ── GRÁFICOS ─────────────────────────────────────────────────

    public static JsonNode getGastosPorCategoria(String desde, String hasta) throws Exception {
        return mapper.readTree(ApiClient.getAuth(
                "/informes/gastos-por-categoria?desde=" + desde + "&hasta=" + hasta));
    }

    public static JsonNode getIngresosVsGastos(String desde, String hasta) throws Exception {
        return mapper.readTree(ApiClient.getAuth(
                "/informes/ingresos-vs-gastos?desde=" + desde + "&hasta=" + hasta));
    }

    public static JsonNode getProyeccion() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/informes/proyeccion"));
    }

    public static void actualizarRegla(String regla) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("regla", regla);
        ApiClient.putAuth("/informes/perfil/regla", mapper.writeValueAsString(body));
    }
}