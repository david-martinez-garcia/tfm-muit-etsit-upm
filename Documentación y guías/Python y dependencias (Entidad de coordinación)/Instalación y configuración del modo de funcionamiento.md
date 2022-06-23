# Instalación de Python con sus dependencias para implementar la entidad de coordinación de la red

**NOTA:** Ejecutar todos los comandos siguientes como `root`.

## 1. Preparación del entorno

En primer lugar, actualizar el sistema:

```
# apt update && apt dist-upgrade && apt autoremove && apt autoclean
```

Una vez completada la ejecución, proceder con los siguientes pasos para la instalación del software necesario.

## 2. Instalación de Python y Flask

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
