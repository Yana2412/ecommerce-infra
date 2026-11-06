# Evidencia de Desarrollo — Issue #34: Validación de formato y tamaño de imagen

**Responsable:** Julio Santiago Verdugo Hernández  
**Fecha:** Noviembre 2026  
**Microservicio:** `srv-products`

Este documento detalla la implementación técnica para cumplir con los requerimientos del **Issue #34**, asegurando que las imágenes subidas a MinIO a través de la API cumplan con criterios estrictos de formato y tamaño.

---

## 1. Restricción de formatos permitidos (JPG, PNG)

Se implementó una lista blanca (whitelist) de tipos MIME permitidos dentro de la capa de servicio (`ProductoService.java`). Antes de procesar la subida a MinIO, se valida el `ContentType` del archivo entrante.

**Código implementado en `ProductoService.java`:**
```java
// Lista de tipos de archivo permitidos (JPG, PNG)
private static final List<String> FORMATOS_PERMITIDOS = Arrays.asList("image/jpeg", "image/png", "image/jpg");

public Imagen subirImagenProducto(Integer id, MultipartFile archivo) throws Exception {
    Producto producto = obtenerPorId(id);

    // Validación de formato de imagen (Issue #34)
    String contentType = archivo.getContentType();
    if (contentType == null || !FORMATOS_PERMITIDOS.contains(contentType.toLowerCase())) {
        throw new IllegalArgumentException("Formato no permitido. Solo se aceptan imagenes JPG o PNG.");
    }
    
    // ... (Lógica de subida a MinIO y guardado de metadatos)
}
```

## 2. Definición y validación de tamaño máximo de archivo

Para proteger el servidor de cargas excesivas de memoria y almacenamiento, se configuró un límite global a través de las propiedades de Spring Boot. Se restringió el tamaño máximo por archivo y por petición a **10MB**.

**Configuración en `application.properties`:**
```properties
# Límite de subida de archivos
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```
*Nota: Si un cliente intenta subir un archivo mayor a 10MB, Spring Boot intercepta automáticamente la petición antes de llegar al controlador, lanzando una excepción `MaxUploadSizeExceededException`.*

## 3. Respuesta con error claro (HTTP 400 Bad Request)

Se capturan las excepciones de validación en la capa del controlador (`ProductoController.java`) para asegurar que el cliente reciba un código de estado HTTP 400 junto con un mensaje descriptivo, en lugar de un error genérico del servidor (500).

**Código implementado en `ProductoController.java`:**
```java
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
```

## 4. Pruebas de Casos Realizadas (Testing)

Se verificaron mediante Postman los siguientes flujos para confirmar la robustez de las validaciones:

1.  **Formato inválido:** Intento de subida de un archivo `.pdf` o `.gif`. El sistema rechaza la petición devolviendo un `400 Bad Request` con el mensaje: *"Formato no permitido. Solo se aceptan imagenes JPG o PNG."*
2.  **Tamaño excedido:** Intento de subida de una imagen de 15MB. El servidor interrumpe la carga y devuelve un error indicando que se ha superado el límite configurado (10MB).
3.  **Archivo correcto:** Subida de una imagen `.png` de 350KB. El sistema procesa la imagen, la guarda en MinIO y devuelve un `200 OK` con los metadatos generados.