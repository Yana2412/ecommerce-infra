package com.example.demo.services;

import com.example.demo.models.Imagen;
import com.example.demo.models.Producto;
import com.example.demo.repositories.ImagenRepository;
import com.example.demo.repositories.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ImagenRepository imagenRepository;

    @Autowired
    private MinioService minioService;

    // Lista de tipos de archivo permitidos (JPG, PNG)
    private static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("image/jpeg", "image/png", "image/jpg");

    public Producto crearProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public List<Producto> obtenerTodos() {
        return productoRepository.findAll();
    }

    public List<Producto> obtenerActivos() {
        return productoRepository.findByActivoTrue();
    }

    public Producto obtenerPorId(Integer id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con el ID: " + id));
    }

    public Producto actualizarProducto(Integer id, Producto productoDetalles) {
        Producto producto = obtenerPorId(id);
        producto.setNombre(productoDetalles.getNombre());
        producto.setDescripcion(productoDetalles.getDescripcion());
        producto.setPrecio(productoDetalles.getPrecio());
        producto.setStock(productoDetalles.getStock());
        return productoRepository.save(producto);
    }

    public void cambiarEstado(Integer id, boolean activo) {
        Producto producto = obtenerPorId(id);
        producto.setActivo(activo);
        productoRepository.save(producto);
    }

    public Imagen subirImagenProducto(Integer id, MultipartFile archivo) throws Exception {
        Producto producto = obtenerPorId(id);

        // Validación de formato de imagen (Issue #34)
        String contentType = archivo.getContentType();
        if (contentType == null || !FORMATOS_PERMITIDOS.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Formato no permitido. Solo se aceptan imagenes JPG o PNG.");
        }

        // Subida a MinIO
        String urlImagen = minioService.subirImagen(archivo);

        // Guardar metadatos en PostgreSQL (Issue #33)
        Imagen imagen = new Imagen();
        imagen.setProducto(producto);
        imagen.setNombreArchivo(archivo.getOriginalFilename());
        imagen.setRutaMinio(urlImagen);
        imagen.setTipo(contentType);
        imagen.setTamano((int) archivo.getSize());

        return imagenRepository.save(imagen);
    }

    public Imagen obtenerImagenProducto(Integer productoId) {
        // Verificar que el producto exista
        obtenerPorId(productoId);
        return imagenRepository.findTopByProductoIdOrderByIdDesc(productoId)
                .orElseThrow(() -> new RuntimeException("El producto no tiene una imagen asociada"));
    }
}