package com.arkaly.arkalybackend.controllers;
import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.models.enums.Rol;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import com.arkaly.arkalybackend.security.JwtUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController // Peticiones http y las respuestas son datos no vistas(HTML)
@RequestMapping("/api/auth") // Todos los endpoints de esta clase serán para autentificación
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils,
                          UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // --- RUTA 1: REGISTRO (Para guardar la contraseña encriptada) ---
    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@RequestBody Map<String, String> request) {

        if (usuarioRepository.existsByEmail(request.get("email"))) {
            return ResponseEntity.badRequest().body("El email ya está en uso");
        }

        if (usuarioRepository.existsByUsername(request.get("username"))) {
            return ResponseEntity.badRequest().body("El username ya está en uso");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(request.get("nombre"));
        nuevoUsuario.setEmail(request.get("email"));
        nuevoUsuario.setUsername(request.get("username"));
        nuevoUsuario.setPasswordHash(passwordEncoder.encode(request.get("password")));
        nuevoUsuario.setActivo(true);

        if (usuarioRepository.count() == 0) {
            nuevoUsuario.setRol(Rol.ADMIN);
        } else {
            nuevoUsuario.setRol(Rol.USUARIO);
        }

        usuarioRepository.save(nuevoUsuario);
        return ResponseEntity.ok("Usuario registrado con éxito");
    }

    // --- RUTA 2: LOGIN (Para obtener el Token) ---
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        // 1. Spring Security comprueba si el email y la contraseña coinciden con la BD
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.get("email"), request.get("password"))
        );

        // 2. Si es correcto, generamos el Token
        String token = jwtUtils.generateToken(request.get("email"));

        // 3. Se lo enviamos al usuario en formato JSON
        Usuario usuario = usuarioRepository.findByEmail(request.get("email"))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", usuario.getId(),
                "nombre", usuario.getNombre()
        ));
    }

    @PutMapping("/cambiar-rol")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> cambiarRol(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String nuevoRol = request.get("rol");

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setRol(Rol.valueOf(nuevoRol));
        usuarioRepository.save(usuario);

        return ResponseEntity.ok("Rol actualizado con exito");
    }
}