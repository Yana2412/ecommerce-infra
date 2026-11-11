#!/bin/bash
set -e
FECHA=$(date +%Y%m%d_%H%M%S)
DIR_TMP="/tmp/backup_$FECHA"
DIR_DESTINO="/respaldos"
LOG_FILE="/respaldos/logs/backup.log"
INICIO=$(date +%s)

echo "[$FECHA] Iniciando respaldo automatizado en srv-data..." >> "$LOG_FILE"
mkdir -p "$DIR_TMP"
sudo -u postgres pg_dump -F c ecommerce_usuarios > "$DIR_TMP/ecommerce_usuarios_$FECHA.dump"
sudo -u postgres pg_dump -F c ecommerce_productos > "$DIR_TMP/ecommerce_productos_$FECHA.dump"
if [ -d "/data" ]; then
    cp -r /data "$DIR_TMP/minio_data"
elif [ -d "/var/lib/minio" ]; then
    cp -r /var/lib/minio "$DIR_TMP/minio_data"
fi
ARCHIVO_FINAL="$DIR_DESTINO/backup_ecommerce_$FECHA.tar.gz"
tar -czf "$ARCHIVO_FINAL" -C "$DIR_TMP" .
sha256sum "$ARCHIVO_FINAL" > "$ARCHIVO_FINAL.sha256"
chown root:respaldos "$ARCHIVO_FINAL" "$ARCHIVO_FINAL.sha256"
chmod 640 "$ARCHIVO_FINAL" "$ARCHIVO_FINAL.sha256"

rm -rf "$DIR_TMP"

find "$DIR_DESTINO" -maxdepth 1 -name "backup_ecommerce_*.tar.gz*" -mtime +7 -exec rm -f {} \;

FIN=$(date +%s)
DURACION=$((FIN - INICIO))
echo "[$FECHA] Respaldo completado exitosamente en $DURACION segundos: $ARCHIVO_FINAL" >> "$LOG_FILE"

exit 0
