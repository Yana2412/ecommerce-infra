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