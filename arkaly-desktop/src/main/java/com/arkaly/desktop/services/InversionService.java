package com.arkaly.desktop.services;

import com.arkaly.desktop.utils.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InversionService {

    private static final ObjectMapper mapper = new ObjectMapper();

    // ── RESUMEN ──────────────────────────────────────────────────

    public static JsonNode getResumen() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/inversiones/resumen"));
    }

    // ── FAVORITOS ────────────────────────────────────────────────

    public static JsonNode getSiguiendo() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/inversiones/favoritos"));
    }

    public static void agregarFavorito(String simbolo, String nombre, String tipo) throws Exception {
        String json = String.format(
                "{\"simbolo\":\"%s\",\"nombre\":\"%s\",\"tipoActivo\":\"%s\"}",
                simbolo, nombre, tipo);
        ApiClient.postAuth("/inversiones/favoritos", json);
    }

    public static void eliminarFavorito(int id) throws Exception {
        ApiClient.deleteAuth("/inversiones/favoritos/" + id);
    }

    // ── POSICIONES ───────────────────────────────────────────────

    public static JsonNode getPosiciones(String tipo) throws Exception {
        String url = "TODOS".equals(tipo)
                ? "/inversiones/posiciones"
                : "/inversiones/posiciones/" + tipo;
        return mapper.readTree(ApiClient.getAuth(url));
    }

    public static Set<String> getSimbolosConPosicion() throws Exception {
        Set<String> set = new HashSet<>();
        for (JsonNode p : mapper.readTree(ApiClient.getAuth("/inversiones/posiciones")))
            set.add(p.path("simbolo").asText());
        return set;
    }

    // ── MERCADO ──────────────────────────────────────────────────

    public static List<JsonNode> getMercadoPorTipo(String tipo) throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(ApiClient.getAuth("/activos/mercado/tipo/" + tipo)))
            lista.add(n);
        return lista;
    }

    public static List<JsonNode> buscar(String query) throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(ApiClient.getAuth("/activos/buscar/" + query)))
            lista.add(n);
        return lista;
    }

    // ── HISTORICO ────────────────────────────────────────────────

    public static JsonNode getHistorico(String simbolo, String tipo,
                                        String period, String interval) throws Exception {
        String url = "/inversiones/historico/" + simbolo
                + "?tipo=" + tipo + "&period=" + period + "&interval=" + interval;
        return mapper.readTree(ApiClient.getAuth(url));
    }

    // ── OPERACIONES ──────────────────────────────────────────────

    public static void registrarOperacion(String simbolo, String tipo,
                                          String tipoOp, String cantidad,
                                          String precio, String notas) throws Exception {
        String json = String.format(
                "{\"simbolo\":\"%s\",\"tipoActivo\":\"%s\",\"tipoOperacion\":\"%s\"," +
                        "\"cantidad\":\"%s\",\"precioUnitario\":\"%s\",\"notas\":\"%s\"}",
                simbolo, tipo, tipoOp, cantidad, precio, notas);
        ApiClient.postAuth("/inversiones/operacion", json);
    }

    public static JsonNode getOperaciones(String tipo) throws Exception {
        String url = "TODOS".equals(tipo)
                ? "/inversiones/operaciones"
                : "/inversiones/operaciones/" + tipo;
        return mapper.readTree(ApiClient.getAuth(url));
    }

    // ── ALERTAS ──────────────────────────────────────────────────

    public static JsonNode getAlertas() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/alertas"));
    }

    public static void crearAlerta(String simbolo, String tipo,
                                   String condicion, String precio) throws Exception {
        String json = String.format(
                "{\"simbolo\":\"%s\",\"tipoActivo\":\"%s\",\"condicion\":\"%s\",\"precioObjetivo\":\"%s\"}",
                simbolo, tipo, condicion, precio);
        ApiClient.postAuth("/alertas", json);
    }

    public static void toggleAlerta(int id) throws Exception {
        ApiClient.putAuth("/alertas/" + id + "/toggle", "{}");
    }

    public static void eliminarAlerta(int id) throws Exception {
        ApiClient.deleteAuth("/alertas/" + id);
    }

    // ── METAS ────────────────────────────────────────────────────

    public static JsonNode getMetas() throws Exception {
        return mapper.readTree(ApiClient.getAuth("/metas"));
    }

    public static void crearMeta(String nombre, String cantidad, String fecha) throws Exception {
        String json = String.format(
                "{\"nombre\":\"%s\",\"cantidadObjetivo\":\"%s\",\"fechaLimite\":\"%s\"}",
                nombre, cantidad, fecha);
        ApiClient.postAuth("/metas", json);
    }

    public static void actualizarMeta(int id, double nuevaCantidad) throws Exception {
        ApiClient.putAuth("/metas/" + id,
                String.format("{\"cantidadActual\":\"%.2f\"}", nuevaCantidad));
    }

    public static void eliminarMeta(int id) throws Exception {
        ApiClient.deleteAuth("/metas/" + id);
    }
}