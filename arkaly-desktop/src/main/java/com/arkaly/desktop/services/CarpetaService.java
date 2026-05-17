package com.arkaly.desktop.services;

import com.arkaly.desktop.utils.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class CarpetaService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<JsonNode> getRaiz() throws Exception {
        return parsearArray(ApiClient.getAuth("/carpetas/raiz"));
    }

    public static List<JsonNode> getSubcarpetas(int idPadre) throws Exception {
        return parsearArray(ApiClient.getAuth("/carpetas/" + idPadre + "/subcarpetas"));
    }

    public static void crear(String nombre, Integer idPadre) throws Exception {
        String idPadreStr = idPadre != null ? ",\"idCarpetaPadre\":\"" + idPadre + "\"" : "";
        ApiClient.postAuth("/carpetas", "{\"nombre\":\"" + nombre + "\"" + idPadreStr + "}");
    }

    public static void eliminar(int id) throws Exception {
        ApiClient.deleteAuth("/carpetas/" + id);
    }

    public static List<JsonNode> buscar(String nombre) throws Exception {
        return parsearArray(ApiClient.getAuth("/carpetas/buscar?nombre=" + nombre));
    }

    private static List<JsonNode> parsearArray(String json) throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(json)) lista.add(n);
        return lista;
    }
}