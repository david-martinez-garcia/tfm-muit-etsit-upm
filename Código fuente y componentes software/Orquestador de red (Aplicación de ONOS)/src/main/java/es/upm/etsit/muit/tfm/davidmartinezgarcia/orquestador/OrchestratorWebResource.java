/*
 * Copyright 2022-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador;

// Importación de paquetes y clases requeridas.

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.onlab.packet.MacAddress;
import org.onosproject.net.HostId;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.intent.Intent;
import org.onosproject.rest.AbstractWebResource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * Recurso web para la exposición de la API REST del Orquestador.
 * Basado en código generado por arquetipos Maven de ONOS.
 * REFERENCIAS DE AYUDA:
 * [1]: https://www.twilio.com/blog/5-ways-to-make-http-requests-in-java
 * [2]: https://stackoverflow.com/questions/13750010/jersey-client-how-to-add-a-list-as-query-parameter
 * [3]: https://stackoverflow.com/questions/13171265/how-to-find-out-incoming-restful-requests-ip-using-jax-rs-on-heroku
 * [4]: https://www.javatips.net/api/onos-master/web/api/src/main/java/org/onosproject/rest/resources/ApplicationsWebResource.java
 * @author David Martínez García
 */
@Path("/api")
public class OrchestratorWebResource extends AbstractWebResource {

    /**
     * Referencia a la interfaz del Orquestador.
     * La interfaz define un servicio OSGi en el entorno de ONOS.
     * Este servicio es implementado por la clase Orchestrator.
     */
    private OrchestratorInterface orchestratorService;

    /**
     * Instancia de NetworkScenario (patrón Singleton).
     */
    private NetworkScenario networkScenario = NetworkScenario.getInstance();

    /**
     * Se invoca cuando un vProxy llama a la API REST del Orquestador para seleccionar la caché que servirá
     * el contenido solicitado por un cliente.
     * La llamada a la API REST se realiza a la URI /onos/orchestrator/api/cache.
     * En la llamada deben especificarse como parámetros la IP del cliente y el contenido solicitado (nombre del contenido).
     * El método, internamente, llama a su vez a la API REST del Coordinador para obtener la dirección IP de la caché. Con ella,
     * esta aplicación instala un Intent para habilitar la comunicación entre el vProxy y la caché seleccionada.
     * @return 200 OK, con la dirección IP de la caché que servirá el contenido.
     */
    @GET
    @Path("/cache")
    public Response getSelectedCache(
        @QueryParam("client") final String client,
        @QueryParam("content") final String content,
        @Context final HttpServletRequest request
    ) {
        // Se obtiene la dirección IP del vProxy a partir de la petición a la API REST.
        String vProxyIp = request.getRemoteAddr();
        // Se llama a la API REST del Coordinador para obtener la dirección IP de la caché seleccionada.
        String cacheIp = getCacheFromCoordinator(client, content);

        // Se obtiene la referencia a la implementación del servicio del Orquestador.
        orchestratorService = get(OrchestratorInterface.class);

        // Con las direcciones IP del vProxy y la caché seleccionada, se construyen sendos objetos HostId.
        // Se emplean para comprobar y construir un Intent entre esos hosts en la red.
        HostId hostId1 = HostId.hostId(MacAddress.valueOf(networkScenario.getMacFromIp(vProxyIp)));
        HostId hostId2 = HostId.hostId(MacAddress.valueOf(networkScenario.getMacFromIp(cacheIp)));

        // Se comprueba si ya existe un Intent entre el vProxy y la caché seleccionada.
        boolean check = orchestratorService.checkIntentAlreadySubmitted(hostId1, hostId2);

        // En caso de no existir:
        if (check == false) {
            // Se crea y se envía.
            orchestratorService.submitHostToHostIntent(hostId1, hostId2,
            DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(),
            Intent.DEFAULT_INTENT_PRIORITY);
        }
        
        /**
         * En todos los casos, como respuesta al HTTP GET enviado por el vProxy,
         * se devuelve la IP de la caché seleccionada para que éste pueda hacer la redirección.
         */ 
        return ok(cacheIp).build();
    }

    /**
     * Llama a la API REST del Coordinador para obtener la caché que 
     * servirá el contenido solicitado por el cliente. El contenido se servirá con un vProxy de intermediario.
     * @param client - Dirección IP del cliente que ha solicitado el contenido.
     * @param content - Contenido solicitado (nombre del contenido).
     * @return Cadena de caracteres con la dirección IP de la caché seleccionada.
     */
    public static String getCacheFromCoordinator(String client, String content) {
        // Se define la URL base donde está escuchando la API REST del Coordinador.
        String coordinatorEndPoint = "http://coordinador.pruebasgiros.home.arpa:5000/coordinator/api/cache";
        // Se definen los parámetros: cliente solicitante (dirección IP) y contenido solicitado (nombre del contenido).
        String clientParameter = "?client="+client;
        String contentParameter = "&content="+content;
        // Con la URL base y los parámetros, se construye la URL completa para realizar la llamada HTTP GET a la API REST del Coordinador.
        String urlString = coordinatorEndPoint+clientParameter+contentParameter;
        // Cadena de caracteres para almacenar el resultado de la respuesta.
        String responseBody = "";
        try {
            // Se construye el objeto URL a partir de la URL completa.
            URL url = new URL(urlString);
            /**
             * Se abre la conexión a partir del objeto URL, se establecen las
             * propiedades de la conexión (método GET y respuesta esperada en formato JSON) y se espera
             * el cuerpo de la respuesta, el cual se escribe línea a línea en la cadena de caracteres
             * responseBody si la respuesta al HTTP GET devuelve el código 200 OK.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "application/json");
            BufferedReader br = null;
            StringBuilder sb = new StringBuilder();
            String line = "";
            if (connection.getResponseCode() == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
            }
            responseBody = sb.toString();
        }
        /**
         * Manejo de excepciones.
         */
        catch (MalformedURLException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
        /**
         * Se utiliza la librería GSON, de Google, para interpretar la respuesta del
         * Coordinador. La respuesta está en notación JSON con el nombre de host de
         * la caché seleccionada y su dirección IP.
         */
        Gson gson = new Gson();
        // Se realiza el "parsing" a un objeto JSON desde el cuerpo de la respuesta.
        JsonObject jsonObject = gson.fromJson(responseBody, JsonObject.class);
        /**
         * A partir del objeto JSON generado, se obtiene el valor de la clave "ip_addr",
         * que contiene la dirección IP de la caché seleccionada.
         * La dirección IP se almacena en una cadena de caracteres, la cual se devuelve
         * como finalización de la ejecución de este método.
         */ 
        JsonElement jsonElement = jsonObject.get("ip_addr");
        String cacheIp = jsonElement.getAsString();
        return cacheIp;
    }

}
