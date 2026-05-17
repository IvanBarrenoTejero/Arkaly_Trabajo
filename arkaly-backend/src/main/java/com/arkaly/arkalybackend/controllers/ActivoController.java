package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.ActivoFavoritoDTO;
import com.arkaly.arkalybackend.dto.ActivoMercadoDTO;
import com.arkaly.arkalybackend.models.ActivosFavorito;
import com.arkaly.arkalybackend.models.ActivosMercado;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.ActivoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/activos")
public class ActivoController {

    private final ActivoService activoService;
    private final UsuarioRepository usuarioRepository;

    public ActivoController(ActivoService activoService,
                            UsuarioRepository usuarioRepository) {
        this.activoService = activoService;
        this.usuarioRepository = usuarioRepository;
    }

    // MERCADO
    @GetMapping("/mercado")
    public ResponseEntity<List<ActivoMercadoDTO>> getTodosActivos() {
        return ResponseEntity.ok(activoService.getTodosActivos());
    }

    @GetMapping("/mercado/{tipo}")
    public ResponseEntity<List<ActivoMercadoDTO>> getActivosPorTipo(@PathVariable String tipo) {
        return ResponseEntity.ok(activoService.getActivosPorTipo(
                TipoActivo.valueOf(tipo.toUpperCase())));
    }

    @PostMapping("/mercado")
    public ResponseEntity<?> agregarActivoMercado(@RequestBody Map<String, String> request) {
        ActivosMercado activo = activoService.agregarActivoMercado(
                request.get("simbolo"),
                request.get("nombre"),
                TipoActivo.valueOf(request.get("tipoActivo").toUpperCase()));
        return ResponseEntity.ok(Map.of("mensaje", "Activo añadido", "id", activo.getId()));
    }

    @GetMapping("/buscar/{query}")
    public ResponseEntity<?> buscarActivos(@PathVariable String query) {
        return ResponseEntity.ok(activoService.buscarActivos(query));
    }

    // FAVORITOS
    @GetMapping("/favoritos")
    public ResponseEntity<List<ActivoFavoritoDTO>> getFavoritos(Authentication auth) {
        return ResponseEntity.ok(activoService.getFavoritos(getIdUsuario(auth)));
    }

    @PostMapping("/favoritos")
    public ResponseEntity<?> agregarFavorito(@RequestBody Map<String, String> request,
                                             Authentication auth) {
        ActivosFavorito fav = activoService.agregarFavorito(
                getIdUsuario(auth),
                request.get("simbolo"),
                request.get("nombre"),
                TipoActivo.valueOf(request.get("tipoActivo").toUpperCase()));
        return ResponseEntity.ok(Map.of("mensaje", "Activo añadido a favoritos", "id", fav.getId()));
    }

    @DeleteMapping("/favoritos/{id}")
    public ResponseEntity<?> eliminarFavorito(@PathVariable Integer id) {
        activoService.eliminarFavorito(id);
        return ResponseEntity.ok("Activo eliminado de favoritos");
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}
