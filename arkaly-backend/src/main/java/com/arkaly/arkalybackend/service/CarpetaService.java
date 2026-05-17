package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.CarpetaDTO;
import com.arkaly.arkalybackend.dto.EtiquetaDTO;
import com.arkaly.arkalybackend.models.Carpeta;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.CarpetaRepository;
import com.arkaly.arkalybackend.repositories.EtiquetaElementoRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
public class CarpetaService {

    private final CarpetaRepository carpetaRepository;
    private final UsuarioRepository usuarioRepository;
    private final EtiquetaElementoRepository etiquetaElementoRepository;

    public CarpetaService(CarpetaRepository carpetaRepository,
                          UsuarioRepository usuarioRepository,
                          EtiquetaElementoRepository etiquetaElementoRepository) {
        this.carpetaRepository = carpetaRepository;
        this.usuarioRepository = usuarioRepository;
        this.etiquetaElementoRepository = etiquetaElementoRepository;
    }

    // CONSULTAS

    // Carpetas raíz del usuario (sin carpeta padre)
    public List<Carpeta> getCarpetasRaiz(Integer idUsuario) {
        return carpetaRepository.findByIdUsuarioIdAndIdCarpetaPadreIsNull(idUsuario);
    }

    // Subcarpetas de una carpeta concreta
    public List<Carpeta> getSubcarpetas(Integer idCarpetaPadre) {
        return carpetaRepository.findByIdCarpetaPadreId(idCarpetaPadre);
    }

    // Búsqueda de carpetas por nombre parcial
    public List<Carpeta> buscarCarpetas(Integer idUsuario, String nombre) {
        return carpetaRepository
                .findByIdUsuarioIdAndNombreContainingIgnoreCase(idUsuario, nombre);
    }

    // OPERACIONES

    // Crea una carpeta; lanza excepción si ya existe una con el mismo nombre en ese nivel
    public Carpeta crearCarpeta(Integer idUsuario, String nombre, Integer idCarpetaPadre) {
        Optional<Usuario> resultadoUsuario = usuarioRepository.findById(idUsuario);

        if (resultadoUsuario.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (carpetaRepository.existsByNombreAndIdUsuarioIdAndIdCarpetaPadreId(
                nombre, idUsuario, idCarpetaPadre)) {
            throw new RuntimeException("Ya existe una carpeta con ese nombre en este nivel");
        }

        Carpeta carpeta = new Carpeta();
        carpeta.setIdUsuario(resultadoUsuario.get());
        carpeta.setNombre(nombre);

        if (idCarpetaPadre != null) {
            Optional<Carpeta> resultadoPadre = carpetaRepository.findById(idCarpetaPadre);

            if (resultadoPadre.isEmpty()) {
                throw new RuntimeException("Carpeta padre no encontrada");
            }

            carpeta.setIdCarpetaPadre(resultadoPadre.get());
        }

        return carpetaRepository.save(carpeta);
    }

    // Renombra una carpeta existente
    public Carpeta renombrarCarpeta(Integer idCarpeta, String nuevoNombre) {
        Optional<Carpeta> resultado = carpetaRepository.findById(idCarpeta);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Carpeta no encontrada");
        }

        Carpeta carpeta = resultado.get();
        carpeta.setNombre(nuevoNombre);
        return carpetaRepository.save(carpeta);
    }

    // Elimina una carpeta y su contenido en cascada
    public void eliminarCarpeta(Integer idCarpeta) {
        Optional<Carpeta> resultado = carpetaRepository.findById(idCarpeta);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Carpeta no encontrada");
        }

        carpetaRepository.delete(resultado.get());
    }

    // MAPPER

    // Convierte una Carpeta a su DTO, incluyendo la etiqueta si existe
    public CarpetaDTO toDTO(Carpeta carpeta) {
        CarpetaDTO dto = new CarpetaDTO();
        dto.setId(carpeta.getId());
        dto.setNombre(carpeta.getNombre());
        dto.setFechaCreacion(carpeta.getFechaCreacion() != null
                ? carpeta.getFechaCreacion().atZone(ZoneId.systemDefault()).toLocalDateTime()
                : null);
        dto.setIdUsuario(carpeta.getIdUsuario().getId());
        dto.setIdCarpetaPadre(carpeta.getIdCarpetaPadre() != null
                ? carpeta.getIdCarpetaPadre().getId() : null);

        etiquetaElementoRepository.findByIdCarpetaId(carpeta.getId())
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