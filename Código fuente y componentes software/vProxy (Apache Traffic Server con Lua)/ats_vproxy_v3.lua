-- Script de Lua para la selección dinámica de caché de contenidos en Apache Traffic Server (vProxy)

-- VERSIÓN 3

-- Este script se ejecuta cuando se aplica una regla de "map" especificada en el fichero "remap.config" de ATS

-- El script llama a la API REST del Orquestador de la red (aplicación de ONOS desarrollada) y obtiene la dirección IP de la caché seleccionada,
-- redirigiendo la petición del cliente a dicha caché. Guarda la caché seleccionada para el contenido solicitado por parte del cliente.
-- El script también comprueba si ya se ha seleccionado anteriormente una caché para el contenido solicitado por el cliente, y en ese caso
-- redirige hacia esa caché. La asignación cliente:contenido:caché se almacena en un fichero de texto.

-- REFERENCIAS:
-- [1]: https://docs.trafficserver.apache.org/en/latest/admin-guide/plugins/lua.en.html
-- [2]: https://wikitech-static.wikimedia.org/wiki/Apache_Traffic_Server#Lua_scripting
-- [3]: https://w3.impa.br/~diego/software/luasocket/http.html
-- [4]: https://stackoverflow.com/questions/18264601/how-to-send-a-correct-authorization-header-for-basic-authentication
-- [5]: https://www.tutorialspoint.com/lua/lua_file_io.htm
-- [6]: https://stackoverflow.com/questions/11201262/how-to-read-data-from-a-file-in-lua
-- [7]: https://www.lua.org/pil/21.1.html
-- [8]: https://www.codegrepper.com/code-examples/whatever/lua+check+if+string+contains
-- [9]: https://stackoverflow.com/questions/25218499/writing-multiline-text-files-in-lua
-- [10]: http://lua-users.org/wiki/StringTrim
-- [11]: https://stackoverflow.com/questions/23690977/patterns-in-lua-with-space

local ORCHESTRATOR = ''
local ONOS_USER = ''
local ONOS_PASS = ''

