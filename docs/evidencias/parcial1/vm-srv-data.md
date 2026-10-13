# Evidencia - VM srv-data (Issue #2)

**Responsable:** axelcruz0305
**Rama:** infra/2-vm-srv-data

## 1. Rocky Linux Instalado y configurado
- Hostname: srv-data
- IP interna: 192.168.100.30 (interfaz enp0s8)
- Comando usado: `sudo hostnamectl set-hostname srv-data`
- Comando IP: `sudo nmcli connection add type ethernet ifname enp0s8 con-name enp0s8 ipv4.addresses 192.168.100.30/24 ipv4.method manual`

## 2. PostgreSQL instalado
- Comando: `sudo dnf install postgresql-server postgresql -y`
- Inicializacion: `sudo postgresql-setup --initdb`
- Servicio activo: `sudo systemctl enable --now postgresql`

## 3. Base de datos creada
- Base: ecommerce_usuarios
- Usuario de privilegios minimos: usuario_app
- Comando: `sudo -u postgres psql -c "CREATE DATABASE ecommerce_usuarios;"`

## Incidentes encontrados y resueltos
**Síntoma:** La conexion remota con `psql -h 192.168.100.30 -U usuarios_app` fallaba con la autenticacion password aun con la contraseña correcta.
**Diagnostico:** Se probo con metodo `trust` en pg_hba.conf y si conecto.
**Causa:** Incompatibilidad del metodo scram-sha256
**Solucion** Se cambio el metodo por md5
**Relacion con sistemas operativos** Gestión de servicios (systemd), configuracion de red (interfaces, firewall) y control de acceso/autenticacion a nivel de servicio.

## Conexión verificada
