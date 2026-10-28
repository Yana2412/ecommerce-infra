# Instalación de MinIO en srv-data

**VM:** srv-data (192.168.100.30) · Rocky Linux 9.8 · **Responsable:** Axel

## Instalación
Binario en /usr/local/bin/minio. Versión: (pega la salida de `minio --version`).
Nota: la edición comunitaria ya no distribuye binarios nuevos; es una versión congelada.

## Usuario y datos
Usuario `minio-user` sin shell, datos en /var/lib/minio/data.

## Credenciales
En /etc/default/minio (root:root, permisos 600). No se suben al repositorio.
Plantilla sin secretos: minio/minio.env.example

## Servicio systemd
Archivo: systemd/minio.service. Comandos de verificación usados.

## Firewall
9000 solo desde 192.168.100.12. Consola 9001 sin regla externa.

## SELinux
Ajuste aplicado: restorecon sobre /usr/local/bin/minio.

## Verificación
Salida del health check, consola web y prueba tras reinicio.
