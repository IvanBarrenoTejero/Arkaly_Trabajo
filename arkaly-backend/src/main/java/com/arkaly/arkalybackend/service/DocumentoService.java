package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.DocumentoResponseDTO;
import com.arkaly.arkalybackend.dto.EtiquetaDTO;
import com.arkaly.arkalybackend.models.Carpeta;
import com.arkaly.arkalybackend.models.Documento;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.CarpetaRepository;
import com.arkaly.arkalybackend.repositories.DocumentoRepository;
import com.arkaly.arkalybackend.repositories.EtiquetaElementoRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class DocumentoService {

    private final DocumentoRepository documentoRepository;
    private final CarpetaRepository carpetaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EtiquetaElementoRepository etiquetaElementoRepository;

    @Value("${arkaly.storage.path}")
    private String storagePath;

    public DocumentoService(DocumentoRepository documentoRepository,
                            CarpetaRepository carpetaRepository,
                            UsuarioRepository usuarioRepository,
                            EtiquetaElementoRepository etiquetaElementoRepository) {
        this.documentoRepository = documentoRepository;
        this.carpetaRepository = carpetaRepository;
        this.usuarioRepository = usuarioRepository;
        this.etiquetaElementoRepository = etiquetaElementoRepository;
    }

    // CONSULTAS

    // Documentos dentro de una carpeta
    public List<Documento> getDocumentosPorCarpeta(Integer idCarpeta) {
        return documentoRepository.findByIdCarpetaId(idCarpeta);
    }

    // Documentos sin carpeta asignada (nivel raíz)
    public List<Documento> getDocumentosRaiz(Integer idUsuario) {
        return documentoRepository.findByIdUsuarioIdAndIdCarpetaIsNull(idUsuario);
    }

    // Búsqueda de documentos por nombre parcial
    public List<Documento> buscarDocumentos(Integer idUsuario, String nombre) {
        return documentoRepository
                .findByIdUsuarioIdAndNombreOriginalContainingIgnoreCase(idUsuario, nombre);
    }

    // OPERACIONES

    // Guarda el archivo en disco y persiste los metadatos en base de datos
    public Documento subirDocumento(Integer idUsuario, Integer idCarpeta,
                                    String categoria, MultipartFile file) throws IOException {
        Optional<Usuario> resultadoUsuario = usuarioRepository.findById(idUsuario);

        if (resultadoUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultadoUsuario.get();

        Path directorioUsuario = Paths.get(storagePath, String.valueOf(idUsuario));
        Files.createDirectories(directorioUsuario);

        String nombreOriginal = file.getOriginalFilename();
        String extension = nombreOriginal.substring(nombreOriginal.lastIndexOf("."));
        String nombreArchivo = UUID.randomUUID() + extension;

        Files.copy(file.getInputStream(), directorioUsuario.resolve(nombreArchivo));

        Documento documento = new Documento();
        documento.setIdUsuario(usuario);
        documento.setNombreOriginal(nombreOriginal);
        documento.setNombreArchivo(nombreArchivo);
        documento.setRutaRelativa(idUsuario + "/" + nombreArchivo);
        documento.setTipoMime(file.getContentType());
        documento.setTamanioBytes(file.getSize());
        documento.setCategoria(categoria);

        if (idCarpeta != null) {
            Optional<Carpeta> resultadoCarpeta = carpetaRepository.findById(idCarpeta);

            if (resultadoCarpeta.isEmpty()) {
                throw new RuntimeException("Carpeta no encontrada");
            }

            documento.setIdCarpeta(resultadoCarpeta.get());
        }

        return documentoRepository.save(documento);
    }

    // Mueve un documento a otra carpeta; null lo sube al nivel raíz
    public Documento moverDocumento(Integer idDocumento, Integer idCarpetaDestino) {
        Optional<Documento> resultadoDoc = documentoRepository.findById(idDocumento);

        if (resultadoDoc.isEmpty()) {
            throw new RuntimeException("Documento no encontrado");
        }

        Documento documento = resultadoDoc.get();

        if (idCarpetaDestino == null) {
            documento.setIdCarpeta(null);
        } else {
            Optional<Carpeta> resultadoCarpeta = carpetaRepository.findById(idCarpetaDestino);

            if (resultadoCarpeta.isEmpty()) {
                throw new RuntimeException("Carpeta no encontrada");
            }

            documento.setIdCarpeta(resultadoCarpeta.get());
        }

        return documentoRepository.save(documento);
    }

    // Borra el archivo del disco y elimina el registro de base de datos
    public void eliminarDocumento(Integer idDocumento) throws IOException {
        Optional<Documento> resultado = documentoRepository.findById(idDocumento);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Documento no encontrado");
        }

        Documento documento = resultado.get();
        Files.deleteIfExists(Paths.get(storagePath, documento.getRutaRelativa()));
        documentoRepository.delete(documento);
    }

    // Devuelve un documento por su id
    public Documento obtenerPorId(Integer id) {
        Optional<Documento> resultado = documentoRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Documento no encontrado");
        }

        return resultado.get();
    }

    // Lee y devuelve los bytes del archivo físico
    public byte[] descargarArchivo(Documento documento) throws IOException {
        return Files.readAllBytes(Paths.get(storagePath, documento.getRutaRelativa()));
    }

    // MAPPER

    // Convierte un Documento a su DTO, incluyendo la etiqueta si existe
    public DocumentoResponseDTO toDTO(Documento documento) {
        DocumentoResponseDTO dto = new DocumentoResponseDTO();
        dto.setId(documento.getId());
        dto.setNombreOriginal(documento.getNombreOriginal());
        dto.setRutaRelativa(documento.getRutaRelativa());
        dto.setTipoMime(documento.getTipoMime());
        dto.setTamanioBytes(documento.getTamanioBytes());
        dto.setCategoria(documento.getCategoria());
        dto.setFechaSubida(documento.getFechaSubida() != null
                ? documento.getFechaSubida().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null);
        dto.setIdUsuario(documento.getIdUsuario().getId());
        dto.setIdCarpeta(documento.getIdCarpeta() != null
                ? documento.getIdCarpeta().getId() : null);

        etiquetaElementoRepository.findByIdDocumentoId(documento.getId())
                .ifPresent(ee -> {
                    EtiquetaDTO etiquetaDTO = new EtiquetaDTO();
                    etiquetaDTO.setId(ee.getIdEtiqueta().getId());
                    etiquetaDTO.setNombre(ee.getIdEtiqueta().getNombre());
                    etiquetaDTO.setColor(ee.getIdEtiqueta().getColor());
                    dto.setEtiqueta(etiquetaDTO);
                });

        return dto;
    }
}