-- Inicialización del script y recogida de parámetros en la invocación.
function __init__(argtb)
    if (#argtb) < 3 then
        ts.debug(argtb[0] .. ' ORCHESTRATOR (REST API) IP OR DOMAIN NAME, ONOS USERNAME AND ONOS PASSWORD ARE REQUIRED')
        return -1
    end

    -- Se guardan los parámetros: dirección IP del Orquestador; usuario de ONOS y contraseña (para autenticación).
    ORCHESTRATOR = argtb[1]
    ONOS_USER = argtb[2]
    ONOS_PASS = argtb[3]
end

-- Interpreta la URI del contenido solicitado, en forma /uri_part/uri_part/..., y devuelve
-- sólo la primera parte de la URI.
-- Ejemplo: para la URI /uri1/uri2/uri3, devuelve uri1.
function parse_content_uri(content_uri)
    -- Si el contenido pedido no tiene una cadena de caracteres (contenido por defecto), se identifica con un espacio en blanco y se devuelve.
    if content_uri == "/" then return " " end
    if content_uri == "/ " then return " " end

    -- Se sustituyen los caracteres / por espacios en blanco.
    content_uri = content_uri:gsub("/", " ")

    -- Se eliminan los espacios en blanco al principio y al final.
    content_uri = content_uri:gsub("^%s+", ""):gsub("%s+$", "")

    -- Se separa la URI parte a parte y se devuelve la parte de la primera iteración.
    for uri_part in string.gmatch(content_uri, "[0-9a-zA-Z]*") do
        return uri_part
    end
end

-- Escribe en el fichero de log información sobre la ejecución.
function write_logging(message)
    -- Se abre el fichero.
    local logfile = io.open("/opt/ts/var/log/trafficserver/vproxy.log", "a+")

    -- Se escribe el mensaje de logging.
    logfile:write(message, "\n")

    -- Se cierra el fichero.
    logfile:close()
end    

-- Escribe en el fichero de cachés seleccionadas la asignación de caché para el contenido solcitado por un cliente.
-- La estructura del fichero es: <dirección IP del cliente>:<contenido>:<dirección IP de la caché seleccionada>.
function save_cache_selected(client, content, cache)
    write_logging("[vProxy] Saving selected cache at "..cache.." to serve content "..content.." requested by client at "..client)
    
    -- Se abre el fichero.
    local cachesfile = io.open("/opt/ts/etc/trafficserver/vproxy_selected_caches.txt", "a+")
    
    -- Se escribe la asociación cliente:contenido:caché seleccionada.
    cachesfile:write(client..":"..content..":"..cache, "\n")
    
    -- Se cierra el fichero.
    cachesfile:close()
end    

-- Lee del fichero de cachés seleccionadas y comprueba si ya se ha seleccionado anteriormente una caché para el contenido solicitado por un cliente.
-- Devuelve la dirección IP de la caché si hay correspondencia, o nil en caso contrario.
function check_previous_cache_selected(client, content)
    write_logging("[vProxy] Checking if there is already a cache selected to serve content "..content.." requested by client at "..client.."...")
    
    -- Se intenta abrir el fichero.
    local cachesfile = io.open("/opt/ts/etc/trafficserver/vproxy_selected_caches.txt", "r")
    
    -- Si el fichero no existe se devuelve nil.
    if not cachesfile then
        write_logging("[vProxy] Selected caches file does not exist - No cache has been selected previously")
        return nil
    end
    
    -- Variable para almacenar la caché seleccionada previamente, si la hay.
    local cache = ''
    
    -- Se obtienen todas las líneas del fichero.
    local lines = cachesfile:lines()
    
    -- Línea a línea:
    for line in lines do
        -- Se comprueba si hay una asociación de cliente y contenido.
        if string.find(line, client..":"..content) then
            -- Si la hay, se obtiene la caché que se seleccionó previamente.
            cache = line:gsub(client..":"..content..":", "")
        end
    end
    
    -- Se ciera el fichero.
    cachesfile:close()
    
    -- Finalmente, si no hay caché seleccionada previamente, se cierra el fichero y se devuelve nil.
    if cache == '' then
        write_logging("[vProxy] Could not find a previous cache. Will get one from the Orchestrator")
        return nil
    end
    
    write_logging("[vProxy] Found previous cache at "..cache)
    
    -- Se devuelve la caché seleccionada.
    return cache
end

-- Llama a la API REST del Orquestador de la red para obtener la dirección IP de la caché que servirá el contenido.
function get_cache_from_orchestrator(client, content)
    write_logging("[vProxy] Requesting Orchestrator via REST API to select a cache to serve content "..content.." requested by client at "..client.."...")
    
    -- Se importa el módulo HTTP de LuaSocket.
    local http = require("socket.http")
    
    -- Se construye la URL para realizar la llamada a la API REST.
    local url = "http://"..ONOS_USER..":"..ONOS_PASS.."@"..ORCHESTRATOR..":8181/onos/orchestrator/api/cache"
    url = url.."?client="..client.."&content="..content
    
    -- Se realiza la llamada y se obtiene la respuesta, con el cuerpo, el código y las cabeceras.
    local b, c, h = http.request(url)
    local cache = b
    -- cache es el cuerpo de la respuesta (body): directamente la dirección IP de la caché seleccionada.
    
    write_logging("[vProxy] Selected cache at "..cache.." - It will serve content "..content.." requested by client at "..client)
    
    -- Con la caché seleccionada, se guarda la selección en el fichero correspondiente.
    save_cache_selected(client, content, cache)
    
    -- Se devuelve la dirección IP de la caché seleccionada.
    return cache
end

-- Acciones a realizar con el "matching" de la regla de remap en Apache Traffic Server.
function do_remap()
    ts.debug('[vProxy] Doing remap...')
    
    -- Se obtiene la dirección IP del cliente que hace la petición.
    local ip, port, family = ts.client_request.client_addr.get_addr()
    local client = ip
    
    -- Se obtiene el contenido solicitado por el cliente, que será la URI solicitada (sin parámetros).
    local content = ts.client_request.get_uri()
    content = parse_content_uri(content)
    write_logging("[vProxy] Received request for content "..content.." by client at "..client)
    
    -- Se comprueba si existe una caché seleccionada previamente para el contenido solicitado por el cliente.
    local cache = check_previous_cache_selected(client, content)
    
    -- Si no hay caché seleccionada previamente, se llama a la API REST para seleccionar una.
    if cache == nil then
        cache = get_cache_from_orchestrator(client, content)
    end
    -- Si existe una caché seleccionada previamente, la variable "cache" ya es la dirección IP de la caché, y no será nil.
    -- No es necesaria una condición else.
    ts.debug('[vProxy] Selected cache is at IP '..cache)
    
    -- Se configuran los parámetros para el remap.
    ts.client_request.set_url_host(cache)
    ts.client_request.set_url_port(80)
    ts.client_request.set_url_scheme('http')
    
    -- La finalización de la función devuelve un código que indica que se ha realizado el remap.
    return TS_LUA_REMAP_DID_REMAP
end
