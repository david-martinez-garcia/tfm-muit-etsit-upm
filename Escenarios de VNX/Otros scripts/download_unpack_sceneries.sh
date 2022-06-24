#!/bin/bash

echo 'Se descargarán y desempaquetarán los escenarios de VNX'

echo ''
echo ''

echo 'Se creará un directorio para almacenar los escenarios en: $(pwd)/escenarios_red_vnx'

echo ''
echo ''

mkdir -p escenarios_red_vnx

sudo wget https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/releases/download/v1.0.0/escenarios_red_vnx.tar.gz -P $(pwd)/escenarios_red_vnx

cd $(pwd)/escenarios_red_vnx

sudo tar -xvzpf escenarios_red_vnx.tar.gz

sudo rm -Rf *.tar.gz

echo ''
echo ''

echo 'Completado'
