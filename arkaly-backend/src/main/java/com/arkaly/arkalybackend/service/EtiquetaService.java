package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.EtiquetaDTO;
import com.arkaly.arkalybackend.models.*;
import com.arkaly.arkalybackend.repositories.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class EtiquetaService {

    private final EtiquetaRepository etiquetaRepository;
    private final EtiquetaElementoRepository etiquetaElementoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CarpetaRepository carpetaRepository;
    private final DocumentoRepository documentoRepository;

    public EtiquetaService(EtiquetaRepository etiquetaRepository,
                           EtiquetaElementoRepository etiquetaElementoRepository,
                           UsuarioRepository usuarioRepository,
                           CarpetaRepository carpetaRepository,
                           DocumentoRepository documentoRepository) {
        this.etiquetaRepository = etiquetaRepository;
        this.etiquetaElementoRepository = etiquetaElementoRepository;
        this.usuarioRepository = usuarioRepository;
        this.carpetaRepository = carpetaRepository;
        this.documentoRepository = documentoRepository;
    }

    public List<Etiqueta> getEtiquetasUsuario(Integer idUsuario) {
        return etiquetaRepository.findByIdUsuarioId(idUsuario);
    }

    public Etiqueta crearEtiqueta(Integer idUsuario, String nombre, String color) {
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Etiqueta etiqueta = new Etiqueta();
        etiqueta.setIdUsuario(usuario);
        etiqueta.setNombre(nombre);
        etiqueta.setColor(color);
        return etiquetaRepository.save(etiqueta);
    }

    public void eliminarEtiqueta(Integer idEtiqueta) {
        etiquetaElementoRepository.findByIdEtiquetaId(idEtiqueta)
                .forEach(etiquetaElementoRepository::delete);
        etiquetaRepository.deleteById(idEtiqueta);
    }

    public void asignarEtiquetaCarpeta(Integer idEtiqueta, Integer idCarpeta) {
        etiquetaElementoRepository.findByIdCarpetaId(idCarpeta)
                .ifPresent(etiquetaElementoRepository::delete);

        Etiqueta etiqueta = etiquetaRepository.findById(idEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));
        Carpeta carpeta = carpetaRepository.findById(idCarpeta)
                .orElseThrow(() -> new RuntimeException("Carpeta no encontrada"));

        EtiquetaElemento elemento = new EtiquetaElemento();
        elemento.setIdEtiqueta(etiqueta);
        elemento.setIdCarpeta(carpeta);
        etiquetaElementoRepository.save(elemento);
    }

    public void asignarEtiquetaDocumento(Integer idEtiqueta, Integer idDocumento) {
        etiquetaElementoRepository.findByIdDocumentoId(idDocumento)
                .ifPresent(etiquetaElementoRepository::delete);

        Etiqueta etiqueta = etiquetaRepository.findById(idEtiqueta)
                .orElseThrow(() -> new RuntimeException("Etiqueta no encontrada"));
        Documento documento = documentoRepository.findById(idDocumento)
                .orElseThrow(() -> new RuntimeException("Documento no encontrado"));

        EtiquetaElemento elemento = new EtiquetaElemento();
        elemento.setIdEtiqueta(etiqueta);
        elemento.setIdDocumento(documento);
        etiquetaElementoRepository.save(elemento);
    }

    public void quitarEtiquetaCarpeta(Integer idCarpeta) {
        etiquetaElementoRepository.findByIdCarpetaId(idCarpeta)
                .ifPresent(etiquetaElementoRepository::delete);
    }

    public void quitarEtiquetaDocumento(Integer idDocumento) {
        etiquetaElementoRepository.findByIdDocumentoId(idDocumento)
                .ifPresent(etiquetaElementoRepository::delete);
    }

    public EtiquetaDTO toDTO(Etiqueta etiqueta) {
        EtiquetaDTO dto = new EtiquetaDTO();
        dto.setId(etiqueta.getId());
        dto.setNombre(etiqueta.getNombre());
        dto.setColor(etiqueta.getColor());
        return dto;
    }
}