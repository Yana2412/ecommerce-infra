# Documentación de Instalación y Configuración - srv-users (VM 1)

## 1. Datos Generales de la VM
- **Hostname estático:** srv-users
- **Sistema Operativo:** Rocky Linux 9.8 (Blue Onyx)
- **Kernel:** Linux 5.14.0-687.10.1.el9_8.0.1.x86_64
- **Arquitectura:** x86_64 (VirtualBox)
- **Usuario no-root:** `angeles` (perteneciente al grupo `wheel`)
- **IP Privada (enp0s8):** `192.168.100.11/24`
- **Puerto configurado:** `8081/tcp`

---

## 2. Configuración del Hostname

Se asignó el nombre de host estático a la máquina virtual utilizando `hostnamectl`:

```bash
sudo hostnamectl set-hostname srv-users


## Verificación del Hostname:
hostnamectl

##Salida:
Static hostname: srv-users
Icon name: computer-vm
Chassis: vm
Machine ID: c9fb187fae2d41cca8e2cfbb2d2d1850
Boot ID: c02d7a38121e43f791cd292d6e280a7f
Virtualization: oracle
Operating System: Rocky Linux 9.8 (Blue Onyx)
CPE OS Name: cpe:/o:rocky:rocky:9::baseos
Kernel: Linux 5.14.0-687.10.1.el9_8.0.1.x86_64
Architecture: x86-64
Hardware Vendor: innotek GmbH
Hardware Model: VirtualBox
Firmware Version: VirtualBox


## Creación del Usuario No-Root (Administrador)
Se creó el usuario no-root angeles y se le asignaron privilegios de superusuario agregándolo al grupo wheel

´´Bash
sudo useradd -m -G wheel angeles
sudo passwd angeles
´
##Verificación de Privilegios de Sudo:

sudo -l -U angeles

##4. Configuración de Red e IP Estática

Estado inicial de dispositivos:
nmcli device status

Salida:
DEVICE  TYPE      STATE                                CONNECTION          
enp0s3  ethernet  conectado                            enp0s3              
enp0s8  ethernet  conectando (obteniendo configuración IP)  Conexión cableada 1 
lo      loopback  connected (externally)               lo


##Comandos de asignación de IP estática:
sudo nmcli con mod "Conexión cableada 1" ipv4.addresses 192.168.100.11/24
sudo nmcli con mod "Conexión cableada 1" ipv4.method manual
sudo nmcli con up "Conexión cableada 1"

##verificacion de ip 
ip addr show enp0s8
3: enp0s8: <BROADCAST,MULTICAST,UP,LOWER_UP> mtu 1500 qdisc fq_codel state UP group default qlen 1000
    link/ether 08:00:27:ee:05:c2 brd ff:ff:ff:ff:ff:ff
    inet 192.168.100.11/24 brd 192.168.100.255 scope global noprefixroute enp0s8
    valid_lft forever preferred_lft forever
    inet6 fe80::9eaf:9138:163e:78d9/64 scope link noprefixroute 
    valid_lft forever preferred_lft forever

##Apertura del Puerto 8081 en firewalld:
sudo firewall-cmd --permanent --add-port=8081/tcp
sudo firewall-cmd --reload
sudo firewall-cmd --list-ports

##salida
8081/tcp

---

## 5. Compilar y ejecutar el microservicio (Issue #8)

El desarrollo se hace en IntelliJ IDEA en Windows; Rocky Linux solo tiene el
JRE/JDK instalado (sin Maven), así que el flujo es: **compilar el .jar en
Windows y transferirlo a la VM**.

### 5.1 Generar el .jar en IntelliJ (Windows)
1. Abrir el módulo `microservicio-usuarios/src` en IntelliJ.
2. Panel Maven (lateral derecho) → `microservicio-usuarios` → `Lifecycle` →
   doble clic en `package` (o `mvn clean package` en la terminal integrada).
3. Verificar que el Java del proyecto sea 17 (`File > Project Structure > SDK`),
   igual que en el `pom.xml`.
4. El jar queda en `microservicio-usuarios/src/target/microservicio-usuarios-0.0.1-SNAPSHOT.jar`.

### 5.2 Verificar Java en Rocky Linux
```bash
java -version
```
Debe ser Java 17 o superior. Si `srv-users` tiene Rocky Linux minimal, puede
que solo tenga el JRE; para compilar cosas en la propia VM (no obligatorio
para este flujo) haría falta `sudo dnf install java-17-openjdk-devel`.

### 5.3 Copiar el jar a la VM
Desde Windows (PowerShell, con OpenSSH client instalado, o WinSCP):
```bash
scp microservicio-usuarios-0.0.1-SNAPSHOT.jar angeles@192.168.100.11:/tmp/
```

### 5.4 Colocar el jar y crear el servicio en la VM
```bash
sudo mkdir -p /opt/microservicio-usuarios
sudo mv /tmp/microservicio-usuarios-0.0.1-SNAPSHOT.jar /opt/microservicio-usuarios/
sudo chown angeles:angeles /opt/microservicio-usuarios/microservicio-usuarios-0.0.1-SNAPSHOT.jar

sudo cp systemd/microservicio-usuarios.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now microservicio-usuarios
sudo systemctl status microservicio-usuarios
```
El servicio corre como el usuario `angeles` (no root), como pide la regla del
microservicio. Recuerda cambiar el `JWT_SECRET` del archivo `.service` por un
valor real antes de usarlo en la VM.

### 5.5 Ver logs
```bash
journalctl -u microservicio-usuarios -f
```

### 5.6 Probar los endpoints
```bash
curl -X POST http://192.168.100.11:8081/api/usuarios \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Angeles","correo":"angeles@ejemplo.com","password":"password123"}'

curl -X POST http://192.168.100.11:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"angeles@ejemplo.com","password":"password123"}'
```
El login debe responder con un JSON que incluye `token`, `correo` y `rol`.

### 5.7 Probar sin conexión a la BD (perfil "memoria")
Mientras no haya red hacia `srv-data`, la app corre en modo temporal: por
defecto `spring.profiles.active=memoria` en `application.properties`, así que
`mvn spring-boot:run` (o correr `UsersApplication` desde IntelliJ) ya arranca
sin necesitar PostgreSQL, guardando los usuarios en una lista en RAM
(`UsuarioRepositorioMemoriaAdapter`). Sirve para probar `POST /api/usuarios`
y `POST /api/auth/login` de una vez, aunque los datos se pierdan al reiniciar.

Cuando ya tengan conexión real a `srv-data`, cambien una sola línea en
`application.properties`:
```properties
spring.profiles.active=bd
```
y revisen que `application-bd.properties` tenga el usuario/contraseña
correctos de PostgreSQL. No hay que tocar nada más del código: el
`UsuarioService` usa la interfaz `UsuarioRepositorioPuerto`, y Spring elige
automáticamente la implementación (memoria o JPA/PostgreSQL) según el perfil.