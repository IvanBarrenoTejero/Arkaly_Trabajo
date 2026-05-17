package com.arkaly.arkalybackend.security;

import com.arkaly.arkalybackend.models.Usuario;
import com.arkaly.arkalybackend.repositories.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Implementación de UserDetailsService que conecta Spring Security
 * con nuestra base de datos de usuarios.
 *
 * Spring Security llama a loadUserByUsername() automáticamente
 * cuando necesita verificar las credenciales de un usuario.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UserDetailsServiceImpl(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<Usuario> resultado = usuarioRepository.findByEmail(email);

        if (resultado.isEmpty()) {
            throw new UsernameNotFoundException("Usuario no encontrado: " + email);
        }

        Usuario usuario = resultado.get();
        String rol = "ROLE_" + usuario.getRol().name();

        return new User(
                usuario.getEmail(),
                usuario.getPasswordHash(),
                List.of(new SimpleGrantedAuthority(rol))
        );
    }
}