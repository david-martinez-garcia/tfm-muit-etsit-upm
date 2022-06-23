# Generación de arquetipos para el desarrollo de aplicaciones
En este documento se describen los procedimientos para la generación de arquetipos para el desarrollo de aplicaciones de ONOS.

El concepto de arquetipo es equivalente al de una plantilla, que proporciona una base sobre la que desarrollar aplicaciones. La generación de arquetipos hace uso de Apache Maven y de la instalación de ONOS en versión de desarrollo, por lo que para poder generar los arquetipos previamente hay que disponer de un entorno con estas utilidades disponibles.

El desarrollo de aplicaciones de ONOS utiliza el lenguaje de programación Java, por lo que se debe disponer, además, de un IDE (_Integrated Development Environment_ o Entorno de Desarrollo Integrado) o editor de código con soporte para este lenguaje.

Para el desarrollo de aplicaciones de ONOS se parte siempre de un arquetipo base, denominado `app`, que representa la aplicación de ONOS en sí misma. Sobre este arquetipo base se pueden implementar extensiones o capas superiores, denominadas _overlays_, que añaden funcionalidad. Estas son:
- `cli`, que permite invocar comandos de la aplicación en la CLI de ONOS.
- `rest`, que permite exponer una API REST de la aplicación de ONOS para que pueda ser utilizada por terceras aplicaciones. Esta API extiende la definida propiamente por ONOS, e incorporará la funcionalidad adicional deseada por el programador.
- `ui`, que permite desarrollar elementos HTML para interactuar con la aplicación desde la GUI Web de ONOS.

Para esta parte se recomienda crear un directorio en el directorio `home` del usuario, que puede llamarse `onos-apps`. Este directorio permitirá tener organizadas las apps que se desarrollen.

```
$ cd
$ mkdir onos-apps
$ cd onos-apps/
```

A continuación, se describen los pasos a seguir para generar los distintos arquetipos. Para ello, se supone que:
- Se tiene ONOS en versión de desarrollo compilado e instalado en el directorio `home` del usuario: `/home/usuario/onos`.
- Se ha creado el directorio anteriormente mencionado, y que el directorio de trabajo del terminal de comandos está establecido en él.

## 1. Arquetipo de aplicación base
Para generar el arquetipo de aplicación base se ejecuta el siguiente comando:

```
$ ./home/usuario/onos/tools/dev/bin/onos-create-app app es.upm.dit.giros giros-onos-app 1.0-SNAPSHOT es.upm.dit.giros.onos.app
```

donde:
- `usuario` es el nombre de usuario en el sistema.
- `app` indica el tipo de arquetipo desde el que se generará la plantilla de aplicación: en este caso, se usa el arquetipo de aplicación base.
- `es.upm.dit.giros` indica el FQDN (_Fully Qualified Domain Name_ o Nombre de Dominio Completo) inverso de la organización que desarrolla la aplicación.
- `giros-onos-app` indica el nombre de la aplicación a desarrollar. El comando generará un directorio con los ficheros fuente de la plantilla con ese mismo nombre.
- `1.0-SNAPSHOT` indica la versión de la aplicación.
- `es.upm.dit.giros.onos.app` es el FQDN inverso de la aplicación (el FQDN inverso de la organización más una extensión con el nombre que quiera darse para la aplicación).

Tras ejecutar el comando se preguntará al usuario una confirmación en el terminal. Pulsar `Y` y confirmar. Se generará un nuevo directorio con el código fuente de la plantilla de la aplicación base.

## 2. Arquetipo para _overlay_ de CLI
Para el caso de arquetipo que añade la funcionalidad de ejecutar comandos de la aplicación en la CLI de ONOS se ejecuta el siguiente comando, muy similar:

```
$ ./home/usuario/onos/tools/dev/bin/onos-create-app cli es.upm.dit.giros giros-onos-app 1.0-SNAPSHOT es.upm.dit.giros.onos.app
```

Únicamente cambia el parámetro `app` por `cli`. Si se especifica el resto de parámetros con los mismos valores, el comando añadirá nuevos ficheros de código fuente plantilla para implementar la funcionalidad en el directorio anteriormente creado.

El _overlay_ que define el arquetipo `cli` por defecto añade el comando de CLI `sample`, que imprime por ésta la cadena `Hello World`.

## 3. Arquetipo para _overlay_ de API REST
Este arquetipo permite definir una API REST a exponer para interactuar con la aplicación a través de ONOS. El comando es muy similar:

```
$ ./home/usuario/onos/tools/dev/bin/onos-create-app rest es.upm.dit.giros giros-onos-app 1.0-SNAPSHOT es.upm.dit.giros.onos.app
```

Únicamente cambia `app` o `cli` por `rest`. Al igual que con `cli`, si se especifican los mismos valores para el resto de parámetros, el comando añade nuevos ficheros de código fuente plantilla para implementar la funcionalidad.

## 4. Arquetipo para _overlay_ de interfaz Web
Este arquetipo añade elementos Web a la GUI de ONOS para poder interactuar con la aplicación. Este _overlay_ puede especificarse con tres tipos distintos:
- `ui`: vista personalizada en la interfaz Web (seleccionable desde el menú).
- `uitab`: vista tabular.
- `uitopo`: vista de topología.

El comando es muy similar a los anteriores:

```
$ ./home/usuario/onos/tools/dev/bin/onos-create-app {ui|uitab|uitopo} es.upm.dit.giros giros-onos-app 1.0-SNAPSHOT es.upm.dit.giros.onos.app
```

Nótese que hay que elegir uno de los tres en cada invocación del comando.

Tras la generación de las plantillas, el siguiente paso es escribir el código de la aplicación. Una vez escrito, se realiza la compilación, instalación, activación, desactivación y desinstalación (gestión del ciclo de vida de las aplicaciones).
