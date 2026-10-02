package com.ecommerce.users.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Matcher a mano (en vez de requestMatchers(HttpMethod.POST, "/api/usuarios"))
        // porque esa variante con HttpMethod dio problemas para que reconociera bien
        // la ruta en este proyecto; esta forma con lambda es mas explicita y funciona
        // igual de bien: compara metodo HTTP y path directamente sobre el request.
        RequestMatcher registroUsuario =
                request -> "POST".equalsIgnoreCase(request.getMethod())
                        && "/api/usuarios".equals(request.getRequestURI());

        // GET /api/usuarios (listar TODOS los usuarios) es informacion sensible:
        // solo un ADMIN puede verla. GET/PUT sobre un usuario puntual
        // (/api/usuarios/{id}) los puede hacer cualquier usuario autenticado,
        // pero el propio UsuarioService revisa que sea su propia cuenta o que
        // quien pregunta sea ADMIN (ver verificarPropioOAdmin).
        RequestMatcher listarUsuarios =
                request -> "GET".equalsIgnoreCase(request.getMethod())
                        && "/api/usuarios".equals(request.getRequestURI());

        // PATCH /api/usuarios/{id}/activar y /desactivar (Issue #10): solo ADMIN.
        RequestMatcher cambiarEstadoCuenta =
                request -> "PATCH".equalsIgnoreCase(request.getMethod())
                        && request.getRequestURI().matches("^/api/usuarios/[^/]+/(activar|desactivar)$");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Publicos: login y registro (crear cuenta)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(registroUsuario).permitAll()
                        // Listar todos los usuarios: solo ADMIN
                        .requestMatchers(listarUsuarios).hasRole("ADMIN")
                        // Activar / desactivar cuentas: solo ADMIN (Issue #10)
                        .requestMatchers(cambiarEstadoCuenta).hasRole("ADMIN")
                        // Todo lo demas en /api/usuarios (consultar/actualizar un
                        // usuario puntual, activar/desactivar) requiere un JWT
                        // valido -> Issue #9. El dueño de la cuenta o el ADMIN se
                        // valida dentro del servicio.
                        .anyRequest().authenticated()
                )
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                // Antes esto devolvia 401/403 con el body vacio, dificil de depurar.
                // Ahora devuelve un JSON explicando que paso.
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"mensaje\":\"No autenticado. Este endpoint requiere iniciar sesión (envía el token en el header Authorization: Bearer <token>).\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json");
                            response.getWriter().write(
                                    "{\"mensaje\":\"No tienes permiso para acceder a este recurso.\"}");
                        })
                )
                // Nuestro filtro lee el header Authorization y, si el token es
                // valido, autentica al usuario ANTES de que Spring intente el
                // login por usuario/contraseña (que aqui no usamos).
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}


