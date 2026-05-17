package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.AlertaPrecioDTO;
import com.arkaly.arkalybackend.models.AlertasPrecio;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.AlertaPrecioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/alertas")
public class AlertaController {

    private final AlertaPrecioService alertaPrecioService;
    private final UsuarioRepository usuarioRepository;

    public AlertaController(AlertaPrecioService alertaPrecioService,
                            UsuarioRepository usuarioRepository) {
        this.alertaPrecioService = alertaPrecioService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<AlertaPrecioDTO>> getAlertas(Authentication auth) {
        return ResponseEntity.ok(alertaPrecioService.getAlertasPrecio(getIdUsuario(auth)));
    }

    @GetMapping("/con-precio")
    public ResponseEntity<List<AlertaPrecioDTO>> getAlertasConPrecio(Authentication auth) {
        return ResponseEntity.ok(alertaPrecioService.getAlertasConPrecioActual(getIdUsuario(auth)));
    }

    @GetMapping("/disparadas")
    public ResponseEntity<List<AlertaPrecioDTO>> getAlertasDisparadas(Authentication auth) {
        return ResponseEntity.ok(alertaPrecioService.getAlertasDisparadas(getIdUsuario(auth)));
    }

    @PostMapping
    public ResponseEntity<?> crearAlerta(@RequestBody Map<String, String> request,
                                         Authentication auth) {
        try {
            AlertasPrecio alerta = alertaPrecioService.crearAlertaPrecio(
                    getIdUsuario(auth),
                    request.get("simbolo"),
                    TipoActivo.valueOf(request.get("tipoActivo").toUpperCase()),
                    request.get("condicion"),
                    new BigDecimal(request.get("precioObjetivo")));
            return ResponseEntity.ok(Map.of("mensaje", "Alerta creada", "id", alerta.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAlerta(@PathVariable Integer id) {
        alertaPrecioService.toggleAlertaPrecio(id);
        return ResponseEntity.ok(Map.of("mensaje", "Alerta actualizada"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarAlerta(@PathVariable Integer id) {
        alertaPrecioService.eliminarAlertaPrecio(id);
        return ResponseEntity.ok(Map.of("mensaje", "Alerta eliminada"));
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}
