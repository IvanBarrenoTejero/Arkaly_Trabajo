package com.arkaly.arkalybackend.controllers;

import com.arkaly.arkalybackend.dto.CategoriaTransaccionDTO;
import com.arkaly.arkalybackend.dto.TransaccionDTO;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.service.TransaccionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/transacciones")
public class TransaccionController {

    private final TransaccionService transaccionService;
    private final UsuarioRepository usuarioRepository;

    public TransaccionController(TransaccionService transaccionService,
                                 UsuarioRepository usuarioRepository) {
        this.transaccionService = transaccionService;
        this.usuarioRepository = usuarioRepository;
    }

    // TRANSACCIONES

    @GetMapping
    public ResponseEntity<List<TransaccionDTO>> getTransacciones(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(
                transaccionService.getTransacciones(getIdUsuario(auth), desde, hasta));
    }

    @PostMapping
    public ResponseEntity<TransaccionDTO> crearTransaccion(
            Authentication auth, @RequestBody TransaccionDTO dto) {
        return ResponseEntity.ok(transaccionService.crearTransaccion(getIdUsuario(auth), dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarTransaccion(@PathVariable Integer id) {
        transaccionService.eliminarTransaccion(id);
        return ResponseEntity.noContent().build();
    }

    // CATEGORÍAS

    @GetMapping("/categorias")
    public ResponseEntity<List<CategoriaTransaccionDTO>> getCategorias(Authentication auth) {
        return ResponseEntity.ok(transaccionService.getCategorias(getIdUsuario(auth)));
    }

    @PostMapping("/categorias")
    public ResponseEntity<CategoriaTransaccionDTO> crearCategoria(
            Authentication auth, @RequestBody CategoriaTransaccionDTO dto) {
        return ResponseEntity.ok(transaccionService.crearCategoria(getIdUsuario(auth), dto));
    }

    @DeleteMapping("/categorias/{id}")
    public ResponseEntity<Void> eliminarCategoria(@PathVariable Integer id) {
        transaccionService.eliminarCategoria(id);
        return ResponseEntity.noContent().build();
    }

    private Integer getIdUsuario(Authentication auth) {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(auth.getName());

        if (resultado.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        return resultado.get().getId();
    }
}