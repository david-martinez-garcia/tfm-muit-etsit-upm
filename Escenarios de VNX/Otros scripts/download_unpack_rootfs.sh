#!/bin/bash

echo 'Se descargará y desempaquetará el rootfs de VNX utilizado en los escenarios'

echo ''
echo ''

echo 'El directorio objetivo es: /usr/share/vnx/filesystems/'

echo ''
echo ''

echo '¡¡ATENCIÓN!! COMPRUEBE QUE NO TIENE OTRO ROOTFS CON EL MISMOS NOMBRES. PUEDE COMPROBAR EL NOMBRE DE ESTE ROOTFS EN EL CÓDIGO DE ESTE SCRIPT'

echo ''
echo ''

sleep 10

sudo wget https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/releases/download/v1.0.0/vnx_rootfs_lxc_ubuntu64-18.04-v025.tar.gz -P /usr/share/vnx/filesystems/

cd /usr/share/vnx/filesystems/

sudo tar -xvzpf vnx_rootfs_lxc_ubuntu64-18.04-v025.tar.gz && sudo ln -s vnx_rootfs_lxc_ubuntu64-18.04-v025 rootfs_lxc_ubuntu64

sudo rm -Rf *.tar.gz

echo ''
echo ''

echo 'Completado'


