# Ejecución y gestión de ONOS en versión de producción
En este documento se describen las instrucciones para ejecutar y gestionar ONOS en versión de producción.

La ejecución puede realizarse de dos maneras diferentes:
- Utilizando el lanzador del servicio, que se invoca de manera manual.
- Ejecutando ONOS como servicio, habiendo configurado el sistema anteriormente.

**NOTA:** Todos los comandos siguientes se ejecutan como usuario `root`.

## 1. Ejecución de ONOS

### 1.1. Ejecución de manera manual
ONOS no recomienda la ejecución como usuario con permisos elevados (`sudo` o `root`). No obstante, para un entorno de pruebas, es la manera más sencilla de ejecutarlo.
Para lanzar ONOS de manera manual, ejecutar en un terminal:

```
# /opt/onos/bin/onos-service start
```

Para finalizar la ejecución de ONOS, en la ventana de terminal desde donde se lanzó, pulsar `Ctrl + C`.

### 1.2. Ejecución de ONOS como servicio o *daemon* del sistema

#### Preparación

En primer lugar, instalar los ficheros de servicio y habilitarlo. Ejecutar los siguientes comandos:

```
# cp /opt/onos/init/onos.initd /etc/init.d/onos
# cp /opt/onos/init/onos.conf /etc/init/onos.conf
# cp /opt/onos/init/onos.service /etc/systemd/system/
# systemctl daemon-reload
# systemctl enable onos
```

En segundo lugar, crear el fichero de opciones de ONOS `/opt/onos/options`, que utilizará el servicio de ONOS para configurarse. Para ello, ejecutar el siguiente comando:

```
# nano /opt/onos/options
```

Y escribir en él el siguiente contenido:

```
ONOS_USER=sdn
# Optional: add any apps here that you wish to activate by default
ONOS_APPS=org.onosproject.drivers,org.onosproject.gui2
```

Guardar y salir del editor. En `ONOS_APPS=` es posible especificar una lista de aplicaciones que se activarán con la ejecución del servicio de ONOS. No obstante, también se pueden activar de manera manualmente desde la CLI o la GUI. La activación manual de aplicaciones se describe en el **Apartado 3** del presente documento.

Ejecutar el siguiente comando para dar permisos al fichero que se acaba de crear:

```
# chmod 777 /opt/onos/options
```

#### Ejecución, parada, reinicio y comprobación del estado

Para **ejecutar** ONOS como servicio se pueden lanzar los siguientes comandos:

```
# service onos start

(o alternativamente)

# systemctl start onos.service
```

Para **detener** el servicio de ONOS se pueden ejecutar los siguientes comandos:

```
# service onos stop

(o alternativamente)

# systemctl stop onos.service
```

Para **reiniciar** el servicio de ONOS se pueden ejecutar los siguientes comandos:

```
# service onos restart

(o alternativamente)

# systemctl restart onos.service
```

Para **comprobar el estado** del servicio de ONOS se pueden ejecutar los siguientes comandos:

```
# service onos status

(o alternativamente)

# systemctl status onos.service
```

## 2. Acceso a las interfaces de usuario de ONOS
ONOS proporciona dos interfaces de usuario:
- Una interfaz por línea de comandos (CLI).
- Una interfaz gráfica basada en Web (GUI).

### 2.1. Acceso a la CLI
Para acceder a la CLI, abrir una ventana de terminal y ejecutar:

```
# ssh -p 8101 onos@localhost
```
**La contraseña es `rocks`**.

Alternativamente:

```
# ssh -p 8101 karaf@localhost
```
**La contraseña es `karaf`**.

### 2.2. Acceso a la GUI
Para acceder a la GUI basada en Web, abrir un navegador de Internet (como Mozilla Firefox) y abrir la URL:

```
http://<dirección_IP_de_ONOS>:8181/onos/ui
```

La dirección IP de ONOS será la de la máquina o contenedor en la que esté corriendo. Las credenciales de acceso son las mismas que se pueden utilizar para acceder a la CLI.

## 3. Activación de aplicaciones de ONOS

La primera vez que se lance ONOS se ejecutará sin ninguna aplicación. Para poder controlar redes SDN y realizar la funcionalidad principal, hay que activar varias aplicaciones base, que son:
- `org.onosproject.drivers`: Controladores básicos.
- `org.onosproject.openflow`: Soporte para el protocolo OpenFlow.
- `org.onosproject.gui2`: Interfaz gráfica basada en Web.

La activación de estas aplicaciones puede activar otras como dependencias o subaplicaciones derivadas.

Para activar estas aplicaciones, acceder a la CLI con:

```
# ssh -p 8101 onos@localhost
```
**La contraseña es `rocks`**.

O con:

```
# ssh -p 8101 karaf@localhost
```
**La contraseña es `karaf`**.

Una vez dentro de la CLI, activar las aplicaciones una a una con el comando:

```
karaf@root> app activate <nombre_de_la_aplicación>
```

Para comprobar qué aplicaciones están activadas, ejecutar:

```
karaf@root> apps -s -a
```

La activación de las aplicaciones también se puede realizar desde la GUI, accediendo al menú desplegable `Applications`.
