package com.ecommerce.users.repository;

import com.ecommerce.users.model.Usuario;

import java.util.List;
import java.util.Optional;

/**
 * Contrato que necesita UsuarioService para guardar y consultar usuarios.
 * Tiene dos implementaciones intercambiables por perfil de Spring:
 *  - jpa.UsuarioRepositorioJpaAdapter    (perfil por defecto / "bd"): usa PostgreSQL en srv-data
 *  - memoria.UsuarioRepositorioMemoriaAdapter (perfil "memoria"): guarda en RAM, sin BD
 *
 * El perfil activo se define en application.properties (spring.profiles.active).
 */
public interface UsuarioRepositorioPuerto {

    Usuario guardar(Usuario usuario);

    Optional<Usuario> buscarPorCorreo(String correo);

    boolean existePorCorreo(String correo);

    Optional<Usuario> buscarPorId(Long id);

    List<Usuario> listarTodos();
}
