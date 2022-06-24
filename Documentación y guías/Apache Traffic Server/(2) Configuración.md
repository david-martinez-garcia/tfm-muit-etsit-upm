# Configuración de Apache Traffic Server

En este documento se describen los detalles principales sobre la configuración de Apache Traffic Server. La configuración se realiza mediante ficheros específicos, los cuales se ubican en el directorio `/opt/ts/etc/trafficserver`. Se tienen ficheros para las principales características, aunque los más importantes son los ficheros `records.config`, para la configuración general, y `remap.config`, para las reglas de mapeo de URLs.

La documentación de Apache Traffic Server sirve como punto de partida fundamental para la gestión y operación: https://docs.trafficserver.apache.org/en/latest/index.html.

## 1. Modificación del script de ejecución de Apache Traffic Server

La instalación de Apache Traffic Server genera, por defecto, un script de ejecución que es necesario modificar para poder observar todos los logs de la aplicación. Para modificar este script, dirigirse al directorio de binarios de ATS y editar el fichero `trafficserver`:

```
# cd /opt/ts/bin/
# nano trafficserver
```

Buscar las líneas siguientes:

```
TM_DAEMON=${TM_DAEMON:-$TS_BASE/opt/ts/bin/traffic_manager}
TM_DAEMON_ARGS=""
TS_DAEMON=${TS_DAEMON:-$TS_BASE/opt/ts/bin/traffic_server}
TS_DAEMON_ARGS=""
```

y modificarlas para que queden de la siguiente manera:

```
TM_DAEMON=${TM_DAEMON:-$TS_BASE/opt/ts/bin/traffic_manager}
TM_DAEMON_ARGS="--bind_stdout /opt/ts/var/log/trafficserver/traffic.out --bind_stderr /opt/ts/var/log/trafficserver/traffic.out"
TS_DAEMON=${TS_DAEMON:-$TS_BASE/opt/ts/bin/traffic_server}
TS_DAEMON_ARGS=""
```

Guardar y cerrar el fichero.

## 2. Configuración general de ATS

La configuración general de ATS se realiza mediante el fichero `records.config` ubicado en el directorio `/opt/ts/etc/trafficserver/`.

Para configurar ATS como un **proxy inverso transparente y sin cacheo de contenidos** hay que configurar las siguientes opciones de la siguiente manera:

```
CONFIG proxy.config.http.cache.http INT 0
CONFIG proxy.config.reverse_proxy.enabled INT 1
CONFIG proxy.config.url_remap.pristine_host_hdr INT 1
CONFIG proxy.config.url_remap.remap_required INT 1
```

La configuración del puerto en el que escuchará ATS las peticiones de los clientes se realiza en la directiva `proxy.config.http.server_ports`. Por defecto se utiliza el puerto 8080 para IPv4 e IPv6.

En este modo de funcionamiento, ATS recibe peticiones de los clientes y las redirige a servidores origen, los cuales responden a ATS y éste, a su vez, a los clientes. Desde el punto de vista de los clientes el funcionamiento es transparente, ya que no conocen la infraestructura que hay detrás. Los clientes se comunican con el proxy ATS y, desde su perspectiva, actúa como servidor de contenidos.

Este modo requiere que se establezcan reglas de mapeo, las cuales se describen en el siguiente apartado.

Es posible configurar los tipos de logs que mostrará ATS en el fichero para tal efecto. La configuración se realiza en la directiva `proxy.config.diags.debug.tags`. Para que se muestren también logs de la ejecución de scripts de Lua, modificar la directiva para que quede de la siguiente manera:

```
CONFIG proxy.config.diags.debug.enabled INT 1
CONFIG proxy.config.diags.debug.tags STRING http|dns|ts_lua
```

## 3. Configuración de reglas de mapeo de URLs

El funcionamiento de proxy inverso transparente requiere que se especifquen reglas de mapeo en ATS para las peticiones procedentes de los clientes. Así, estas reglas permiten definir a qué servidores origen se redirigirán las peticiones a ATS para una URL determinada. La configuración de estas reglas de mapeo se realiza en el fichero `remap.config` ubicado en `/opt/ts/etc/trafficserver/`.

De manera fundamental, se tendrán tantas reglas `map` como mapeos de URLs se quieran tener. Por ejemplo, si se tiene un escenario con un proxy ATS y un servidor Web Apache detrás, la regla de mapeo se configuraría de la siguiente manera:

```
map http://<ip_del_proxy_ATS>:<puerto_del_proxy_ATS>/ http://<ip_del_servidor_web_apache>:<puerto_del_servidor_web_apache>/
reverse_map http://<ip_del_servidor_web_apache>:<puerto_del_servidor_web_apache>/ http://<ip_del_proxy_ATS>:<puerto_del_proxy_ATS>/
```

Nótese que, para cada regla `map`, existe una regla `reverse_map`. Esta regla traduce las URLs de las respuestas de los servidores origen para que apunten a ATS.
