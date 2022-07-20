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

import org.onosproject.net.HostId;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;

/**
 * Interfaz del Orquestador.
 * Define un servicio OSGi de ONOS para el componente de aplicación del Orquestador.
 * Esta interfaz debe definir los métodos usados por terceras clases o aplicaciones que hacen uso
 * del Orquestador como servicio.
 * @author David Martínez García
 */
public interface OrchestratorInterface {

    /**
     * Envía un Intent de tipo HostToHostIntent para habilitar la comunicación entre los hosts cuyos HostId se pasan como parámetro.
     * @param host1 - HostId del primer Host extremo de la comunicación.
     * @param host2 - HostId del segundo Host extremo de la comunicación.
     * @param selector - TrafficSelector (selector de tráfico) a aplicar - para matching de reglas OpenFlow.
     * @param treatment - TrafficTreatment (tratamiento de tráfico) a aplicar - acciones a aplicar al tráfico en las reglas de OpenFlow.
     * @param priority - Prioridad a configurar para el Intent.
     */   
    void submitHostToHostIntent(HostId host1, HostId host2, TrafficSelector selector, TrafficTreatment treatment, int priority);

    /**
     * Comprueba si ya existe un Intent para habilitar la comunicación entre los hosts cuyos HostId
     * se pasan como parámetros.
     * @param host1 - HostId del primer Host extremo de la comunicación.
     * @param host2 - HostId del segundo Host extremo de la comunicación.
     * @return Boolean con el resultado de la comprobación: true si existe y false si no existe.
     */
    boolean checkIntentAlreadySubmitted(HostId host1, HostId host2);

}