# Evidencia de Desarrollo — Issue #35: Servicio systemd del microservicio de productos

**Responsable:** Julio Santiago Verdugo Hernández

**Fecha:** Noviembre 2026

**Microservicio:** `srv-products`

Este documento detalla el proceso de transición del despliegue manual (mediante `java -jar`) a un despliegue persistente y automatizado gestionado por `systemd` en el servidor Rocky Linux. Esto garantiza alta disponibilidad y tolerancia a fallos para la API.

## 1. Empaquetado y Transferencia del Artefacto
Previamente al servicio, la aplicación fue empaquetada con Maven omitiendo las pruebas para agilizar el proceso, y transferida al servidor mediante el protocolo SCP a un directorio dedicado:

```bash
# Empaquetado en la máquina host
.\mvnw.cmd clean package -DskipTests

# Transferencia a Rocky Linux
scp target\demo-0.0.1-SNAPSHOT.jar jhernandez@10.34.220.219:/home/jhernandez/ecommerce-app/ecommerce-api.jar
```

## 2. Creación del archivo microservicio-productos.service
Se creó el archivo de configuración del servicio en la ruta del sistema `/etc/systemd/system/microservicio-productos.service`.

**Comando utilizado:**
```bash
sudo nano /etc/systemd/system/microservicio-productos.service
```

**Configuración implementada:**
```ini
[Unit]
Description=Microservicio de Productos
After=network.target

[Service]
# Configurar ejecución como usuario no-root
User=jhernandez
WorkingDirectory=/home/jhernandez/ecommerce-app

# Comando de arranque apuntando a la ruta absoluta
ExecStart=/usr/bin/java -jar /home/jhernandez/ecommerce-app/ecommerce-api.jar
SuccessExitStatus=143

# Verificar reinicio automático ante fallos
Restart=on-failure
RestartSec=5

[Install]
WantedBy=multi-user.target
```
*Nota: La directiva `User=jhernandez` asegura que el servicio se ejecute con privilegios limitados (no-root) por motivos de seguridad, mientras que `Restart=on-failure` garantiza que el sistema operativo levante la aplicación nuevamente si el proceso muere inesperadamente (ej. caída de conexión a base de datos).*

## 3. Habilitar inicio automático y ejecución
Una vez guardado el archivo de configuración, se le notificó al demonio de `systemd` sobre los cambios y se procedió a habilitar el inicio automático (para que arranque junto con el sistema operativo) y a iniciar el servicio de forma inmediata.

**Comandos ejecutados:**
```bash
sudo systemctl daemon-reload
sudo systemctl enable --now microservicio-productos.service
```

## 4. Validación de Estado y Monitoreo (Logs)
Se verificó que el servicio estuviera corriendo correctamente en segundo plano y se comprobó la lectura de los logs del sistema para confirmar el comportamiento de la aplicación (incluyendo sus reintentos de conexión a la base de datos).

**Comandos de gestión:**
```bash
# Revisar el estado (active running / failed)
sudo systemctl status microservicio-productos.service

# Monitorear los registros (logs) del sistema en tiempo real
sudo journalctl -u microservicio-productos.service -f
```