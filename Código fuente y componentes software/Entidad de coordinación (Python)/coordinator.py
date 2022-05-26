# coordinator.py
# Versión 3.
# Coordinador de la red.
# Aplicación Flask de Python.
# El Coordinador expone una API REST mediante la cual el Orquestador (aplicación de ONOS) puede preguntar qué caché
# servirá el contenido solicitado por un cliente.
# La API REST también permite al Orquestador comprobar si un contenido, identificado mediante una dirección IP, se sirve en la red.

# SELECCIÓN DE CACHÉ:
# La selección de la caché puede realizarse de diferentes maneras, especificadas mediante un modo de funcionamiento.
# La especificación del modo de funcionamiento se hace a través de la definición de la variable de entorno
# COORDINATOR_MODE. Su definición debe hacerse de la siguiente manera (en un terminal de comandos):
# $ export COORDINATOR_MODE = <modo de funcionamiento>.
# La variable de entorno debe definirse antes de ejecutar la aplicación Flask. Puede cambiarse su valor durante la ejecución,
# pero debe reiniciarse la ejecución para que el cambio surta efecto.
# El modo de funcionamiento puede ser:
# "random" -> la caché se selecciona aleatoriamente.
# "location" -> la caché se selecciona teniendo en cuenta la localización del cliente (nodo MEC) que pide los contenidos.
# "vCache1", "vCache2" o "vCache3" -> la caché se selecciona de manera fija, especificando su hostname en la invocación.
# Las cachés se almacenan en un fichero JSON.
# También se almacenan los clientes en la red mediante otro fichero JSON.

# COMPROBACIÓN DE CONTENIDO SERVIDO POR LA RED:
# La comprobación de si un contenido se sirve en la red se realiza utilizando consultas PTR de DNS, que devuelven nombres de dominio
# a partir de direcciones IP, y que identifican servicios.

# REFERENCIAS DE AYUDA:
# [1]: https://www.hostinger.com/tutorials/what-is-a-ptr-record-and-how-to-do-reverse-ip-lookup
# [2]: https://dnspython.readthedocs.io/en/latest/resolver-functions.html
# [3]: https://github.com/rthalley/dnspython/blob/master/examples/reverse_name.py
# [4]: https://stackoverflow.com/questions/19867548/python-reverse-dns-lookup-in-a-shared-hosting
# [5]: https://docs.python.org/3/howto/logging.html
# [6]: https://stackoverflow.com/questions/56529391/setting-and-retrieving-environmental-variables-in-flask-applications

# Importación de librerías requeridas.
from flask import Flask, request
import os, json, random, logging
import dns.reversename, dns.resolver

# El modo de funcionamiento de la app se almacena en la variable "mode".
# Se lee su valor usando el módulo os.environ de Python.
mode = os.environ.get("COORDINATOR_MODE")

# Se inicializa la aplicación de Flask.
coordinator = Flask(__name__)

# Se inicializa y configura el objeto de logging (registro).
logger = logging.getLogger("COORDINATOR")
logger.setLevel(logging.DEBUG)

# Se crea y configura el manejador de logging en fichero.
fh = logging.FileHandler("/root/coordinator.log")
fh.setLevel(logging.DEBUG)

# Se crea y configura el formateador para los mensajes de log.
# Los mensajes de log tendrán el formato: FECHA Y HORA - COORDINATOR - NIVEL DE LOG - MENSAJE.
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# Se añade el formateador al manejador de logging en fichero.
fh.setFormatter(formatter)

# Se añade el manejador de fichero al objeto de logging.
logger.addHandler(fh)

# Lee el fichero con el registro de cachés disponibles (formato JSON).
# Devuelve una cadena de caracteres con el objeto/contenido JSON del fichero, para su posterior procesado.
def read_caches_json():
    file = open("/root/caches.json", "r")
    caches = json.load(file)
    return caches

# Lee el fichero con el registro de clientes en la red (formato JSON).
# Devuelve una cadena de caracteres con el objeto/contenido JSON del fichero, para su posterior procesado.
def read_clients_json():
    file = open("/root/clients.json", "r")
    clients = json.load(file)
    return clients

