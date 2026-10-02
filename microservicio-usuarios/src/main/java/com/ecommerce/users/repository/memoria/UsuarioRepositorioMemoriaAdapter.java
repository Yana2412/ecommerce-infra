package com.ecommerce.users.repository.memoria;

import com.ecommerce.users.model.Rol;
import com.ecommerce.users.model.Usuario;
import com.ecommerce.users.repository.UsuarioRepositorioPuerto;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementación TEMPORAL en memoria del repositorio de usuarios.
 *
 * Se usa mientras no hay conexión de red a la VM srv-data (PostgreSQL), para
 * poder probar /api/usuarios y /api/auth/login igual. Los datos viven solo en
 * RAM: se pierden cada vez que se reinicia la aplicación, es exclusivamente
 * para desarrollo/pruebas locales, NUNCA para la VM final.
 *
 * Se activa con el perfil "memoria" (spring.profiles.active=memoria en
 * application.properties). Cuando ya tengan acceso a srv-data, cambien ese
 * valor a "bd" y esta clase deja de usarse automáticamente (queda inactiva,
 * no hace falta borrarla ni comentarla).
 */
@Repository
@Profile("memoria")
public class UsuarioRepositorioMemoriaAdapter implements UsuarioRepositorioPuerto {

    private final Map<Long, Usuario> usuariosPorId = new ConcurrentHashMap<>();
    private final AtomicLong contadorId = new AtomicLong(0);

    /**
     * Cuenta ADMIN de prueba, creada solo aquí (perfil "memoria"), para poder
     * probar GET /api/usuarios (que requiere rol ADMIN) desde IntelliJ sin
     * depender todavía de la base de datos real ni de un mecanismo de
     * ascenso de rol (que no pide este issue).
     *   correo:   admin@ecommerce.com
     *   password: admin123
     */
    public UsuarioRepositorioMemoriaAdapter(PasswordEncoder passwordEncoder) {
        Usuario admin = new Usuario("Administrador", "admin@ecommerce.com",
                passwordEncoder.encode("admin123"));
        admin.setRol(Rol.ADMIN);
        guardar(admin);
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        if (usuario.getId() == null) {
            usuario.setId(contadorId.incrementAndGet());
            if (usuario.getFechaCreacion() == null) {
                usuario.setFechaCreacion(LocalDateTime.now());
            }
        }
        usuariosPorId.put(usuario.getId(), usuario);
        return usuario;
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuariosPorId.values().stream()
                .filter(usuario -> usuario.getCorreo().equalsIgnoreCase(correo))
                .findFirst();
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return buscarPorCorreo(correo).isPresent();
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return Optional.ofNullable(usuariosPorId.get(id));
    }

    @Override
    public List<Usuario> listarTodos() {
        return new ArrayList<>(usuariosPorId.values());
    }
}
