-- Base de datos de usuarios
-- Privilegios mínimos: crear un usuario de BD exclusivo para este microservicio

CREATE DATABASE IF NOT EXISTS ecommerce_usuarios;
USE ecommerce_usuarios;

CREATE TABLE IF NOT EXISTS usuarios (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol ENUM('ADMIN', 'USUARIO') NOT NULL DEFAULT 'USUARIO',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ultimo_acceso DATETIME NULL
);

-- Ejemplo: usuario de aplicación con privilegios mínimos (ajustar host/contraseña)
-- CREATE USER 'app_usuarios'@'192.168.100.%' IDENTIFIED BY 'CAMBIAR_ESTO';
-- GRANT SELECT, INSERT, UPDATE ON ecommerce_usuarios.* TO 'app_usuarios'@'192.168.100.%';
-- FLUSH PRIVILEGES;