# Realiza una consulta PTR al servidor de DNS que esté configurado en el sistema
# dada la dirección IP (cadena de caracteres) que se pasa como parámetro.
# Devuelve el nombre de dominio/nombre de host asociado a esa dirección IP.
def query_dns_ptr(ip_addr):
    result = dns.reversename.from_address(ip_addr)
    ptr = dns.resolver.resolve(result, "PTR")[0]
    return ptr

# Lee el fichero con el registro de contenidos que sirve la red y comprueba si 
# el registro PTR que se pasa como parámetro se corresponde con el de un contenido que sirve la red.
# El registro PTR debe ser una cadena de caracteres.
# Si se encuentra, se devuelve la cadena "served". En caso contrario, se devuelve la cadena "not_served".
def check_content_served(ptr):
    file = open("/root/contents.txt", "r")
    check = "not_served"
    for line in file:
        if ptr in line:
            check = "served"
            break
    if check == "served":
        logger.info("Found match for "+ptr+" - The content IS served by the network")
    else:
        logger.info("Did not find match for "+ptr+" - The content IS NOT served by the network")
    return check

# Función manejadora de peticiones HTTP GET al endpoint en /coordinator/api/cache.
# Este código selecciona una caché en base al modo de funcionamiento y devuelve sus datos como respuesta en formato JSON.
@coordinator.get("/coordinator/api/cache")
def get_cache():
    # Se obtiene la dirección IP del cliente y el nombre del contenido solicitado por éste.
    client_ip = request.args.get("client")
    content = request.args.get("content")
    logger.info("Received request to select vCache for content "+content+" selected by client at "+client_ip)
    # Se obtienen las cachés del fichero JSON.
    caches = read_caches_json()["caches"]
    # En función del modo de funcionamiento de la aplicación, se selecciona la caché en consecuencia.
    # La caché seleccionada (objeto JSON) se almacena en la variable "cache".
    cache = ""
    # Si el modo de funcionamiento es "random", la caché se selecciona aleatoriamente.
    if mode == "random":
        cache = random.choice(caches)
    # Si el modo de funcionamiento es "location":
    elif mode == "location":
        # Primero, se obtienen los clientes del fichero JSON.
        clients = read_clients_json()["clients"]
        client = ""
        # Segundo, se lee cliente a cliente:
        for item in clients:
            # Si la dirección IP del cliente que hizo la petición original coincide con la del cliente actualmente procesándose,
            # se guarda el objeto JSON en la variable "client", y se sale del bucle.
            if item["ip_addr"] == client_ip:
                client = item
                break
        # Tercero, se lee caché a caché:
        for item in caches:
            # Si la localización MEC de la caché coincide con la del cliente almacenado, se guarda el objeto JSON en la variable "cache",
            # y se sale del bucle.
            if item["location"] == client["location"]:
                cache = item
                break
    # En caso contrario, si el modo del funcionamiento es el nombre de la caché fija a seleccionar, se selecciona dicha caché
    # a partir del nombre.
    else:
        for item in caches:
            if item["hostname"] == mode:
                cache = item
                break
    
    logger.info("Request for content "+content+" selected by client at "+client_ip+" will be served by "+cache["hostname"]+" at "+cache["ip_addr"])
    return cache

# Función manejadora de peticiones HTTP GET al endpoint en /coordinator/api/is_content_served.
# Este código permite comprobar si el contenido asociado a la dirección IP enviada como parámetro se sirve en la red.
@coordinator.get("/coordinator/api/is_content_served")
def get_if_content_is_served():
    # Se obtiene la dirección IP a partir del parámetro "ip_addr" de la solicitud.
    ip_addr = request.args.get("ip_addr")
    logger.info("Received request to check if IP address "+ip_addr+" matches for a content the network serves")
    # Se obtiene el registro PTR asociado a la dirección IP.
    ptr = query_dns_ptr(ip_addr)
    # ptr no es una cadena de caracteres, hay que transformarla con str(ptr).
    ptr = str(ptr)
    logger.info("PTR query for "+ip_addr+" resolves "+ptr)
    # Se comprueba si el registro PTR coincide para un contenido que sirve la red, y se devuelve el resultado.
    check = check_content_served(ptr)
    return check
