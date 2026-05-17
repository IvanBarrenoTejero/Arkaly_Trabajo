package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.DocumentoResponseDTO;
import com.arkaly.arkalybackend.models.Documento;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.DocumentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documentos")
public class DocumentoController {

    private final DocumentoService documentoService;
    private final UsuarioRepository usuarioRepository;

    public DocumentoController(DocumentoService documentoService,
                               UsuarioRepository usuarioRepository) {
        this.documentoService = documentoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/raiz")
    public ResponseEntity<List<DocumentoResponseDTO>> getDocumentosRaiz(
            Authentication authentication) {
        Integer idUsuario = getIdUsuario(authentication);
        List<Documento> documentos = documentoService.getDocumentosRaiz(idUsuario);
        List<DocumentoResponseDTO> dtos = documentos.stream()
                .map(documentoService::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/carpeta/{id}")
    public ResponseEntity<List<DocumentoResponseDTO>> getDocumentosPorCarpeta(
            @PathVariable Integer id) {
        List<Documento> documentos = documentoService.getDocumentosPorCarpeta(id);
        List<DocumentoResponseDTO> dtos = documentos.stream()
                .map(documentoService::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<DocumentoResponseDTO>> buscarDocumentos(
            @RequestParam String nombre,
            Authentication authentication) {
        Integer idUsuario = getIdUsuario(authentication);
        List<Documento> documentos = documentoService.buscarDocumentos(idUsuario, nombre);
        List<DocumentoResponseDTO> dtos = documentos.stream()
                .map(documentoService::toDTO)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    @PostMapping("/subir")
    public ResponseEntity<DocumentoResponseDTO> subirDocumento(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) Integer idCarpeta,
            @RequestParam(required = false) String categoria,
            Authentication authentication) {
        try {
            Integer idUsuario = getIdUsuario(authentication);
            Documento documento = documentoService.subirDocumento(
                    idUsuario, idCarpeta, categoria, file);
            return ResponseEntity.ok(documentoService.toDTO(documento));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/{id}/mover")
    public ResponseEntity<DocumentoResponseDTO> moverDocumento(
            @PathVariable Integer id,
            @RequestParam(required = false) Integer idCarpetaDestino) {
        Documento documento = documentoService.moverDocumento(id, idCarpetaDestino);
        return ResponseEntity.ok(documentoService.toDTO(documento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDocumento(@PathVariable Integer id) {
        try {
            documentoService.eliminarDocumento(id);
            return ResponseEntity.ok("Documento eliminado correctamente");
        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Error al eliminar el archivo");
        }
    }

    private Integer getIdUsuario(Authentication authentication) {
        String email = authentication.getName();
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"))
                .getId();
    }

    @GetMapping("/descargar/{id}")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable Integer id) {
        try {
            Documento documento = documentoService.obtenerPorId(id);
            byte[] contenido = documentoService.descargarArchivo(documento);

            return ResponseEntity.ok()
                    .header("Content-Disposition",
                            "attachment; filename=\"" + documento.getNombreOriginal() + "\"")
                    .header("Content-Type", documento.getTipoMime())
                    .body(contenido);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}