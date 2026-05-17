package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.EtiquetaDTO;
import com.arkaly.arkalybackend.models.Etiqueta;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.EtiquetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/etiquetas")
public class EtiquetaController {

    private final EtiquetaService etiquetaService;
    private final UsuarioRepository usuarioRepository;

    public EtiquetaController(EtiquetaService etiquetaService,
                              UsuarioRepository usuarioRepository) {
        this.etiquetaService = etiquetaService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping
    public ResponseEntity<List<EtiquetaDTO>> listarEtiquetas(Authentication auth) {
        return ResponseEntity.ok(etiquetaService.getEtiquetasUsuario(getIdUsuario(auth))
                .stream().map(etiquetaService::toDTO).toList());
    }

    @PostMapping
    public ResponseEntity<EtiquetaDTO> crearEtiqueta(@RequestBody Map<String, String> request,
                                                     Authentication auth) {
        Etiqueta etiqueta = etiquetaService.crearEtiqueta(
                getIdUsuario(auth), request.get("nombre"), request.get("color"));
        return ResponseEntity.ok(etiquetaService.toDTO(etiqueta));
    }

    @PostMapping("/asignar-carpeta/{idCarpeta}")
    public ResponseEntity<?> asignarACarpeta(@PathVariable Integer idCarpeta,
                                             @RequestBody Map<String, Integer> request) {
        etiquetaService.asignarEtiquetaCarpeta(request.get("idEtiqueta"), idCarpeta);
        return ResponseEntity.ok("Etiqueta asignada a carpeta");
    }

    @PostMapping("/asignar-documento/{idDocumento}")
    public ResponseEntity<?> asignarADocumento(@PathVariable Integer idDocumento,
                                               @RequestBody Map<String, Integer> request) {
        etiquetaService.asignarEtiquetaDocumento(request.get("idEtiqueta"), idDocumento);
        return ResponseEntity.ok("Etiqueta asignada a documento");
    }

    @DeleteMapping("/quitar-carpeta/{idCarpeta}")
    public ResponseEntity<?> quitarDeCarpeta(@PathVariable Integer idCarpeta) {
        etiquetaService.quitarEtiquetaCarpeta(idCarpeta);
        return ResponseEntity.ok("Etiqueta quitada de carpeta");
    }

    @DeleteMapping("/quitar-documento/{idDocumento}")
    public ResponseEntity<?> quitarDeDocumento(@PathVariable Integer idDocumento) {
        etiquetaService.quitarEtiquetaDocumento(idDocumento);
        return ResponseEntity.ok("Etiqueta quitada de documento");
    }

    @DeleteMapping("/eliminar/{id}")
    public ResponseEntity<?> eliminarEtiqueta(@PathVariable Integer id) {
        etiquetaService.eliminarEtiqueta(id);
        return ResponseEntity.ok("Etiqueta eliminada");
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}