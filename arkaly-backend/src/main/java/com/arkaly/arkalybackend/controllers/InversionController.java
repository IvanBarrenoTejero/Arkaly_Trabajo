package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.*;
import com.arkaly.arkalybackend.models.*;
import com.arkaly.arkalybackend.models.enums.TipoActivo;
import com.arkaly.arkalybackend.models.enums.TipoOperacion;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.InversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/inversiones")
public class InversionController {

    private final InversionService inversionService;
    private final UsuarioRepository usuarioRepository;

    public InversionController(InversionService inversionService,
                               UsuarioRepository usuarioRepository) {
        this.inversionService = inversionService;
        this.usuarioRepository = usuarioRepository;
    }

    // RESUMEN
    @GetMapping("/resumen")
    public ResponseEntity<ResumenInversionDTO> getResumen(Authentication auth) {
        return ResponseEntity.ok(inversionService.getResumen(getIdUsuario(auth)));
    }

    // HISTÓRICO
    @GetMapping("/historico/{simbolo}")
    public ResponseEntity<?> getHistorico(
            @PathVariable String simbolo,
            @RequestParam(defaultValue = "ACCION") String tipo,
            @RequestParam(defaultValue = "1mo") String period,
            @RequestParam(defaultValue = "1d") String interval) {
        List<PuntoHistoricoDTO> puntos = inversionService.getHistoricoActivo(simbolo, tipo, period, interval);
        return ResponseEntity.ok(Map.of("simbolo", simbolo, "puntos", puntos));
    }

    // POSICIONES
    @GetMapping("/posiciones")
    public ResponseEntity<List<PosicionDTO>> getPosiciones(Authentication auth) {
        return ResponseEntity.ok(inversionService.getPosicionesConValorActual(getIdUsuario(auth)));
    }

    @GetMapping("/posiciones/{tipo}")
    public ResponseEntity<List<PosicionDTO>> getPosicionesPorTipo(
            @PathVariable String tipo, Authentication auth) {
        return ResponseEntity.ok(inversionService.getPosicionesConValorActualPorTipo(
                getIdUsuario(auth), TipoActivo.valueOf(tipo.toUpperCase())));
    }

    // OPERACIONES
    @GetMapping("/operaciones")
    public ResponseEntity<List<CarteraDTO>> getOperaciones(Authentication auth) {
        return ResponseEntity.ok(inversionService.getOperaciones(getIdUsuario(auth)));
    }

    @GetMapping("/operaciones/{tipo}")
    public ResponseEntity<List<CarteraDTO>> getOperacionesPorTipo(
            @PathVariable String tipo, Authentication auth) {
        return ResponseEntity.ok(inversionService.getOperacionesPorTipo(
                getIdUsuario(auth), TipoActivo.valueOf(tipo.toUpperCase())));
    }

    @PostMapping("/operacion")
    public ResponseEntity<?> registrarOperacion(@RequestBody Map<String, String> request,
                                                Authentication auth) {
        try {
            Cartera op = inversionService.registrarOperacion(
                    getIdUsuario(auth),
                    request.get("simbolo"),
                    TipoActivo.valueOf(request.get("tipoActivo").toUpperCase()),
                    TipoOperacion.valueOf(request.get("tipoOperacion").toUpperCase()),
                    new BigDecimal(request.get("cantidad")),
                    new BigDecimal(request.get("precioUnitario")),
                    request.get("notas"));
            return ResponseEntity.ok(Map.of("mensaje", "Operación registrada", "id", op.getId()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}