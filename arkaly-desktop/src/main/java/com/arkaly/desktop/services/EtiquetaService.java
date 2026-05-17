package com.arkaly.desktop.services;

import com.arkaly.desktop.utils.ApiClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class EtiquetaService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<JsonNode> getAll() throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(ApiClient.getAuth("/etiquetas"))) lista.add(n);
        return lista;
    }

    public static void crear(String nombre, String color) throws Exception {
        ApiClient.postAuth("/etiquetas",
                "{\"nombre\":\"" + nombre + "\",\"color\":\"" + color + "\"}");
    }

    public static void asignarACarpeta(int idCarpeta, int idEtiqueta) throws Exception {
        ApiClient.postAuth("/etiquetas/asignar-carpeta/" + idCarpeta,
                "{\"idEtiqueta\":" + idEtiqueta + "}");
    }

    public static void quitarDeCarpeta(int idCarpeta) throws Exception {
        ApiClient.deleteAuth("/etiquetas/quitar-carpeta/" + idCarpeta);
    }

    public static void asignarADocumento(int idDocumento, int idEtiqueta) throws Exception {
        ApiClient.postAuth("/etiquetas/asignar-documento/" + idDocumento,
                "{\"idEtiqueta\":" + idEtiqueta + "}");
    }

    public static void quitarDeDocumento(int idDocumento) throws Exception {
        ApiClient.deleteAuth("/etiquetas/quitar-documento/" + idDocumento);
    }
}