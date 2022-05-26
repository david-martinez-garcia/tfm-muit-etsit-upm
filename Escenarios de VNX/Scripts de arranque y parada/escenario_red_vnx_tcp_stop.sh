#!/bin/bash

echo 'Escenario de red de VNX'
echo 'Desarrollo de mecanismos de redirección de tráfico en redes de campus basadas en SDN para distribución de contenidos multimedia'
echo 'Escenario para validación y realización de pruebas con redirección por protocolo TCP (SYN)/IPv4'

echo ''
echo ''

echo 'Se detendrá y destruirá el escenario...'
sleep 1
sudo vnx -f escenario_red_vnx_tcp.xml --destroy
sleep 1
echo 'Escenario de VNX parado y destruido'
sleep 1

echo ''
echo ''

echo 'Se reiniciará Open vSwitch...'
sudo ovs-vsctl --if-exists del-br mec1
sudo ovs-vsctl --if-exists del-br mec2
sudo ovs-vsctl --if-exists del-br itc
sudo ovs-vsctl --if-exists del-br central
sudo ovs-vsctl emer-reset
sudo /etc/init.d/openvswitch-switch restart
echo 'Open vSwitch reiniciado'

echo ''
echo ''


