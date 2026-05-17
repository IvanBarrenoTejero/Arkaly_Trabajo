package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.CarpetaDTO;
import com.arkaly.arkalybackend.models.Carpeta;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.CarpetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/carpetas")
public class CarpetaController {

    private final CarpetaService carpetaService;
    private final UsuarioRepository usuarioRepository;

    public CarpetaController(CarpetaService carpetaService,
                             UsuarioRepository usuarioRepository) {
        this.carpetaService = carpetaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/raiz")
    public ResponseEntity<List<CarpetaDTO>> getCarpetasRaiz(Authentication auth) {
        return ResponseEntity.ok(carpetaService.getCarpetasRaiz(getIdUsuario(auth))
                .stream().map(carpetaService::toDTO).toList());
    }

    @GetMapping("/{id}/subcarpetas")
    public ResponseEntity<List<CarpetaDTO>> getSubcarpetas(@PathVariable Integer id) {
        return ResponseEntity.ok(carpetaService.getSubcarpetas(id)
                .stream().map(carpetaService::toDTO).toList());
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<CarpetaDTO>> buscarCarpetas(@RequestParam String nombre,
                                                           Authentication auth) {
        return ResponseEntity.ok(carpetaService.buscarCarpetas(getIdUsuario(auth), nombre)
                .stream().map(carpetaService::toDTO).toList());
    }

    @PostMapping
    public ResponseEntity<CarpetaDTO> crearCarpeta(@RequestBody Map<String, String> request,
                                                   Authentication auth) {
        Integer idPadre = request.get("idCarpetaPadre") != null
                ? Integer.parseInt(request.get("idCarpetaPadre")) : null;
        Carpeta carpeta = carpetaService.crearCarpeta(
                getIdUsuario(auth), request.get("nombre"), idPadre);
        return ResponseEntity.ok(carpetaService.toDTO(carpeta));
    }

    @PutMapping("/{id}/renombrar")
    public ResponseEntity<CarpetaDTO> renombrarCarpeta(@PathVariable Integer id,
                                                       @RequestBody Map<String, String> request) {
        return ResponseEntity.ok(carpetaService.toDTO(
                carpetaService.renombrarCarpeta(id, request.get("nombre"))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarCarpeta(@PathVariable Integer id) {
        carpetaService.eliminarCarpeta(id);
        return ResponseEntity.ok("Carpeta eliminada correctamente");
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}