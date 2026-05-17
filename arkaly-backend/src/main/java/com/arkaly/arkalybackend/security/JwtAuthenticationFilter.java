package com.arkaly.arkalybackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que revisa el token JWT en cada petición HTTP.
 * Si el token es válido, autentica al usuario en Spring Security.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay token, dejamos pasar la petición sin autenticar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7); // Quitamos el prefijo "Bearer "

        if (jwtUtils.validateToken(token)) {
            autenticarUsuario(request, token);
        }

        filterChain.doFilter(request, response); // Pasamos al siguiente filtro
    }

    private void autenticarUsuario(HttpServletRequest request, String token) {
        String email = jwtUtils.getEmailFromToken(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Creamos la autenticación con los roles del usuario
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        // Añadimos info extra de la petición (IP, sesión...)
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        // Registramos al usuario como autenticado en Spring Security
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}