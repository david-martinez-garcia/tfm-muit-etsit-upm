# Despliegue y ejecución de los escenarios de VNX

En este documento se presentan las instrucciones para descargar y lanzar los escenarios completos de VNX desarrollados.

En primer lugar, descargar y ejecutar el _script_ que automatiza la descarga y desempaquetado del _rootfs_ de VNX en el que se basan las máquinas virtuales/contenedores
de los escenarios:

```
$ wget https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/blob/main/Escenarios%20de%20VNX/Otros%20scripts/download_unpack_rootfs.sh
$ chmod +x download_unpack_rootfs.sh
$ ./download_unpack_scenarios.sh
```

Después, descargar y ejecutar el _script_ que automatiza la descarga y desempaquetado de los escenarios de VNX:

```
$ wget https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/blob/main/Escenarios%20de%20VNX/Otros%20scripts/download_unpack_scenarios.sh
$ chmod +x download_unpack_scenarios.sh
$ ./download_unpack_scenarios.sh
```

Tras la ejecución de estos comandos, quedará completada la descarga y el desempaquetado de todos los ficheros necesarios.

Para lanzar los escenarios, desplazarse al directorio que los contiene:

```
$ cd escenarios_vnx_completos/
```

## Ejecución del escenario con redirección mediante interceptación y procesado de los establecimientos de conexión TCP

Para lanzar este escenario, ejecutar el siguiente _script_:

```
$ ./escenario_red_vnx_tcp_run.sh
```

El despliegue y configuración tardará unos minutos en completarse. Si se quiere detener la ejecución del escenario, ejecutar el siguiente _script_:

```
$ ./escenario_red_vnx_tcp_stop.sh
```

## Ejecución del escenario con redirección mediante resolución de DNS

Para lanzar este escenario, ejecutar el siguiente _script_:

```
$ ./escenario_red_vnx_dns_run.sh
```

El despliegue y configuración tardará unos minutos en completarse. Si se quiere detener la ejecución del escenario, ejecutar el siguiente _script_:

```
$ ./escenario_red_vnx_dns_stop.sh
```

## Interacción con los escenarios

Interactuar con las máquinas virtuales según se desee. Las credenciales de acceso a sus terminales de comandos son las siguientes:
- Usuario: `root`.
- Contraseña: `xxxx`.

Para interactuar con el navegador Web Mozilla Firefox instalado en los clientes, **desde terminales de comandos en el sistema anfitrión**, ejecutar:

**Para la máquina `Cliente1`:**
```
$ ssh -X root@172.16.0.11
```

**Para la máquina `Cliente2`:**
```
$ ssh -X root@172.16.0.12
```

Una vez establecidas las sesiones SSH, ejecutar el siguiente comando, que abrirá una ventana de Mozilla Firefox desde las máquinas virtuales:

```
# firefox
```

Con el navegador Web abierto, introducir en la barra de direcciones la URL `http://contenidos.pruebasgiros.home.arpa:8080/` y comprobar el funcionamiento.
Para analizar el tráfico de red, instalar la herramienta _Wireshark_, ejecutarla y capturar en las interfaces cuyos nombres terminan en la cadena de caracteres `-e1`.

