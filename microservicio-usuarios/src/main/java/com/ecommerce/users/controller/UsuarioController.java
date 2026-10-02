package com.ecommerce.users.controller;

import com.ecommerce.users.dto.ActualizarUsuarioRequest;
import com.ecommerce.users.dto.RegistroRequest;
import com.ecommerce.users.dto.UsuarioResponse;
import com.ecommerce.users.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // POST /api/usuarios (registro) - publico
    @PostMapping
    public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
        UsuarioResponse respuesta = usuarioService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // GET /api/usuarios - solo ADMIN (Issue #9)
    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    // GET /api/usuarios/{id} - el propio usuario o un ADMIN (Issue #9)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(usuarioService.obtenerPorId(id, authentication));
    }

    // PUT /api/usuarios/{id} - el propio usuario o un ADMIN (Issue #9)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarUsuarioRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(usuarioService.actualizar(id, request, authentication));
    }

    // PATCH /api/usuarios/{id}/activar - solo ADMIN (Issue #10)
    @PatchMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponse> activar(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.activar(id));
    }

    // PATCH /api/usuarios/{id}/desactivar - solo ADMIN (Issue #10)
    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponse> desactivar(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(usuarioService.desactivar(id, authentication));
    }
}
