package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.MetaAhorroDTO;
import com.arkaly.arkalybackend.models.MetasAhorro;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.MetaAhorroService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/metas")
public class MetaController {

    private final MetaAhorroService metaAhorroService;
    private final UsuarioRepository usuarioRepository;

    public MetaController(MetaAhorroService metaAhorroService,
                          UsuarioRepository usuarioRepository) {
        this.metaAhorroService = metaAhorroService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<MetaAhorroDTO>> getMetas(Authentication auth) {
        return ResponseEntity.ok(metaAhorroService.getMetasAhorro(getIdUsuario(auth)));
    }

    @PostMapping
    public ResponseEntity<?> crearMeta(@RequestBody Map<String, String> request,
                                       Authentication auth) {
        try {
            MetasAhorro meta = metaAhorroService.crearMetaAhorro(
                    getIdUsuario(auth),
                    request.get("nombre"),
                    new BigDecimal(request.get("cantidadObjetivo")),
                    request.get("fechaLimite"));
            return ResponseEntity.ok(Map.of("mensaje", "Meta creada", "id", meta.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMeta(@PathVariable Integer id,
                                            @RequestBody Map<String, String> request) {
        try {
            metaAhorroService.actualizarCantidadMeta(id, new BigDecimal(request.get("cantidadActual")));
            return ResponseEntity.ok(Map.of("mensaje", "Meta actualizada"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMeta(@PathVariable Integer id) {
        metaAhorroService.eliminarMetaAhorro(id);
        return ResponseEntity.ok(Map.of("mensaje", "Meta eliminada"));
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}