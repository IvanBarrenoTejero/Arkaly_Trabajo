package com.arkaly.desktop.services;

import com.arkaly.desktop.utils.ApiClient;
import com.arkaly.desktop.utils.SessionManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DocumentoService {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<JsonNode> getPorCarpeta(int idCarpeta) throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(ApiClient.getAuth("/documentos/carpeta/" + idCarpeta)))
            lista.add(n);
        return lista;
    }

    public static List<JsonNode> buscar(String nombre) throws Exception {
        List<JsonNode> lista = new ArrayList<>();
        for (JsonNode n : mapper.readTree(ApiClient.getAuth("/documentos/buscar?nombre=" + nombre)))
            lista.add(n);
        return lista;
    }

    public static byte[] descargar(int id) throws Exception {
        return ApiClient.getBytesAuth("/documentos/descargar/" + id);
    }

    public static void eliminar(int id) throws Exception {
        ApiClient.deleteAuth("/documentos/" + id);
    }

    public static int subir(Path archivo, String categoria, int idCarpeta) throws Exception {
        String boundary = "----Boundary" + System.currentTimeMillis();
        byte[] fileBytes = Files.readAllBytes(archivo);
        String mimeType = Files.probeContentType(archivo);
        if (mimeType == null) mimeType = "application/octet-stream";

        String CRLF = "\r\n";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        String filePart = "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"file\"; filename=\"" +
                archivo.getFileName() + "\"" + CRLF +
                "Content-Type: " + mimeType + CRLF + CRLF;
        baos.write(filePart.getBytes());
        baos.write(fileBytes);
        baos.write(CRLF.getBytes());

        String categoriaPart = "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"categoria\"" + CRLF + CRLF +
                categoria.trim() + CRLF;
        baos.write(categoriaPart.getBytes());

        String carpetaPart = "--" + boundary + CRLF +
                "Content-Disposition: form-data; name=\"idCarpeta\"" + CRLF + CRLF +
                idCarpeta + CRLF;
        baos.write(carpetaPart.getBytes());
        baos.write(("--" + boundary + "--" + CRLF).getBytes());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8081/api/documentos/subir"))
                .header("Authorization", "Bearer " + SessionManager.getToken())
                .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                .POST(HttpRequest.BodyPublishers.ofByteArray(baos.toByteArray()))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient()
                .send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode();
    }
}