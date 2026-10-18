CREATE TABLE IF NOT EXISTS productos(
	id SERIAL PRIMARY KEY,
	nombre VARCHAR(150) NOT NULL,
	descripcion TEXT,
	precio NUMERIC(10, 2) NOT NULL CHECK (precio >=0),
	stock INT NOT NULL DEFAULT 0 CHECK (stock >=0),
	activo BOOLEAN NOT NULL DEFAULT TRUE,
	fecha_creacion TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS imagenes(
	id SERIAL PRIMARY KEY,
	producto_id INT NOT NULL,
	nombre_archivo VARCHAR (255) NOT NULL,
	ruta_minio VARCHAR(500) NOT NULL,
	tipo VARCHAR(50) NOT NULL,
	tamano INT NOT NULL,
	fecha_subida TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
	CONSTRAINT fk_imagenes_productos
		FOREIGN KEY (producto_id)
		REFERENCES productos(id)
		ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_productos_activo ON productos(activo);
CREATE INDEX IF NOT EXISTS idx_imagenes_producto_id ON imagenes(producto_id);
