package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.*;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.InformeService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/informes")
public class InformeController {

    private final InformeService informeService;
    private final UsuarioRepository usuarioRepository;

    public InformeController(InformeService informeService,
                             UsuarioRepository usuarioRepository) {
        this.informeService = informeService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/gastos-por-categoria")
    public ResponseEntity<List<GastoCategoriaDTO>> gastosPorCategoria(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(
                informeService.getGastosPorCategoria(getIdUsuario(auth), desde, hasta));
    }

    @GetMapping("/ingresos-vs-gastos")
    public ResponseEntity<List<IngresoGastoMensualDTO>> ingresosVsGastos(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(
                informeService.getIngresosVsGastos(getIdUsuario(auth), desde, hasta));
    }

    @GetMapping("/proyeccion")
    public ResponseEntity<ProyeccionDTO> proyeccion(Authentication auth) {
        return ResponseEntity.ok(informeService.getProyeccion(getIdUsuario(auth)));
    }

    @GetMapping("/resumen-patrimonial")
    public ResponseEntity<ResumenPatrimonialDTO> resumenPatrimonial(Authentication auth) {
        return ResponseEntity.ok(informeService.getResumenPatrimonial(getIdUsuario(auth)));
    }

    @PutMapping("/perfil/regla")
    public ResponseEntity<?> actualizarRegla(Authentication auth,
                                             @RequestBody Map<String, String> body) {
        informeService.actualizarReglaFinanciera(getIdUsuario(auth), body.get("regla"));
        return ResponseEntity.ok(Map.of("mensaje", "Regla actualizada"));
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}