package com.ecommerce.users.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.ecommerce.users.model.Usuario;
import com.ecommerce.users.repository.UsuarioRepositorioPuerto;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Lee el header "Authorization: Bearer <token>", valida el JWT con JwtUtil y,
 * si es válido, deja al usuario autenticado en el SecurityContext para el
 * resto de la petición (con su correo como "username" y su rol como
 * autoridad, ej. ROLE_ADMIN). Si no hay token, o es inválido/expiró, no
 * autentica a nadie y deja que SecurityConfig decida si la ruta requiere
 * login (rutas permitAll igual funcionan sin token).
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    // ObjectProvider (resolucion perezosa) para evitar una dependencia circular:
    // SecurityConfig -> este filtro -> repositorio -> PasswordEncoder -> SecurityConfig.
    private final ObjectProvider<UsuarioRepositorioPuerto> usuarioRepositorio;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectProvider<UsuarioRepositorioPuerto> usuarioRepositorio) {
        this.jwtUtil = jwtUtil;
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring("Bearer ".length());
            try {
                Claims claims = jwtUtil.validarYObtenerClaims(token);
                String correo = claims.getSubject();
                String rol = claims.get("rol", String.class);

                // Issue #10: si la cuenta fue desactivada (o ya no existe) despues de
                // emitir el token, el token deja de valer aunque no haya expirado.
                boolean cuentaActiva = usuarioRepositorio.getObject()
                        .buscarPorCorreo(correo)
                        .map(Usuario::isActivo)
                        .orElse(false);

                if (cuentaActiva) {
                    List<GrantedAuthority> autoridades = List.of(new SimpleGrantedAuthority("ROLE_" + rol));
                    var authentication = new UsernamePasswordAuthenticationToken(correo, null, autoridades);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                }
            } catch (JwtException e) {
                // Token invalido o expirado: no se autentica. Si la ruta requiere
                // login, SecurityConfig respondera 401 mas adelante en la cadena.
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }
}
