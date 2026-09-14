# Arquitectura

Debe contener:
- Diagrama general de infraestructura (Draw.io)
- Diagrama de red con interfaces y direcciones IP
- Tabla de puertos y orígenes permitidos
- Flujo de registro e inicio de sesión
- Flujo de alta de producto y carga de imagen (parcial 2)
- Flujo de respaldo y restauración (parcial 3)
- Inventario de servicios systemd

## Distribución de VMs (parcial 1)

| VM   | Hostname     | IP privada     | Responsabilidad                       | Internet |
|------|--------------|-----------------|-----------------------------------------|----------|
| VM 1 | srv-users    | 192.168.100.11  | Microservicio de usuarios               | No       |
| VM 2 | srv-products | 192.168.100.12  | Microservicio de productos (parcial 2)  | No       |
| VM 3 | srv-data     | 192.168.100.30  | MariaDB, MinIO, SFTP, respaldos         | No       |
| VM 4 | srv-gateway  | 192.168.100.10  | Nginx API Gateway, Cloudflare Tunnel    | Sí       |
