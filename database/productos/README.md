# MODELO DE LA BASE DE DATOS - PRODUCTO E IMAGENES

Este contiene los scripts SQL de migracion para el microservicio de productos sobre PostgreSQL (`ecommerce_productos`).

#TABLAS

1.**`productos`**: Almacena el catalogo principal con id, nombre, descripcion, precio, stock, estado activo y fecha de creacion.
2.**`imagenes`**: Almacena la metadata de los archivos almacenados en MinIO.

##RELACIONES DE LAS TABLAS

1.**FK**: `imagenes.producto_id` referencia a `productos.id` con eliminacion en cascada (`ON DELETE CASCADE`).
