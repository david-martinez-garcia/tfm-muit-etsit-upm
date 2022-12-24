# Instalación de ONOS en versión de producción y en versión de desarrollo

## 1. Instalación de ONOS en versión de producción

En este Apartado se describe el procedimiento para instalar el controlador ONOS en su versión de producción.

**La instalación se realiza en el _rootfs_ de VNX correspondiente (Ubuntu 18.04 LTS de 64 bits)**.

**IMPORTANTE:** Tal y como se indica en la memoria, si la instalación de ONOS en versión de producción se realiza modificando el _rootfs_ de VNX, ésta queda vinculada a la dirección IP que se asigne al _rootfs_ durante su modificación, por lo que no funcionaría al realizar el despliegue de los escenarios de VNX. Así, en este documento se proporcionan las instrucciones de instalación de manera general.

**NOTA:** Todos los comandos siguientes se ejecutan como usuario `root`.

### 1.1. Preparación del entorno

Según la documentación oficial de ONOS, se tienen que cumplir los siguientes [requisitos mínimos](https://wiki.onosproject.org/display/ONOS/Requirements):

- CPU de 2 núcleos.
- Memoria RAM de 2 GB.
- 10 GB de almacenamiento.
- Una tarjeta de red.

Además, la máquina en la que se ejecute ONOS tiene que tener los siguientes puertos accesibles:

- 8181 para acceder a la API REST y a la interfaz web.
- 8101 para el acceso a la interfaz de comandos.
- 9876 para la comunicación entre máquinas del clúster de ONOS.
- 6653, opcional, para controlar equipamiento de red mediante el protocolo OpenFlow.
- 6640, opcional, para OVSDB.

En primer lugar, se actualiza el sistema con los últimos paquetes disponibles. Para ello, ejecutar:

```
# apt update && apt dist-upgrade && apt autoremove && apt autoclean
```

**Se instalará la versión 2.7.0 _X-Wing_ (LTS)**.

#### Usuarios

Se recomienda la ejecución de ONOS bajo un usuario sin privilegios (no debería ejecutarse como `root`). Para ello, y especialmente si se va a ejecutar ONOS como servicio del sistema, hay que definir un usuario para la ejecución. El siguiente comando realiza esta acción, creando el usuario `sdn`:

```
# adduser sdn --system --group
```

#### Dependencias

Se requieren las siguientes dependencias adicionales:

- JDK de Java 11 (OpenJDK).
- `wget`.
- `curl`.
- `sshpass`, utilizada para realizar una conexión SSH especificando en el mismo comando la contraseña. Útil si se utilizan escenarios de prueba automatizados y se quiere evitar que la terminal de comandos se quede en espera de la introducción de la contraseña.

Primero, se instala OpenJDK 11 y otras dependencias adicionales:

```
# apt install openjdk-11-jdk subversion git nano
```

Una vez instalado, se recomienda configurar las variables `JAVA_HOME` y `JRE_HOME`:

```
# nano /etc/environment
```

Y añadir las siguientes líneas al fichero:

`JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"`

`JRE_HOME="/usr/lib/jvm/java-11-openjdk-amd64/jre"`

`ONOS_POM_VERSION="2.7.0"`

Modificar, también, la variable `PATH` y añadir al final de ésta lo siguiente:

`...(otras definiciones)...:/usr/lib/jvm/java-11-openjdk-amd64"`

Finalmente, instalar `wget`, `curl` y `sshpass` y reiniciar la sesión del usuario:

```
# apt install wget curl sshpass
```

### 1.2. Descarga e instalación de ONOS

Para realizar la instalación, se descargará un fichero `.tar.gz` con ONOS 2.7.0 precompilado. Las dependencias internas de ONOS y paquetes por defecto requieren que se instale en el directorio `/opt` del sistema. Para ello, se crea el directorio si no está ya creado, y se establece ese directorio como el de trabajo en la sesión de consola:

```
# mkdir /opt

# cd /opt
```

Descargar ONOS desde el repositorio oficial:

```
# wget https://repo1.maven.org/maven2/org/onosproject/onos-releases/2.7.0/onos-2.7.0.tar.gz
```

Descomprimir el fichero descargado:

```
# tar xfz onos-2.7.0.tar.gz
# mv onos-2.7.0/ onos/
# chmod 777 -R /opt/onos
```

Con esto queda ONOS en versión de producción instalado.

## 2. Instalación de ONOS en versión de desarrollo

En este Apartado se describen las instrucciones para instalar el controlador ONOS en su versión de desarrollo, de manera que proporcione las herramientas necesarias para programar aplicaciones. Para este propósito se usará la herramienta `onos-create-app`.

**La instalación se realiza en el sistema anfitrión (Ubuntu 18.04 LTS de 64 bits)**.

### 2.1. Preparación del entorno. Dependencias y requisitos necesarios

Para instalar ONOS en versión de producción se necesitan las siguientes dependencias:
- [Apache Maven](https://maven.apache.org/install.html). Requerida para compilar aplicaciones de ONOS.
- [Google Bazel](https://bazel.build/install/ubuntu). Requerida para compilar ONOS.

Las instrucciones aquí definidas se han obtenido de los enlaces anteriores.

Adicionalmente, se necesitan también `subversion`, `git`, `zip`, `wget`, `curl`, `unzip`, `nano`, `python2.7`, `python3`, `openjdk-11-jdk` y `g++`.

**NOTA: Por cada vez que se edite una variable de entorno es necesario reiniciar la sesión del usuario para que los cambios surtan efecto, o bien ejecutar el comando `ldconfig`**.

Todas estas dependencias se utlizarán para compilar, hacer pruebas e instalar las aplicaciones de ONOS, así como para poder compilar la versión de desarrollo de ONOS en el sistema.

### 2.2. Instalación de las dependencias necesarias
En primer lugar, comprobar que el sistema se encuentra actualizado:

```
$ sudo apt update && sudo apt dist-upgrade && sudo apt autoremove && sudo apt autoclean
```

Después, instalar las dependencias iniciales:

```
$ sudo apt install subversion git zip wget curl unzip nano python2.7 python3 openjdk-11-jdk g++
```

Tras la instalación de OpenJDK, se recomienda configurar las variables `JAVA_HOME` y `JRE_HOME`. Para ello, ejecutar:

```
$ sudo nano /etc/environment
```

Y añadir las siguientes líneas al fichero:

`JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"`

`JRE_HOME="/usr/lib/jvm/java-11-openjdk-amd64/jre"`

Modificar, también, la variable `PATH` y añadir al final de ésta lo siguiente:

`...(otras definiciones)...:/usr/lib/jvm/java-11-openjdk-amd64"`

Guardar, cerrar el fichero y reiniciar la sesión de usuario. Una vez completadas estas acciones, se pasa a instalar el resto de dependencias.

#### 2.2.1. Instalación de Apache Maven

**A 24/12/2022 la versión de Maven es la 3.8.6, que es la que aparece reflejada en este documento**.

**Maven se utiliza, actualmente, para compilar las aplicaciones de ONOS**.

Ejecutar los siguientes comandos como `root`.

Primero, se descarga Maven de las fuentes oficiales:

```
# cd /opt

# wget https://dlcdn.apache.org/maven/maven-3/3.8.6/binaries/apache-maven-3.8.6-bin.tar.gz
```

Después, se desempaqueta:

```
# tar xzf apache-maven-3.8.6-bin.tar.gz
```

El directorio de Maven será, por tanto, `/opt/apache-maven-3.8.6/`.

Finalmente, editar el fichero `/etc/environment` y añadir a la variable `PATH` el directorio `/bin` de Apache Maven. Debe quedar de la siguiente manera:

`...(otras definiciones)...:/opt/apache-maven-3.8.6/bin"`.

Con esto queda instalado Apache Maven.

#### 2.2.2. Instalación de Google Bazel

**Bazel se utiliza, actualmente, para compilar ONOS desde su repositorio**.

La mejor manera de obtener Bazel y mantenerlo actualizado es utilizar [Bazelisk](https://github.com/bazelbuild/bazelisk).

Para instalar Bazelisk en el sistema, descargar la última versión desde la [página de _Releases_ que está disponible en el repositorio oficial](https://github.com/bazelbuild/bazelisk/releases). Para sistemas operativos Ubuntu Linux de 64 bits, descargar la variante `bazelisk-linux-amd64`. Una vez descargada, renombrar el archivo a `bazel` y moverlo al directorio `/usr/local/bin`:

```
$ sudo mv bazelisk-linux-amd64 /usr/local/bin/bazel
$ sudo chmod +x /usr/local/bin/bazel
```

Con esto queda instalado Bazelisk. La próxima vez que se ejecute el comando `bazel`, se descargará la versión adecuada de Bazel en función de los requisitos del repositorio oficial de ONOS.

### 2.3. Compilación e instalación de ONOS en versión de desarrollo

En primer lugar, clonar el repositorio de GitHub oficial:

```
$ git clone https://gerrit.onosproject.org/onos
```

Después, compilar ONOS:

```
$ cd onos

$ bazel build onos
```

Con esto queda compilado e instalado ONOS en su versión de desarrollo. Se utilizará la herramienta que proporciona, `onos-create-app` para crear los arquetipos de las aplicaciones y poder desarrollar nuevas aplicaciones. Se utilizará ONOS en su versión de producción para el resto de casos.

La herramienta para crear arquetipos de aplicaciones de ONOS se encuentra en la ruta del sistema `/onos/tools/dev/bin/onos-create-app`.
