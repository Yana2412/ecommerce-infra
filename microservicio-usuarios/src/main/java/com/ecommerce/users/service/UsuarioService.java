package com.ecommerce.users.service;

import com.ecommerce.users.dto.ActualizarUsuarioRequest;
import com.ecommerce.users.dto.LoginRequest;
import com.ecommerce.users.dto.LoginResponse;
import com.ecommerce.users.dto.RegistroRequest;
import com.ecommerce.users.dto.UsuarioResponse;
import com.ecommerce.users.exception.AccesoNoAutorizadoException;
import com.ecommerce.users.exception.CorreoYaRegistradoException;
import com.ecommerce.users.exception.CredencialesInvalidasException;
import com.ecommerce.users.exception.CuentaDesactivadaException;
import com.ecommerce.users.exception.OperacionNoPermitidaException;
import com.ecommerce.users.exception.UsuarioNoEncontradoException;
import com.ecommerce.users.model.Usuario;
import com.ecommerce.users.repository.UsuarioRepositorioPuerto;
import com.ecommerce.users.security.JwtUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepositorioPuerto usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UsuarioService(UsuarioRepositorioPuerto usuarioRepositorio, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UsuarioResponse registrar(RegistroRequest request) {
        if (usuarioRepositorio.existePorCorreo(request.getCorreo())) {
            throw new CorreoYaRegistradoException(request.getCorreo());
        }

        Usuario usuario = new Usuario(
                request.getNombre(),
                request.getCorreo(),
                passwordEncoder.encode(request.getPassword())
        );

        Usuario guardado = usuarioRepositorio.guardar(usuario);

        return aRespuesta(guardado);
    }

    public LoginResponse iniciarSesion(LoginRequest request) {
        Usuario usuario = usuarioRepositorio.buscarPorCorreo(request.getCorreo())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new CredencialesInvalidasException();
        }

        // Regla del microservicio: solo una cuenta activa puede autenticarse.
        if (!usuario.isActivo()) {
            throw new CuentaDesactivadaException();
        }

        usuario.setUltimoAcceso(LocalDateTime.now());
        usuarioRepositorio.guardar(usuario);

        String token = jwtUtil.generarToken(usuario.getCorreo(), usuario.getRol().name());

        return new LoginResponse(token, usuario.getCorreo(), usuario.getRol().name());
    }

    // ---- Issue #9: consulta y actualización ----

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepositorio.listarTodos().stream()
                .map(this::aRespuesta)
                .toList();
    }

    public UsuarioResponse obtenerPorId(Long id, Authentication autenticacion) {
        Usuario usuario = usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        verificarPropioOAdmin(usuario, autenticacion);
        return aRespuesta(usuario);
    }

    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest request, Authentication autenticacion) {
        Usuario usuario = usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        verificarPropioOAdmin(usuario, autenticacion);

        // Si cambia el correo, verificar que no choque con el de otro usuario.
        if (!usuario.getCorreo().equalsIgnoreCase(request.getCorreo())) {
            boolean correoEnUso = usuarioRepositorio.buscarPorCorreo(request.getCorreo())
                    .filter(otro -> !otro.getId().equals(id))
                    .isPresent();
            if (correoEnUso) {
                throw new CorreoYaRegistradoException(request.getCorreo());
            }
        }

        usuario.setNombre(request.getNombre());
        usuario.setCorreo(request.getCorreo());

        Usuario actualizado = usuarioRepositorio.guardar(usuario);
        return aRespuesta(actualizado);
    }

    // ---- Issue #10: activar / desactivar cuenta (solo ADMIN, ver SecurityConfig) ----

    public UsuarioResponse activar(Long id) {
        Usuario usuario = usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));
        usuario.setActivo(true);
        return aRespuesta(usuarioRepositorio.guardar(usuario));
    }

    public UsuarioResponse desactivar(Long id, Authentication autenticacion) {
        Usuario usuario = usuarioRepositorio.buscarPorId(id)
                .orElseThrow(() -> new UsuarioNoEncontradoException(id));

        // Evita que el ultimo ADMIN se bloquee a si mismo y deje el sistema sin
        // nadie que pueda reactivar cuentas.
        if (autenticacion.getName().equalsIgnoreCase(usuario.getCorreo())) {
            throw new OperacionNoPermitidaException("Un administrador no puede desactivar su propia cuenta");
        }

        usuario.setActivo(false);
        return aRespuesta(usuarioRepositorio.guardar(usuario));
    }

    /**
     * Un usuario normal (ROLE_USUARIO) solo puede consultar/actualizar su
     * propia cuenta (comparando el correo del token contra el del recurso).
     * Un ADMIN puede consultar/actualizar cualquier cuenta.
     */
    private void verificarPropioOAdmin(Usuario usuario, Authentication autenticacion) {
        boolean esAdmin = autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> autoridad.getAuthority().equals("ROLE_ADMIN"));
        boolean esPropio = autenticacion.getName().equalsIgnoreCase(usuario.getCorreo());

        if (!esAdmin && !esPropio) {
            throw new AccesoNoAutorizadoException();
        }
    }

    private UsuarioResponse aRespuesta(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getRol().name(),
                usuario.isActivo(),
                usuario.getFechaCreacion()
        );
    }
}

