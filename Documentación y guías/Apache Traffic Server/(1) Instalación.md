# Instalación de Apache Traffic Server

En este documento se describen los pasos a seguir para instalar Apache Traffic Server (en adelante ATS).

Se describen instrucciones para realizar una instalación completa de ATS, incluyendo el soporte para la ejecución de scripts en Lua a través de su plugin correspondiente.

**La instalación se realiza en el _rootfs_ de VNX correspondiente (Ubuntu 18.04 LTS de 64 bits)**.

**NOTA:** Todos los comandos siguientes se ejecutan como usuario `root`.

## 1. Preparación del entorno

En primer lugar, se actualiza el sistema:

```
# apt update && apt dist-upgrade && apt autoremove && apt autoclean
```

Nos aseguramos también de tener `GCC`, `make` y otras librerías y dependencias adicionales instaladas:

```
# apt install gcc make build-essential libreadline-dev libncurses5-dev unzip subversion git nano wget curl
```

## 2. Instalación de LuaJIT (2.1.0 beta 3) y Lua (5.1.5)

Con el sistema y los paquetes actualizados, se instalan LuaJIT y Lua.

Para ello, primero se descarga LuaJIT:

```
# wget https://luajit.org/download/LuaJIT-2.1.0-beta3.tar.gz
```

y se descomprime:

```
# tar -xzf LuaJIT-2.1.0-beta3.tar.gz
```

Una vez descomprimido, se abre el directorio y se compila e instala LuaJIT:

```
# cd LuaJIT-2.1.0-beta3/
# make && sudo make install
# sudo ln -sf luajit-2.1.0-beta3 /usr/local/bin/luajit
```

LuaJIT se instalará bajo el directorio del sistema `/usr/local`.

Una vez instalado LuaJIT, se instala Lua, necesario para resolver dependencias en caso de instalar módulos externos de Lua.

Para ello, primero se descarga la distribución compilable de Lua y se descomprime:

```
# curl -R -O http://www.lua.org/ftp/lua-5.1.5.tar.gz
# tar -zxf lua-5.1.5.tar.gz
```

Tras descomprimirlo, se accede al directorio y se compila e instala Lua, el cual se ubicará también bajo el directorio del sistema `/usr/local`.

```
# cd lua-5.1.5/
# make linux test
# sudo make install
```

Con esto quedan instalados LuaJIT y Lua.

## 3. Instalación de LuaSocket

Para instalar LuaSocket, primero se obtiene una copia de su repositorio oficial, desde el cual se compilará e instalará:

```
# git clone https://github.com/diegonehab/luasocket
```

Una vez clonado el repositorio, se accede al directorio y se modifica el fichero `src/makefile` para añadir la opción de compilación que permitirá el uso de LuaSocket a través de LuaJIT:

```
# cd luasocket/
# cd src/
# nano makefile
```

Se buscan las líneas:

```
# MYLDFLAGS: to be set by user if needed
MYLDFLAGS?=
```

y se editan tal que queden de la siguiente manera:

```
# MYLDFLAGS: to be set by user if needed
MYLDFLAGS?=-lluajit-5.1 -ldl -lm
```

Guardamos y cerramos la edición. Se vuelve al directorio de LuaSocket y se realiza la compilación e instalación. LuaSocket se instalará también bajo el directorio del sistema `/usr/local`:

```
# cd ../
# make
# sudo make install
```

Una vez instalado, se realiza una última acción, necesaria para que LuaJIT pueda encontrar la instalación de LuaSocket:

```
# export LD_LIBRARY_PATH=/usr/local/lib:$LD_LIBRARY_PATH
# ldconfig
```

Si queremos que esta acción sea persistente, editar el fichero `/etc/environment`, añadir la siguiente línea:

```
LD_LIBRARY_PATH="/usr/local/lib"
```

y reiniciar el sistema.

## 4. Instalación de Apache Traffic Server

La instalación de ATS se realiza mediante su repositorio de GitHub oficial. A continuación, se describen los pasos a seguir.

En primer lugar, se instalan las dependencias necesarias:

```
# apt install autoconf automake libtool pkg-config libmodule-install-perl gcc zlib1g-dev libssl-dev libpcre3-dev libcap-dev libhwloc-dev libncurses5-dev libcurl4-openssl-dev flex
```

Una vez instaladas, se obtiene una copia del repositorio oficial y se accede al directorio:

```
# git clone https://github.com/apache/trafficserver
# cd trafficserver/
```

Después, se genera el script de configuración:

```
# autoreconf -if
```

y se configura el árbol de código fuente:

```
# ./configure --prefix=/opt/ts --with-luajit=/usr/local
```

Se especifica que se instale ATS bajo el directorio `/opt/ts` con soporte para LuaJIT, el cual está instalado bajo `/usr/local`.

Una vez configurado el árbol fuente, se compila ATS y se comprueba la compilación (esta parte tardará varios minutos):

```
# make
# make check
```

Cuando finalice la compilación y las comprobaciones, se instala ATS y se hace limpieza (para liberar espacio):

```
# sudo make install
# make clean
```

Opcionalmente, se pueden realizar las pruebas de regresión:

```
# cd /opt/ts
# bin/traffic_server -R 1
```

Con esto, queda instalado ATS con todas sus dependencias necesarias.
