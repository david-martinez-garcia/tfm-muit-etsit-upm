#!/bin/bash

echo "Se descargarán y desempaquetarán los escenarios de VNX"

echo ""
echo ""

echo "Se creará un directorio para almacenar los escenarios en: $(pwd)/escenarios_vnx_completos"

echo ""
echo ""

mkdir -p escenarios_vnx_completos

wget https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/releases/download/v1.0.0/escenarios_vnx_completos.tar.gz -P $(pwd)/escenarios_vnx_completos

cd $(pwd)/escenarios_vnx_completos

tar -xvzpf escenarios_vnx_completos.tar.gz

rm -Rf *.tar.gz

echo ""
echo ""

echo "Completado"
