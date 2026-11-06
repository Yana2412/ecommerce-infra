package com.example.demo.controllers;

import com.example.demo.models.Imagen;
import com.example.demo.models.Producto;
import com.example.demo.services.ProductoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @Autowired
    private ProductoService productoService;

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@Valid @RequestBody Producto producto) {
        return new ResponseEntity<>(productoService.crearProducto(producto), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerTodos() {
        // Retorna unicamente los productos activos para el listado publico (Issue #32)
        return ResponseEntity.ok(productoService.obtenerActivos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto(@PathVariable Integer id, @Valid @RequestBody Producto producto) {
        return ResponseEntity.ok(productoService.actualizarProducto(id, producto));
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activarProducto(@PathVariable Integer id) {
        productoService.cambiarEstado(id, true);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarProducto(@PathVariable Integer id) {
        productoService.cambiarEstado(id, false);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(@PathVariable Integer id, @RequestParam("file") MultipartFile file) {
        try {
            Imagen imagenGuardada = productoService.subirImagenProducto(id, file);
            return ResponseEntity.ok(imagenGuardada);
        } catch (IllegalArgumentException e) {
            // Error 400 cuando el archivo no cumple las validaciones (Issue #34)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al subir la imagen");
        }
    }

    @GetMapping("/{id}/imagen")
    public ResponseEntity<?> obtenerImagen(@PathVariable Integer id) {
        try {
            Imagen imagen = productoService.obtenerImagenProducto(id);
            return ResponseEntity.ok(imagen);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}