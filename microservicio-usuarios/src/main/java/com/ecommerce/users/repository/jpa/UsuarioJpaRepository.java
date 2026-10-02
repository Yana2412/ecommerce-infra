package com.ecommerce.users.repository.jpa;

import com.ecommerce.users.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio Spring Data JPA "real", contra PostgreSQL en srv-data.
 * Solo se usa cuando el perfil activo NO es "memoria" (ver UsuarioRepositorioJpaAdapter).
 */
public interface UsuarioJpaRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
