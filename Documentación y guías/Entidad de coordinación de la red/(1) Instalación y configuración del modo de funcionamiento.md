# Entidad de coordinación de la red - Instalación de dependencias y configuración del modo de funcionamiento

## 1. Instalación de Python con sus dependencias

**La instalación se realiza en el _rootfs_ de VNX correspondiente (Ubuntu 18.04 LTS de 64 bits)**.

**NOTA:** Todos los comandos siguientes se ejecutan como usuario `root`.

### 1.1. Preparación del entorno

En primer lugar, actualizar el sistema:

```
# apt update && apt dist-upgrade && apt autoremove && apt autoclean
```

Una vez completada la ejecución, proceder con los siguientes pasos para la instalación del software necesario.

### 1.2. Instalación de Python, Flask y otras dependencias

En primer lugar, instalar Python3 y otras dependencias:

```
# apt install python3 subversion git wget curl nano
```

En segundo lugar, instalar PIP. PIP es una herramienta para instalar paquetes y dependencias de Python:

```
# apt install python3-pip
```

Después, actualizar PIP:

```
# python3 -m pip install --upgrade pip
```

Una vez que PIP está actualizado, instalar los siguientes paquetes (PIP instalará otras dependencias requeridas por estos):

```
# python3 -m pip install flask requests requests-toolbelt cryptography idna dnspython
```

Con esto quedaría completada la instalación.

## 2. Configuración del modo de funcionamiento

### 2.1. Descripción de los modos de funcionamiento

La entidad de coordinación puede funcionar, para el mecanismo de selección de caché, de los siguientes modos:

- `random`: La caché se selecciona aleatoriamente de entre las desplegadas en el escenario de red.
- `location`: La caché se selecciona en función de la localización (nodo MEC) del cliente que inició la petición para acceder a un contenido. En este modo de funcionamiento, siempre se selecciona la caché conectada al mismo nodo MEC que el cliente.
- `vCache1`: Siempre se selecciona la caché `vCache1`.
- `vCache2`: Siempre se selecciona la caché `vCache2`.
- `vCache3`: Siempre se selecciona la caché `vCache3`.

### 2.2. Configuración del modo de funcionamiento

La configuración del modo de funcionamiento debe realizarse, por lo general, antes de ejecutar la entidad de coordinación. Esta configuración se realiza mediante una variable de entorno, llamada `COORDINATOR_MODE`, cuyo valor puede ser cualquiera de los indicados en el apartado anterior. Para configurar esta variable de entorno, en una terminal de comandos, ejecutar:

```
# export COORDINATOR_MODE=<valor>
```

Si la entidad de coordinación ya estaba ejecutándose antes del cambio, es necesario reiniciarla.
