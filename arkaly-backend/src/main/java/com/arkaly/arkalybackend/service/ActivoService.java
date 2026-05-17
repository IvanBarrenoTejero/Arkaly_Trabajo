package com.arkaly.arkalybackend.service;

import com.arkaly.arkalybackend.clients.YFinanceClient;
import com.arkaly.arkalybackend.dto.ActivoFavoritoDTO;
import com.arkaly.arkalybackend.dto.ActivoMercadoDTO;
import com.arkaly.arkalybackend.models.ActivosFavorito;
import com.arkaly.arkalybackend.models.ActivosMercado;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.repositories.ActivoFavoritoRepository;
import com.arkaly.arkalybackend.repositories.ActivoMercadoRepository;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio para la gestión de activos financieros:
 * - Activos disponibles en el mercado (catálogo)
 * - Activos marcados como favoritos por el usuario
 * - Búsqueda de activos via Yahoo Finance
 */
@Service
public class ActivoService {

    private final ActivoFavoritoRepository activoFavoritoRepository;
    private final ActivoMercadoRepository activoMercadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final YFinanceClient yFinanceClient;

    public ActivoService(ActivoFavoritoRepository activoFavoritoRepository,
                         ActivoMercadoRepository activoMercadoRepository,
                         UsuarioRepository usuarioRepository,
                         YFinanceClient yFinanceClient) {
        this.activoFavoritoRepository = activoFavoritoRepository;
        this.activoMercadoRepository = activoMercadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.yFinanceClient = yFinanceClient;
    }

    // CATÁLOGO DE MERCADO

    // Activos filtrados por tipo
    public List<ActivoMercadoDTO> getActivosPorTipo(TipoActivo tipo) {
        return activoMercadoRepository.findByTipoActivo(tipo).stream()
                .map(this::toActivoMercadoDTO).toList();
    }

    // Todos los activos del catálogo
    public List<ActivoMercadoDTO> getTodosActivos() {
        return activoMercadoRepository.findAll().stream()
                .map(this::toActivoMercadoDTO).toList();
    }

    // Añade un activo al catálogo
    public ActivosMercado agregarActivoMercado(String simbolo, String nombre, TipoActivo tipo) {
        ActivosMercado activo = new ActivosMercado();
        activo.setSimbolo(simbolo.toUpperCase());
        activo.setNombre(nombre);
        activo.setTipoActivo(tipo);
        activo.setEsDefault(false);
        return activoMercadoRepository.save(activo);
    }

    // Busca activos en Yahoo Finance
    public List<Map<String, String>> buscarActivos(String query) {
        return yFinanceClient.buscarActivos(query);
    }

    // FAVORITOS

    // Favoritos del usuario
    public List<ActivoFavoritoDTO> getFavoritos(Integer idUsuario) {
        return activoFavoritoRepository.findByIdUsuarioId(idUsuario).stream()
                .map(this::toFavoritoDTO).toList();
    }

    // Añade un activo a favoritos
    public ActivosFavorito agregarFavorito(Integer idUsuario, String simbolo,
                                           String nombre, TipoActivo tipoActivo) {
        Optional<Usuario> resultado = usuarioRepository.findById(idUsuario);

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = resultado.get();

        ActivosFavorito fav = new ActivosFavorito();
        fav.setIdUsuario(usuario);
        fav.setSimbolo(simbolo);
        fav.setNombre(nombre);
        fav.setTipoActivo(tipoActivo);
        fav.setFechaAgregado(Instant.now());
        return activoFavoritoRepository.save(fav);
    }

    // Elimina un favorito por su id
    public void eliminarFavorito(Integer id) {
        activoFavoritoRepository.deleteById(id);
    }

    // MAPPERS

    private ActivoFavoritoDTO toFavoritoDTO(ActivosFavorito f) {
        ActivoFavoritoDTO dto = new ActivoFavoritoDTO();
        dto.setId(f.getId());
        dto.setSimbolo(f.getSimbolo());
        dto.setNombre(f.getNombre());
        dto.setTipoActivo(f.getTipoActivo().name());
        return dto;
    }

    private ActivoMercadoDTO toActivoMercadoDTO(ActivosMercado a) {
        ActivoMercadoDTO dto = new ActivoMercadoDTO();
        dto.setId(a.getId());
        dto.setSimbolo(a.getSimbolo());
        dto.setNombre(a.getNombre());
        dto.setTipoActivo(a.getTipoActivo().name());
        return dto;
    }
}