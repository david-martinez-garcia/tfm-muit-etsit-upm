#!/bin/bash

echo 'Escenario de red de VNX'
echo 'Desarrollo de mecanismos de redirección de tráfico en redes de campus basadas en SDN para distribución de contenidos multimedia'
echo 'Escenario para validación y realización de pruebas con redirección por protocolo DNS'

echo ''
echo ''

echo 'Se reiniciará Open vSwitch antes de arrancar el escenario...'
sudo ovs-vsctl --if-exists del-br mec1
sudo ovs-vsctl --if-exists del-br mec2
sudo ovs-vsctl --if-exists del-br itc
sudo ovs-vsctl --if-exists del-br central
sudo ovs-vsctl emer-reset
sudo /etc/init.d/openvswitch-switch restart
echo 'Open vSwitch reiniciado'

echo ''
echo ''

echo 'Se creará el escenario...'
sleep 1
sudo vnx -f escenario_red_vnx_dns.xml --create
sleep 1
echo 'Escenario de VNX creado'
sleep 1

echo ''
echo ''

echo 'Se arrancará el controlador ONOS...'
sudo vnx -f escenario_red_vnx_dns.xml --execute start-onos
sleep 1
echo 'Controlador ONOS arrancado. Esperando 30 segundos antes de continuar para completar el arranque...'
sleep 30

echo ''
echo ''

echo 'Se activarán las aplicaciones base de ONOS...'
sudo vnx -f escenario_red_vnx_dns.xml --execute activate-onos-base-apps
sleep 1
echo 'Aplicaciones base activadas. Esperando 15 segundos antes de continuar para completar la activación de las aplicaciones...'
sleep 15

echo ''
echo ''

echo 'Se instalará el Orquestador (aplicación de ONOS)...'
sudo vnx -f escenario_red_vnx_dns.xml --execute install-orchestrator
sleep 1
echo 'Orquestador instalado'
sleep 1

echo ''
echo ''

echo 'Se configurará el Orquestador para usar redirección por DNS...'
sudo vnx -f escenario_red_vnx_dns.xml --execute configure-orchestrator
sleep 1
echo 'Orquestador configurado'
sleep 1

echo ''
echo ''

echo 'Se arrancará el Coordinador...'
sudo vnx -f escenario_red_vnx_dns.xml --execute start-coordinator
sleep 1
echo 'Coordinador arrancado'
sleep 1

echo ''
echo ''

echo 'Se arrancarán los vProxy (Apache Traffic Server)...'
sudo vnx -f escenario_red_vnx_dns.xml --execute start-vProxy1
sleep 1
sudo vnx -f escenario_red_vnx_dns.xml --execute start-vProxy2
sleep 1
echo 'vProxy arrancados'
sleep 1

echo ''
echo ''

echo 'Se arrancarán las vCache (Apache HTTP Server)...'
sudo vnx -f escenario_red_vnx_dns.xml --execute start-vCache1
sleep 1
sudo vnx -f escenario_red_vnx_dns.xml --execute start-vCache2
sleep 1
sudo vnx -f escenario_red_vnx_dns.xml --execute start-vCache3
sleep 1
echo 'vCache arrancadas'
sleep 1

echo ''
echo ''

echo 'Escenario lanzado. Para detenerlo, ejecutar el script escenario_red_vnx_dns_stop.sh'


