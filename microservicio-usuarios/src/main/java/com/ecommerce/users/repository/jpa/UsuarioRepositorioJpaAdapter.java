package com.ecommerce.users.repository.jpa;

import com.ecommerce.users.model.Usuario;
import com.ecommerce.users.repository.UsuarioRepositorioPuerto;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Implementación real: guarda en PostgreSQL (srv-data) usando UsuarioJpaRepository.
 * Activa siempre que el perfil "memoria" NO esté activo (es decir, perfil por
 * defecto o perfil "bd").
 */
@Repository
@Profile("!memoria")
public class UsuarioRepositorioJpaAdapter implements UsuarioRepositorioPuerto {

    private final UsuarioJpaRepository jpaRepository;

    public UsuarioRepositorioJpaAdapter(UsuarioJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Usuario guardar(Usuario usuario) {
        return jpaRepository.save(usuario);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return jpaRepository.findByCorreo(correo);
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return jpaRepository.existsByCorreo(correo);
    }

    @Override
    public Optional<Usuario> buscarPorId(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<Usuario> listarTodos() {
        return jpaRepository.findAll();
    }
}
