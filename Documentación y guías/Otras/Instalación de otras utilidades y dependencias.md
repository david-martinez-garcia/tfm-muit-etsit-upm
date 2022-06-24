# Instalación de otras utilidades y dependencias

En este documento se presentan los comandos para instalar otras herramientas necesarias y de utilidad descritas en el **Apartado 4.3.5** de la memoria.

## Instalación en _rootfs_ de VNX

```
# apt update && apt dist-upgrade && apt autoremove && apt autoclean
# apt install subversion git nano wget curl sshpass xauth ffmpeg firefox unbound
```

## Instalación en sistema anfitrión

### Microsoft Visual Studio Code

La descarga e instalación de Microsoft Visual Studio Code puede realizarse mediante el fichero `.deb` proporcionado en el sitio Web oficial: https://code.visualstudio.com/.
Los _plugins_ de soporte a los lenguajes de programación _Java_, _Python_ y _Lua_ se instalan desde la propia herramienta.

### Wireshark

Para instalar Wireshark, ejecutar los siguientes comandos:

```
$ sudo apt update && sudo apt dist-upgrade && sudo apt autoremove && sudo apt autoclean
$ sudo add-apt-repository ppa:wireshark-dev/stable
$ sudo apt update && sudo apt install wireshark
$ sudo adduser $USER wireshark
```

### Herramientas para generación de MPEG-DASH

Consultar el documento [Generación de MPEG-DASH](https://github.com/martinezgarciadavid/tfm-muit-etsit-upm/blob/main/Documentaci%C3%B3n%20y%20gu%C3%ADas/Otras/Generaci%C3%B3n%20de%20MPEG-DASH.md)
para realizar la instalación de las herramientas necesarias.

La descarga del cliente de referencia _dash.js_ en versión 4.3.0 puede realizarse desde el siguiente enlace: https://github.com/Dash-Industry-Forum/dash.js/releases/tag/v4.3.0
