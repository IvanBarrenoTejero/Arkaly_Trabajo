package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.dto.MetaAhorroDTO;
import com.arkaly.arkalybackend.models.MetasAhorro;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.MetaAhorroRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para la gestión de metas de ahorro del usuario.
 * Permite crear, actualizar y eliminar metas, marcándolas automáticamente
 * como completadas cuando se alcanza la cantidad objetivo.
 */
@Service
public class MetaAhorroService {

    private final MetaAhorroRepository metaAhorroRepository;
    private final UsuarioRepository usuarioRepository;

    public MetaAhorroService(MetaAhorroRepository metaAhorroRepository,
                             UsuarioRepository usuarioRepository) {
        this.metaAhorroRepository = metaAhorroRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Todas las metas del usuario, de más reciente a más antigua
    public List<MetaAhorroDTO> getMetasAhorro(Integer idUsuario) {
        return metaAhorroRepository.findByIdUsuarioIdOrderByFechaCreacionDesc(idUsuario)
                .stream().map(this::toMetaAhorroDTO).toList();
    }

    // Crea una nueva meta de ahorro
    public MetasAhorro crearMetaAhorro(Integer idUsuario, String nombre,
                                       BigDecimal cantidadObjetivo, String fechaLimite) {
        Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultado.get();

        MetasAhorro meta = new MetasAhorro();
        meta.setIdUsuario(usuario);
        meta.setNombre(nombre);
        meta.setCantidadObjetivo(cantidadObjetivo);
        meta.setCantidadActual(BigDecimal.ZERO);
        meta.setFechaLimite(fechaLimite != null && !fechaLimite.isEmpty()
                ? LocalDate.parse(fechaLimite) : null);
        meta.setCompletada(false);
        meta.setFechaCreacion(Instant.now());
        return metaAhorroRepository.save(meta);
    }

    // Actualiza la cantidad ahorrada; si llega al objetivo la marca como completada
    public void actualizarCantidadMeta(Integer id, BigDecimal nuevaCantidad) {
        Optional<MetasAhorro> resultado = metaAhorroRepository.findById(id);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Meta no encontrada");
        }

        MetasAhorro meta = resultado.get();
        meta.setCantidadActual(nuevaCantidad);
        if (nuevaCantidad.compareTo(meta.getCantidadObjetivo()) >= 0) {
            meta.setCompletada(true);
        }
        metaAhorroRepository.save(meta);
    }

    // Elimina una meta por su id
    public void eliminarMetaAhorro(Integer id) {
        metaAhorroRepository.deleteById(id);
    }

    // MAPPER

    private MetaAhorroDTO toMetaAhorroDTO(MetasAhorro m) {
        MetaAhorroDTO dto = new MetaAhorroDTO();
        dto.setId(m.getId());
        dto.setNombre(m.getNombre());
        dto.setCantidadObjetivo(m.getCantidadObjetivo());
        dto.setCantidadActual(m.getCantidadActual());
        dto.setFechaLimite(m.getFechaLimite());
        dto.setCompletada(m.getCompletada());
        dto.setFechaCreacion(m.getFechaCreacion() != null
                ? m.getFechaCreacion().atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                : null);
        return dto;
    }
}