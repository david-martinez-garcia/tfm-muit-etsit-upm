# Compilación y gestión del ciclo de vida de las aplicaciones
En este documento se describen las instrucciones para compilar y gestionar el ciclo de vida de las aplicaciones de ONOS desarrolladas (instalación, activación, desactivación y desinstalación).

## 1. Compilación de la aplicación
Una vez completada la edición del código fuente de la aplicación, en el directorio raíz de la misma (debería ser `/home/usuario/onos-apps/<nombre_de_app>`) invocar el siguiente comando de Apache Maven, que realiza la compilación de la aplicación:

```
$ mvn clean install
```

El comando comprobará que no existen errores en el código, ejecutará pruebas y compilará la aplicación, generando ficheros `.jar` y `.oar`, siendo este último el que se usará para instalar la aplicación en ONOS. Si no se quieren ejecutar pruebas de la aplicación desarrollada, invocar la compilación especificando la opción `-DskipTests`:

```
$ mvn clean install -DskipTests
```

## 2. Gestión del ciclo de vida de las aplicaciones
El ciclo de vida de las aplicaciones consiste en las siguientes fases:
- Instalación.
- Activación.
- Desactivación.
- Desinstalación.

Para que una aplicación pueda activarse necesita estar instalada. La activación implica su ejecución de manera automática, y la desactivación su finalización.

### 2.1. Instalación
Para instalar una aplicación, ejecutar, desde el directorio raíz de la aplicación desarrollada, el siguiente comando:

```
# /opt/onos/bin/onos-app localhost install target/giros-onos-app-1.0-SNAPSHOT.oar
```

- El parámetro `localhost` indica en qué controlador instalar la aplicación. En este caso, hace referencia al controlador ONOS ejecutándose en la máquina local.
- El parámetro `install` indica que se quiere instalar la aplicación. Puede sustituirse por `reinstall` si ya existía una versión anterior de la misma aplicación. A `install` y `reinstall` puede añadirse el símbolo `!` al final para indicar que se quiere activar la aplicación inmediatamente tras la instalación (`install!` o `reinstall!`).
- `target/giros-onos-app-1.0-SNAPSHOT.oar` especifica la ruta al fichero `.oar` compilado anteriormente (paquete de la aplicación compilada que se quiere instalar).

Tras la ejecución del comando se mostrará información en el terminal con los detalles de la aplicación que se ha instalado.

La aplicación también puede instalarse desde la GUI, en el menú correspondiente para la gestión de las aplicaciones, haciendo click en el botón de añadir (`+`) y seleccionando el fichero `.oar` desde la ventana emergente.

### 2.2. Activación
La activación de una aplicación puede realizarse de tres formas:

1. Desde el mismo comando de instalación, especificando el símbolo `!` como ya se ha visto.
2. Accediendo a la CLI y ejecutando el comando `app activate <fqdn_de_la_app>`.
3. Accediendo a la GUI, entrando al menú de gestión de aplicaciones, seleccionar la aplicación instalada y haciendo click en el botón `Play`.

La activación de una aplicación implica su ejecución inmediata. A nivel interno, la aplicación invocará el método `activate()`.

### 2.3. Desactivación
La desactivación de una aplicación puede realizarse de dos maneras:

1. Desde la CLI, ejecutando el comando `app deactivate <fqdn_de_la_app>`.
2. Desde la GUI, entrando al menú de gestión de aplicaciones, seleccionar la aplicación deseada y haciendo click en el botón `Stop`.

Al desactivar una aplicación se detiene su ejecución de manera inmediata. A nivel interno, la aplicación invocará el método `deactivate()`.

### 2.4. Desinstalación
Para desinstalar una aplicación, acceder a la GUI y, en el menú de gestión de aplicaciones, seleccionar la aplicación deseada y hacer click en el botón de la papelera.
