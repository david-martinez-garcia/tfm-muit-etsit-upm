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

import org.onlab.rest.AbstractWebApplication;

import java.util.Set;

/**
 * Aplicación web para la API REST del Orquestador.
 * Registra las clases que implementan la API REST.
 * @author David Martínez García
 */
public class OrchestratorWebApplication extends AbstractWebApplication {
    @Override
    public Set<Class<?>> getClasses() {
        return getClasses(OrchestratorWebResource.class);
    }
}
