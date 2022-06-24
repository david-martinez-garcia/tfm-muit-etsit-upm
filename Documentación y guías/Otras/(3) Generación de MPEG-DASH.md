# Generación de MPEG-DASH

En este documento se presentan las instrucciones seguidas para generar un _streaming_ MPEG-DASH de ejemplo. Las instrucciones se presentan para el sistema operativo
Ubuntu Linux 18.04 LTS.

**Las instrucciones se ejecutan en el sistema anfitrión (Ubuntu 18.04 LTS de 64 bits)**.

## 1. Descarga e instalación de los ficheros y dependencias necesarias.

**NOTA: Ejecutar todos los comandos en el mismo directorio.**

Se ha partido del vídeo _Big Buck Bunny_, en resolución 3840x2160 (16:9) a 60 fps, que puede obtenerse desde el siguiente enlace: https://download.blender.org/demo/movies/BBB/bbb_sunflower_2160p_60fps_normal.mp4

Para generar el _streaming_ es necesario disponer de las siguientes dependencias, instalables mediante línea de comandos: `unzip`, `ffmpeg`, `ubuntu-restricted-extras` y `python3.8`.

También es necesario descargar e instalar (o desempaquetar):

- GPAC, que puede obtenerse desde el siguiente enlace: https://download.tsi.telecom-paristech.fr/gpac/release/2.0/gpac_2.0-rev0-g418db414-master_amd64.deb
- _Bento4_, que puede descargarse desde el siguiente enlace: https://www.bok.net/Bento4/binaries/Bento4-SDK-1-6-0-639.x86_64-unknown-linux.zip

Primero, se descarga el vídeo _Big Buck Bunny_ original y se renombra:

```
$ wget https://download.blender.org/demo/movies/BBB/bbb_sunflower_2160p_60fps_normal.mp4
$ mv bbb_sunflower_2160p_60fps_normal.mp4 bbb_original.mp4
```

Después, descargar e instalar las dependencias necesarias mediante línea de comandos:

```
$ sudo apt update && sudo apt dist-upgrade && sudo apt autoremove && sudo apt autoclean
$ sudo apt install unzip ffmpeg ubuntu-restricted-extras python3.8
```

Una vez instaladas las dependencias, se descarga e instala GPAC:

```
$ wget https://download.tsi.telecom-paristech.fr/gpac/release/2.0/gpac_2.0-rev0-g418db414-master_amd64.deb
$ sudo dpkg -i gpac_2.0-rev0-g418db414-master_amd64.deb
$ sudo apt install --fix-missing
```

Y, posteriormente, se descarga y descomprime el _framework_ Bento4:

```
$ wget https://www.bok.net/Bento4/binaries/Bento4-SDK-1-6-0-639.x86_64-unknown-linux.zip
$ unzip Bento4-SDK-1-6-0-639.x86_64-unknown-linux.zip
$ mv Bento4-SDK-1-6-0-639.x86_64-unknown-linux bento4
```

Con esto, quedan instaladas todas las dependencias necesarias.

## 2. Generación de las representaciones

Se generan 5 representaciones con fragmentos de 2 segundos de duración:

- Resolución: 160x90 (16:9) a 60 fps, con 100 kb/s de _bitrate_.
- Resolución: 640x360 (16:9) a 60 fps, con 600 kb/s de _bitrate_.
- Resolución: 1280x720 (16:9) a 60 fps, con 2400 kb/s de _bitrate_.
- Resolución: 1920x1080 (16:9) a 60 fps, con 4800 kb/s de _bitrate_.
- Resolución: 3840x2160 (16:9) a 60 fps, con 8002 kb/s de _bitrate_.

Para generar las distintas representaciones, se ejecutan los siguientes comandos:

```
$ ffmpeg -i bbb_original.mp4 -c:v libx264 -b:v 100000 -r 60 -level 3.0 -profile:v baseline -x264opts "keyint=120:min-keyint=120:no-scenecut" -vf scale=160:90 bbb_1.mp4
$ ffmpeg -i bbb_original.mp4 -c:v libx264 -b:v 600000 -r 60 -level 3.0 -profile:v baseline -x264opts "keyint=120:min-keyint=120:no-scenecut" -vf scale=640:360 bbb_2.mp4
$ ffmpeg -i bbb_original.mp4 -c:v libx264 -b:v 2400000 -r 60 -level 3.0 -profile:v baseline -x264opts "keyint=120:min-keyint=120:no-scenecut" -vf scale=1280:720 bbb_3.mp4
$ ffmpeg -i bbb_original.mp4 -c:v libx264 -b:v 4800000 -r 60 -level 3.0 -profile:v baseline -x264opts "keyint=120:min-keyint=120:no-scenecut" -vf scale=1920:1080 bbb_4.mp4
$ ffmpeg -i bbb_original.mp4 -c:v libx264 -b:v 8002000 -r 60 -level 3.0 -profile:v baseline -x264opts "keyint=120:min-keyint=120:no-scenecut" -vf scale=3840:2160 bbb_5.mp4
```

Como resultado, se obtienen 5 ficheros, uno por representación: `bbb_1.mp4`, `bbb_2.mp4`, `bbb_3.mp4`, `bbb_4.mp4` y `bbb_5.mp4`.

## 3. Segmentación de las representaciones

Con las diferentes representaciones generadas, se segmentan mediante la ejecución de los siguientes comandos:

```
$ cd bento4/bin
$ ./mp4fragment ../../bbb_1.mp4 ../../bbb_1_fragmented.mp4
$ ./mp4fragment ../../bbb_2.mp4 ../../bbb_2_fragmented.mp4
$ ./mp4fragment ../../bbb_3.mp4 ../../bbb_3_fragmented.mp4
$ ./mp4fragment ../../bbb_4.mp4 ../../bbb_4_fragmented.mp4
$ ./mp4fragment ../../bbb_5.mp4 ../../bbb_5_fragmented.mp4
```

## 4. Generación del _streaming_ MPEG-DASH

Finalmente, ejecutar el siguiente comando, que generará el _streaming_ MPEG-DASH a partir de las representaciones segmentadas:

```
$ ./mp4dash ../../bbb_1_fragmented.mp4 ../../bbb_2_fragmented.mp4 ../../bbb_3_fragmented.mp4 ../../bbb_4_fragmented.mp4 ../../bbb_5_fragmented.mp4
```

El resultado se almacenará en el directorio `output`.
