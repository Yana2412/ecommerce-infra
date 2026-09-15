# ecommerce-infra

Proyecto Integrador de Sistemas Operativos — Plataforma e-commerce distribuida sobre Rocky Linux
Universidad Autónoma de Chiapas · ISC. MCC. Ernaes Antonio Trujillo Vizuet

## Integrantes
- Angeles Dayana Rodas Hernandez — srv-users / microservicio de usuarios
- Axel Yeray Cruz Arreola — srv-data / base de datos y MinIO
- Diego Daniel Palafox Sánchez — srv-gateway / API Gateway y Cloudflare Tunnel
- Julio Santiago Verdugo Hernandez — srv-products / microservicio de productos

## Arquitectura
Ver `docs/arquitectura/`.

Internet -> Cloudflare Tunnel -> srv-gateway (Nginx) -> red privada 192.168.100.0/24
                                    - srv-users     (192.168.100.11:8081)
                                    - srv-products  (192.168.100.12:8082)
                                    - srv-data      (192.168.100.30) - MariaDB + MinIO + SFTP + respaldos

## Cómo ejecutar y probar la solución
_(completar conforme avance el proyecto: cómo levantar cada VM, cómo iniciar cada servicio, cómo probar con Swagger/Postman)_

## Estado por parcial
- [ ] Parcial 1 — Infraestructura y microservicio de usuarios
- [ ] Parcial 2 — Productos, imágenes y almacenamiento
- [ ] Parcial 3 — Gateway, seguridad y recuperación

## Reglas del repositorio
- Commits identificables por integrante.
- Funcionalidades organizadas mediante Issues.
- Cambios integrados mediante ramas y Pull Requests.
- Nunca subir contraseñas, tokens ni claves privadas (usar variables de entorno / .gitignore).
