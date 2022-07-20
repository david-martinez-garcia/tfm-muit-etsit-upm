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

// Importación de paquetes, clases y constantes requeridas.

import org.onlab.packet.Ethernet;
import org.onlab.packet.IPv4;
import org.onlab.packet.IpAddress;
import org.onlab.packet.IpPrefix;
import org.onlab.packet.MacAddress;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.ConnectPoint;
import org.onosproject.net.Device;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.PortNumber;
import org.onosproject.net.device.DeviceService;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.flow.criteria.Criteria;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyService;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Dictionary;
import java.util.LinkedList;
import java.util.Map;
import java.util.Properties;

import static es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador.OrchestratorConstants.UNSPECIFIED_REDIRECTION;
import static es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador.OrchestratorConstants.TCP_IPV4_REDIRECTION;
import static es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador.OrchestratorConstants.DNS_REDIRECTION;

/**
 * Clase Orchestrator.
 * Orquestador de la red y componente principal de la aplicación.
 * Implementación del servicio OSGi en ONOS definido por la interfaz OrchestratorInterface.
 * @author David Martínez García
 */
@Component(immediate = true,
           service = {OrchestratorInterface.class},
           property = {
               "withRedirection" + "=" + UNSPECIFIED_REDIRECTION
           })
public class Orchestrator implements OrchestratorInterface {

    /**
     * Objeto Logger para mostrar mensajes de log en ONOS.
     */
    private final Logger log = LoggerFactory.getLogger(Orchestrator.class);

    /**
     * Referencia al servicio CoreService para la activación de la aplicación en ONOS.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected CoreService coreService;

    /**
     * Referencia al servicio ComponentConfigService para la configuración de propiedades de la aplicación en ONOS.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected ComponentConfigService cfgService;

    /**
     * Referencia al servicio IntentService para la gestión de los Intents de ONOS:
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected IntentService intentService;

    /**
     * Referencia al servicio FlowRuleService para la gestión de reglas en las tablas de flujo.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected FlowRuleService flowRuleService;

    /**
     * Referencia al servicio PacketService para la gestión de paquetes (tráfico de red) en ONOS.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected PacketService packetService;

    /**
     * Referencia al servicio TopologyService para la gestión de la topología de la red SDN.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected TopologyService topologyService;

    /**
     * Referencia al servicio DeviceService para la gestión de los dispositivos en la red SDN.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected DeviceService deviceService;

    /**
     * Referencia al servicio HostService para la gestión de los hosts en la red SDN.
     */
    @Reference(cardinality = ReferenceCardinality.MANDATORY)
    protected HostService hostService;

    /**
     * Identificador de la aplicación en el contexto de ONOS.
     */
    private ApplicationId appId;

    /**
     * Tipo de redirección usado en el escenario en el que está instalado la aplicación.
     * Por defecto, se utiliza el valor UNSPECIFIED_REDIRECTION. Debe configurarse utilizando la CLI.
     */
    private int withRedirection = UNSPECIFIED_REDIRECTION;

    /**
     * Representación de los hosts en el escenario de red. Se usa con patrón Singleton.
     */
    private NetworkScenario networkScenario;
    
    /**
     * Identifica si un cliente ha sido el origen de la solicitud de un contenido.
     */
    private boolean clientIsSourceForContentRequest = false;

    /**
     * Dirección Ethernet/MAC flotante de contenidos.
     * Se almacena como objeto MacAddress.
     */
    private MacAddress content_eth_addr;

    /**
     * Dirección IP  flotante de contenidos.
     * Se almacena en formato int (entero).
     */
    private int content_ip_addr;

    /**
     * Se almacena la dirección MAC/Ethernet del proxy que sirve los contenidos (si los sirve la propia red).
     * Se almacena como objeto MacAddress.
     */
    private MacAddress proxy_eth_addr;

    /**
     * Se almacena la dirección IP del proxy que sirve los contenidos (si los sirve la propia red).
     * Se almacena en formato int (entero).
     */
    private int proxy_ip_addr;

    /**
     * Clase interna ReactivePacketProcessorWithTcpIpv4Redirection.
     * Implementa la interfaz PacketProcessor para procesamiento reactivo de paquetes según el protocolo OpenFlow.
     * Realiza procesado reactivo de paquetes para tráfico ARP y TCP/IPv4.
     * Utilizado con redirección TCP/IPv4.
     * Tomado del ejemplo de programación de ONOS accesible en la siguiente referencia:
     * [1]: https://wiki.onosproject.org/display/ONOS11/Application+tutorial#Applicationtutorial-Writingtheapplication
     */
    private class ReactivePacketProcessorWithTcpIpv4Redirection implements PacketProcessor {

        /* El método process implementa la lógica de procesado de paquetes. */
        @Override
        public void process(PacketContext context) {
            if (context.isHandled() == true) {
                return;
            }
            
            // Del contexto de paquetes se obtiene el paquete de entrada y el punto de conexión desde donde se obtuvo.
            InboundPacket inPacket = context.inPacket();
            ConnectPoint connectPoint = inPacket.receivedFrom();
            log.info("[Orchestrator][Reactive traffic processing] Received OF_PACKET_IN from network device "+connectPoint.deviceId().toString());
            
            // Del paquete de entrada se obtiene la trama Ethernet.
            Ethernet inEthernet = inPacket.parsed();

            // Se obtienen como HostId los extremos de la comunicación de la cabecera Ethernet: origen y destino.
            HostId srcHostId = HostId.hostId(inEthernet.getSourceMAC());
            HostId dstHostId = HostId.hostId(inEthernet.getDestinationMAC());
            log.info("[Orchestrator][Reactive traffic processing] OF_PACKET_IN contains Ethernet frame from "+srcHostId.mac().toString()
            +" to "+dstHostId.mac().toString());

            /**
             * Utilizando el servicio de hosts de ONOS, HostService, se intenta obtener las referencias a los hosts
             * origen y destino de la comunicación. Nótese que, si alguno no existiera, el objeto correspondiente sería null.
             */
            Host src = hostService.getHost(srcHostId);
            Host dst = hostService.getHost(dstHostId);

            /**
             * Se obtiene el valor del campo EtherType de la cabecera Ethernet.
             * El campo EtherType identifica el tipo de protocolo que lleva la carga útil de la trama Ethernet.
             * En esta aplicación, será ARP o IPv4/TCP, según lo configurado en los conmutadores para redirigr el tráfico
             * al controlador ONOS.
             */
            short etherType = inEthernet.getEtherType();

            // Si el EtherType es ARP:
            if (etherType == Ethernet.TYPE_ARP) {
                log.info("[Orchestrator][Reactive traffic processing] EtherType is ARP");
                
                // En caso de que el destino no exista porque ONOS todavía no tiene conocimiento de él:
                if (dst == null) {
                    // Se envía el paquete por el puerto FLOOD del conmutador correspondiente y se sale del procesado.
                    flood(context);
                    return;
                }
                // En caso de que sí exista:
                else {
                    // Se instala un Intent, si no existe, y se reenvía el paquete al destino.
                    if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                        submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                    }
                    forwardPacketToDst(context.inPacket().unparsed(), dst);
                }
            }

            // Si el EtherType es IPv4:
            if (etherType == Ethernet.TYPE_IPV4) {
                // Puesto que la aplicación sólo procesa tráfico IPv4 con TCP (SYN), no es necesario especificar ninguna condición adicional.

                log.info("[Orchestrator][Reactive traffic processing] EtherType is IPv4");
                log.info("[Orchestrator][Reactive traffic processing][IPv4] Payload is TCP with SYN flag");

                // Se obtiene la carga útil del protocolo IP.
                IPv4 ipv4 = (IPv4) inEthernet.getPayload();

                log.info("[Orchestrator][Reactive packet processing][IPv4] Source is at "+IPv4.fromIPv4Address(ipv4.getSourceAddress())+
                " and destination is at "+IPv4.fromIPv4Address(ipv4.getDestinationAddress()));

                // Si el origen es un cliente:
                if (networkScenario.isClient(IPv4.fromIPv4Address(ipv4.getSourceAddress()))) {
                    log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Source is a client");
                    /**
                     * Si el destino es para un contenido que sirve la red, se realiza el
                     * procesado, modificando el paquete y reenviándolo al proxy de su nodo MEC.
                     * Se envía un Intent para próximas peticiones.
                     */
                    if (checkContentIsServed(IPv4.fromIPv4Address(ipv4.getDestinationAddress()))) {
                        log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Destination is for a content the network serves");

                        // Se guardan las direcciones MAC/Ethernet e IP flotantes de los contenidos.
                        content_eth_addr = inEthernet.getDestinationMAC();
                        content_ip_addr = ipv4.getDestinationAddress();

                        // Se obtiene la referencia del host cliente en el escenario para poder obtener su localización MEC.
                        NetworkScenarioHost client = networkScenario.getHostsMap().get(IPv4.fromIPv4Address(ipv4.getSourceAddress()));
                            
                        // Se obtiene el proxy del nodo MEC en el que está el cliente.
                        String proxyIp = null;
                        String proxyMac = null;
                        Map<String, NetworkScenarioHost> hosts = networkScenario.getHostsMap();
                        for (NetworkScenarioHost host: hosts.values()) {
                            if ((host.getLocation().equals(client.getLocation()))
                            && host.getHostname().toLowerCase().contains("proxy")) {
                                proxyIp = host.getIpAddress();
                                proxyMac = host.getEthAddress();
                                log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Selected proxy at MEC location "
                                +client.getLocation()+" for traffic redirection");
                            }
                        }

                        // Se obtiene el objeto Host de ONOS que referencia al proxy seleccionado.
                        Host proxy = hostService.getHost(HostId.hostId(MacAddress.valueOf(proxyMac)));

                        // Se guarda el paquete IPv4 original para realizar las modificaciones.
                        IPv4 modifiedIPv4 = ipv4;
                        // La dirección IP de destino ahora es la del proxy. La de origen sigue siendo la del cliente.
                        modifiedIPv4.setDestinationAddress(proxyIp);
                            
                        // Se actualiza la trama Ethernet con el datagrama IPv4 modificado.
                        inEthernet.setPayload(modifiedIPv4);
                        // La dirección MAC de destino ahora es la del proxy. La de origen sigue siendo la del cliente.
                        inEthernet.setDestinationMACAddress(proxyMac);

                        // Se serializa la trama Ethernet para crear el paquete y se envuelve en un ByteBuffer.
                        ByteBuffer packet = ByteBuffer.wrap(inEthernet.serialize());

                        /**
                         * Se crea el selector y el tratamiento de tráfico, para próximas comunicaciones.
                         * SELECTOR: Se quiere hacer correspondencia con el tráfico TCP/IPv4 procedente del cliente hacia los contenidos.
                         * TRATAMIENTO: Se quiere modificar las cabeceras, de tal forma que se ponga como direcciones de destino las del proxy en el mismo
                         * nodo MEC que el cliente.
                         * En el switch correspondiente, se reenvía el tráfico por el puerto al que está conectado el proxy.
                         */
                        TrafficSelector selector = DefaultTrafficSelector.builder()
                        .matchInPort(src.location().port())
                        .matchEthSrc(src.mac())
                        .matchEthDst(content_eth_addr)
                        .matchIPSrc(IpPrefix.valueOf(ipv4.getSourceAddress(), 32))
                        .matchIPDst(IpPrefix.valueOf(content_ip_addr, 32))
                        .matchEthType(Ethernet.TYPE_IPV4)
                        .matchIPProtocol(IPv4.PROTOCOL_TCP)
                        .build();

                        TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                        .setEthDst(MacAddress.valueOf(proxyMac))
                        .setIpDst(IpAddress.valueOf(proxyIp))
                        .setOutput(proxy.location().port())
                        .build();

                        // Se instala una regla de flujo en el conmutador MEC correspondiente (el que había reenviado el paquete al controlador).
                        installFlowRule(context.inPacket().receivedFrom().deviceId(), selector, treatment, FlowRule.MAX_PRIORITY);
                        // Se reenvía el paquete al proxy.
                        forwardPacketToDst(packet, proxy);

                        // Se guarda que la petición de contenidos ha sido originada por un cliente, para procesar la respuesta desde el proxy.
                        clientIsSourceForContentRequest = true;

                        return;
                    }
                    /**
                     * En caso de que el contenido solicitado no se sirva por la propia red, simplemente se crea el Intent, si no existe,
                     * y se reenvía el paquete tal cual al destino.
                     */
                    else {
                        log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Destination is for a content the network does not serve");
                        if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                            submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                        }
                        forwardPacketToDst(context.inPacket().unparsed(), dst);
                        return;
                    }
                }
                // Si el origen es un proxy:
                else if (networkScenario.isProxy(IPv4.fromIPv4Address(ipv4.getSourceAddress()))) {
                    log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Source is a proxy");
                    /**
                     * En caso de que el destino sea un cliente, hay que comprobar si antes ha habido una petición
                     * por parte del cliente para un contenido. En ese caso hay que modificar el paquete y reenviarlo
                     * al cliente, para que éste crea que viene directamente de la dirección flotante de contenidos.
                     */
                    if (networkScenario.isClient(IPv4.fromIPv4Address(ipv4.getDestinationAddress()))) {
                        if (clientIsSourceForContentRequest == true) {
                            log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Destination is a client for a previous content request");
                                
                            // Se guarda la dirección MAC/Ethernet e IP del proxy que servirá los contenidos.
                            proxy_eth_addr = inEthernet.getSourceMAC();
                            proxy_ip_addr = ipv4.getSourceAddress();
                                
                            /**
                             * Las direcciones MAC e IPv4 origen ahora tienen que cambiarse por las flotantes del contenido solicitado,
                             * almacenadas con anterioridad.
                             * Ahora hay que obtener el paquete entrante procedente del proxy y modificar las cabeceras para
                             * que el cliente crea que viene de esas direcciones flotantes.
                             */
                                
                            // Se obtiene el datagrama IPv4 del paquete entrante, procedente del proxy y con destino al cliente.
                            IPv4 modifiedIPv4 = ipv4;
                                
                            // Se cambia la dirección de origen por la flotante de contenidos, obtenida del contexto anterior.
                            modifiedIPv4.setSourceAddress(content_ip_addr);

                            // Con el datagrama IPv4 modificado, se establece como payload de la trama Ethernet.
                            inEthernet.setPayload(modifiedIPv4);
                            // Se cambia la dirección de origen por la flotante de contenidos, obtenida del contexto anterior.
                            inEthernet.setSourceMACAddress(content_eth_addr);

                            // Se serializa la trama Ethernet modificada antes de reenviarla al cliente.
                            ByteBuffer packet = ByteBuffer.wrap(inEthernet.serialize());

                            /**
                             * Se crea el selector y el tratamiento de tráfico, para próximas comunicaciones.
                             * SELECTOR: Se quiere hacer correspondencia con el tráfico TCP/IPv4 procedente del proxy hacia el cliente.
                             * TRATAMIENTO: Se quiere modificar las cabeceras, de tal forma que se ponga como direcciones origen
                             * las flotantes del contenido solicitado por el cliente.
                             * En el switch correspondiente, se reenvía el tráfico por el puerto al que está conectado el cliente.
                             */
                            TrafficSelector selector = DefaultTrafficSelector.builder()
                            .matchInPort(src.location().port())
                            .matchEthSrc(proxy_eth_addr)
                            .matchEthDst(dst.mac())
                            .matchIPSrc(IpPrefix.valueOf(proxy_ip_addr, 32))
                            .matchIPDst(IpPrefix.valueOf(ipv4.getDestinationAddress(), 32))
                            .matchEthType(Ethernet.TYPE_IPV4)
                            .matchIPProtocol(IPv4.PROTOCOL_TCP)
                            .build();

                            TrafficTreatment treatment = DefaultTrafficTreatment.builder()
                            .setEthSrc(content_eth_addr)
                            .setIpSrc(IpAddress.valueOf(content_ip_addr))
                            .setOutput(dst.location().port())
                            .build();

                            // Se instala una regla de flujo en el conmutador MEC correspondiente (el que había reenviado el paquete al controlador).
                            installFlowRule(context.inPacket().receivedFrom().deviceId(), selector, treatment, FlowRule.MAX_PRIORITY);
                            // Se reenvía el paquete al cliente.
                            forwardPacketToDst(packet, dst);
                            
                            // Ya se ha procesado la respuesta inicial del proxy al cliente.
                            clientIsSourceForContentRequest = false;
                                
                            return;
                        }
                        /**
                         * Si previamente no ha habido petición inicial del cliente, se crea el Intent, si no existe,
                         * y se reenvía el paquete.
                         */
                        else {
                            log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Destination is a client with no previous content request");
                            if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                                submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                            }
                            forwardPacketToDst(context.inPacket().unparsed(), dst);
                            return;
                        }
                    }
                    /**
                     * En caso de que el destino de la comunicación del proxy sea otro host
                     * distinto a un cliente, se crea el Intent, si no existe, y se reenvía el paquete normalmente.
                     */
                    else {
                        log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Destination is another host");
                        if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                            submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                        }
                        forwardPacketToDst(context.inPacket().unparsed(), dst);
                        return;
                    }
                }
                // Si el origen es cualquier host que no sea un cliente o un proxy:
                else {
                    log.info("[Orchestrator][Reactive traffic processing][IPv4][TCP] Source is not a client or a proxy");
                    // Se crea el Intent, si no existe, y se reenvía el paquete normalmente.
                    if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                        submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                    }
                    forwardPacketToDst(context.inPacket().unparsed(), dst);
                    return;
                }
            }
        }
    }

    /**
     * Clase interna ReactivePacketProcessorWithDnsRedirection.
     * Implementa la interfaz PacketProcessor para procesamiento reactivo de paquetes según el protocolo OpenFlow.
     * Realiza procesado reactivo de paquetes para tráfico ARP.
     * Utilizado con redirección por DNS.
     * Tomado del ejemplo de programación de ONOS accesible en la siguiente referencia:
     * [1]: https://wiki.onosproject.org/display/ONOS11/Application+tutorial#Applicationtutorial-Writingtheapplication
     */
    private class ReactivePacketProcessorWithDnsRedirection implements PacketProcessor {

        /* El método process implementa la lógica de procesado de paquetes. */
        @Override
        public void process(PacketContext context) {
            if (context.isHandled() == true) {
                return;
            }

            // Del contexto de paquetes se obtiene el paquete de entrada y el punto de conexión desde donde se obtuvo.
            InboundPacket inPacket = context.inPacket();
            ConnectPoint connectPoint = inPacket.receivedFrom();
            log.info("[Orchestrator][Reactive traffic processing] Received OF_PACKET_IN from network device "+connectPoint.deviceId().toString());
            
            // Del paquete de entrada se obtiene la trama Ethernet.
            Ethernet inEthernet = inPacket.parsed();

            // Se obtienen como HostId los extremos de la comunicación de la cabecera Ethernet: origen y destino.
            HostId srcHostId = HostId.hostId(inEthernet.getSourceMAC());
            HostId dstHostId = HostId.hostId(inEthernet.getDestinationMAC());
            log.info("[Orchestrator][Reactive traffic processing] OF_PACKET_IN contains Ethernet frame from "+srcHostId.mac().toString()
            +" to "+dstHostId.mac().toString());

            /**
             * Utilizando el servicio de hosts de ONOS, HostService, se intenta obtener la referencia al host
             * destino de la comunicación. Si no existiera, se devolvería null.
             */
            Host dst = hostService.getHost(dstHostId);

            // Como este procesador de paquetes sólo procesa tráfico ARP, no es necesario especificar una condición para el campo EtherType.
            log.info("[Orchestrator][Reactive traffic processing] EtherType is ARP");
                
            // En caso de que el destino no exista porque ONOS todavía no tiene conocimiento de él:
            if (dst == null) {
                // Se envía el paquete por el puerto FLOOD del conmutador correspondiente y se sale del procesado.
                flood(context);
                return;
            }
            // En caso de que sí exista:
            else {
                // Se instala un Intent, si no existe, para habilitar la comunicación entre los hosts y se reenvía el paquete al destino.
                if (!checkIntentAlreadySubmitted(srcHostId, dstHostId)) {
                    submitHostToHostIntent(srcHostId, dstHostId, DefaultTrafficSelector.builder().build(), DefaultTrafficTreatment.builder().build(), Intent.DEFAULT_INTENT_PRIORITY);
                }
                forwardPacketToDst(context.inPacket().unparsed(), dst);
            }
        }
    }

    /**
     * Referencia al procesador de paquetes definido y utilizado por la aplicación.
     * En función del valor de la propiedad withRedirection será:
     * -> Implementación ReactivePacketProcessorWithTcpIpv4Redirection si withRedirection = 1.
     * -> Implementación ReactivePacketProcessorWithDnsRedirection si withRedirection = 2.
     */
    private PacketProcessor processor;
    
    /**
     * Este método se invoca con la activación de la aplicación en ONOS.
     * @param context - Contexto del componente de aplicación.
     */
    @Activate
    protected void activate(ComponentContext context) {
        // Se obtiene la referencia a la instancia con información sobre el escenario de red.
        networkScenario = NetworkScenario.getInstance();
        log.info("[Orchestrator] NetworkScenario instantiated");

        // Se registra la aplicación en el servicio CoreService de ONOS y se obtiene un identificador de aplicación.
        appId = coreService.registerApplication("es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador");
        log.info("[Orchestrator] Application registered with AppID: "+appId);

        // Se registra este componente de aplicación con el servicio de configuración.
        cfgService.registerProperties(getClass());
        // Se lee la propiedad withRedirection y se configura con su valor por defecto.
        configureComponent(context);

        log.info("[Orchestrator] Application activated");
    }

    /**
     * Este método se invoca con el evento de modificación de la propiedad withRedirection de la aplicación.
     * El evento se produce en el contexto de ONOS.
     * @param context - Contexto del componente de aplicación.
     */
    @Modified
    public void modified(ComponentContext context) {
        log.info("[Orchestrator] Application modified with new withRedirection property value");
        // Directamente se invoca el método que configura la propiedad withRedirection.
        configureComponent(context);
    }

    /**
     * Configura la propiedad withRedirection de la aplicación con el valor que se ha
     * especificado en el contexto del componente de aplicación.
     * @param context - Contexto del componente de aplicación.
     */
    private void configureComponent(ComponentContext context) {
        // Se intenta leer el diccionario de propiedades del contexto. Si es null, se crea uno nuevo.
        Dictionary<?, ?> properties = context != null ? context.getProperties() : new Properties();

        // Se obtiene el valor de la propiedad withRedirection en formato de cadena de caracteres (String).
        String property = Tools.get(properties, "withRedirection");

        // Se transforma de String a int y se guarda el valor de la propiedad.
        withRedirection = Integer.parseInt(property);
        log.info("[Orchestrator] Property withRedirection has value "+withRedirection);

        // Se configura el tipo de procesador de tráfico a utilizar en función del valor de la propiedad configurada.
        definePacketProcessor();
    }

    /**
     * Este método se invoca con la desactivación de la aplicación en ONOS.
     */
    @Deactivate
    protected void deactivate() {
        log.info("[Orchestrator] Deactivating application...");
        
        // Se eliminan todos los Intents enviados por esta aplicación.
        deleteIntents();

        // Se elimina el procesador de paquetes reactivo de esta aplicación que se haya registrado.
        packetService.removeProcessor(processor);
        processor = null;

        // Se desregistran las propiedades de la aplicación.
        cfgService.unregisterProperties(getClass(), false);

        log.info("[Orchestrator] Application deactivated");
    }

    /**
     * Define el procesador de paquetes a utilizar.
     * La definición se realiza en base al valor de la propiedad withRedirection,
     * almacenada en la variable withRedirection.
     */
    private void definePacketProcessor() {
        log.info("[Orchestrator] Defining and configuring packet processor");
        log.info("[Orchestrator] Event triggered because of new withRedirection property value");

        // En función del valor de la propiedad WITH_REDIRECTION:
        switch (withRedirection) {
            // Si se usa redirección por TCP/IPv4:
            case TCP_IPV4_REDIRECTION:
                log.info("[Orchestrator] Using TCP/IPv4 redirection");
                // Se comprueba si antes ya había registrado otro procesador de tráfico:
                if (processor != null) {
                    // En ese caso, se elimina.
                    packetService.removeProcessor(processor);
                    processor = null;
                    log.info("[Orchestrator] Previous packet processor removed");
                }

                // En cualquier caso, se instancia el procesador de paquetes y se añade al servicio de paquetes de ONOS.
                processor = new ReactivePacketProcessorWithTcpIpv4Redirection();
                packetService.addProcessor(processor, PacketProcessor.ADVISOR_MAX);
                log.info("[Orchestrator] Reactive packet processor with TCP/IPv4 redirection instantiated and registered");

                // Se instalan reglas de flujo en los conmutadores de los nodos MEC para redirigir el tráfico TCP SYN con IPv4 a esta aplicación.
                // No usado actualmente. Los flujos se definen manualmente por una implementación de OpenFlow 1.5 incompleta en Open vSwitch.
                // installInitialTCPv4FlowRules();

                break;
            
            // Si se usa redirección por DNS:
            case DNS_REDIRECTION:
            log.info("[Orchestrator] Using DNS redirection");
                // Se comprueba si antes ya había registrado otro procesador de tráfico:
                if (processor != null) {
                    // En ese caso, se elimina.
                    packetService.removeProcessor(processor);
                    processor = null;
                    log.info("[Orchestrator] Previous packet processor removed");
                }

                // En cualquier caso, se instancia el procesador de paquetes y se añade al servicio de paquetes de ONOS.
                processor = new ReactivePacketProcessorWithDnsRedirection();
                packetService.addProcessor(processor, PacketProcessor.ADVISOR_MAX);
                log.info("[Orchestrator] Reactive packet processor with DNS redirection instantiated and registered");

                break;
        }
    }

    /**
     * Elimina (purga) los Intents definidos por la aplicación (y sólo los instalados por esta aplicación).
     */
    private void deleteIntents() {
        log.info("[Orchestrator] Deleting all Intents installed by this application...");
        // Se obtienen todos los Intents enviados por esta aplicación.
        Iterable<Intent> intents = intentService.getIntentsByAppId(appId);
        // Se recorre Intent a Intent:
        for (Intent intent: intents) {
            // Se obtiene el estado del Intent.
            IntentState state = intentService.getIntentState(intent.key());
            // Si el estado es INSTALLED (instalado):
            if (state == IntentState.INSTALLED) {
                // Se retira el Intent (WITHDRAW). El proceso no es inmediato.
                intentService.withdraw(intent);
                // Se vuelve a obtener el estado en bucle hasta que sea retirado (WITHDRAWN).
                state = intentService.getIntentState(intent.key());
                while (state != IntentState.WITHDRAWN) {
                    // Esperar (no hacer nada).
                    state = intentService.getIntentState(intent.key());
                }
                // Cuando se ha retirado completamente, se purga (elimina) de ONOS, y se pasa al siguiente Intent.
                intentService.purge(intent);
                continue;
            }
            // Para otros Intents que no tengan el estado INSTALLED, directamente se purgan (eliminan) de ONOS.
            else {
                intentService.purge(intent);
            }
        }
        log.info("[Orchestrator] Intents deleted");
    }

    @Override
    public boolean checkIntentAlreadySubmitted(HostId host1, HostId host2) {
        log.info("[Orchestrator] Checking if there is already an Intent submitted between "+host1.toString()+" and "+host2.toString()+"...");
        // Se almacena en una variable de tipo boolean el resultado de la comprobación. El valor por defecto es false.
        boolean check = false;

        // A partir de los HostId pasados como parámetro, se construye una LinkedList (Collection) para realizar la comprobación de manera más rápida.
        Collection<HostId> hostIds = new LinkedList<HostId> ();
        hostIds.add(host1);
        hostIds.add(host2);

        // Se obtienen todos los Intents enviados por esta aplicación.
        Iterable<Intent> intents = intentService.getIntentsByAppId(appId);

        // Se itera Intent a Intent.
        for (Intent intent: intents) {
            // Si el Intent es de la instancia HostToHostIntent:
            if (intent instanceof HostToHostIntent) {
                // Se obtienen sus recursos de red (HostIds):
                Collection<NetworkResource> networkResources = intent.resources();
                // Y si los HostIds pasados como parámetro están definidos en ese Intent:
                if (networkResources.containsAll(hostIds)) {
                    /**
                     * Entonces, significa que ya hay un Intent enviado para habilitar la comunicación entre
                     * los hosts cuyos correspondientes HostId se han pasado como parámetros.
                     * Por tanto, la variable para almacenar el resultado se pone a true, y se
                     * sale del bucle, ya que al haber encontrado un Intent, no es necesario procesar más.
                     */
                    check = true;
                    log.info("[Orchestrator] There is already an Intent submitted between "+host1.toString()+" and "+host2.toString());
                    log.info("[Orchestrator] There is no need for submitting another Intent between those resources");
                    break;
                }
            }
        }

        // Se muestra un mensaje de log si la comprobación es negativa.
        if (check == false) {
            log.info("[Orchestrator] There is not an Intent submitted between "+host1.toString()+" and "+host2.toString());
        }

        // Se devuelve el resultado de la comprobación.
        return check;
    }

    @Override
    public void submitHostToHostIntent(HostId host1, HostId host2, TrafficSelector selector, TrafficTreatment treatment, int priority) {
        log.info("[Orchestrator] Submitting HostToHostIntent between "+host1.mac().toString()+" and "+host2.mac().toString()+"...");
        
        // Se construye el Intent a partir de los parámetros.
        HostToHostIntent intent = HostToHostIntent.builder().appId(appId).one(host1).two(host2).selector(selector).treatment(treatment).priority(priority).build();
        
        // Y se envía (submit) al servicio de Intents de ONOS (IntentService).
        intentService.submit(intent);

        log.info("[Orchestrator] HostToHostIntent submitted");
    }

    /**
     * Instala una regla de flujo en el dispositivo de red cuyo DeviceId se pasa como parámetro.
     * Adicionalmente, se especifica el selector de tráfico, el tratamiento deseado, y la prioridad a establecer para la regla.
     * @param deviceId - Identificador del dispositivo de red (switch) en el que instalar la regla.
     * @param selector - Selector de tráfico a aplicar para la regla.
     * @param treatment - Tratamiento de tráfico a aplicar para la regla.
     * @param priority - Prioridad a aplicar para la regla.
     */
    private void installFlowRule(DeviceId deviceId, TrafficSelector selector, TrafficTreatment treatment, int priority) {
        // Se construye la regla de flujo (objeto FlowRule) a partir de los parámetros pasados y se hace permanente.
        FlowRule flowRule = DefaultFlowRule.builder()
        .forDevice(deviceId)
        .fromApp(appId)
        .withPriority(priority)
        .withSelector(selector)
        .withTreatment(treatment)
        .makePermanent()
        .build();

        // Se envía la regla de flujo al servicio de reglas de flujo de ONOS (FlowRuleService).
        flowRuleService.applyFlowRules(flowRule);
    }

    /**
     * Instala en los switches MEC reglas de flujo iniciales para interceptar todo el tráfico TCP
     * en datagramas IPv4.
     * El procesado de este tráfico se hace en la clase interna ReactivePacketProcessor.
     * NO UTILIZADO: El funcionamiento es enviar al controlador (esta aplicación) todo el tráfico TCP/IPv4
     * con el flag SYN activo (establecimiento de conexión). Puesto que la implementación de OpenFlow 1.3 en ONOS
     * no soporta reglas de flujo con match a los flags de TCP, las reglas de flujo se instalan tras lanzar los
     * escenarios de red en VNX.
     * ONOS soporta matching con los flags de TCP en OpenFlow 1.5, pero Open vSwitch, que es el switch utilizado,
     * no implementa correctamente esta versión de OpenFlow y el controlador no establece bien la conexión con los switches.
     */
    private void installInitialTCPv4FlowRules() {
        // Procesar en el controlador todo el tráfico TCP/IPv4.
        // Para ello, instalar las reglas de flujo correspondientes en los switches MEC.
        
        // Se construye el selector de tráfico, para el tráfico TCP/IPv4.
        TrafficSelector selector = DefaultTrafficSelector.builder()
        .matchEthType(Ethernet.TYPE_IPV4)
        .matchIPProtocol(IPv4.PROTOCOL_TCP)
        //.add(Criteria.matchTcpFlags(2)) --> Con soporte correcto para OpenFlow 1.5.
        // La implementación Open vSwitch para OpenFlow 1.5 está incompleta en esta parte.
        .build();

        // Se construye el tratamiento de tráfico, que consiste en reenviar el tráfico al controlador (esta aplicación).
        TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(PortNumber.CONTROLLER).build();

        // Se obtienen todos los dispositivos de red (conmutadores) del servicio de dispositivos de ONOS (DeviceService).
        Iterable<Device> devices = deviceService.getDevices();
        // Se itera dispositivo a dispositivo.
        for (Device device: devices) {
            // Se obtiene el identificador del dispositivo.
            DeviceId deviceId = device.id();
            // Si el identificador (Datapath ID) se corresponde con el de alguno de los conmutadores de los nodos MEC:
            if (networkScenario.isMecSwitch(deviceId.toString())) {
                // Se construye una regla de flujo con el selector y el tratamiento de tráfico definidos anteriormente.
                // Se establece una prioridad muy alta: 40000.
                // La regla de flujo se instalará en esos conmutadores de los nodos MEC.
                FlowRule flowRule = DefaultFlowRule.builder()
                .forDevice(deviceId)
                .fromApp(appId)
                .withPriority(40000)
                .withSelector(selector)
                .withTreatment(treatment)
                .makePermanent()
                .build();

                // Se envía la regla del flujo al servicio de reglas de flujo de ONOS (FlowRuleService).
                flowRuleService.applyFlowRules(flowRule);
            }
        }
    }

    /**
     * Llama a la API REST del Coordinador de la red para comprobar si la dirección IP asociada a un contenido
     * solicitado por un cliente se corresponde con un contenido que sirve la red.
     * @param ip_addr - Dirección IP asociada al contenido solicitado por un cliente.
     * @return Boolean con el resultado de la comprobación: true en caso afirmativo y false en caso negativo.
     */
    private boolean checkContentIsServed(String ip_addr) {
        // Variable para almacenar el resultado de la comprobación.
        boolean check = false;
        // Se define la URL base donde está escuchando la API REST del Coordinador para comprobar si el contenido se sirve por la red.
        String coordinatorEndPoint = "http://coordinador.pruebasgiros.home.arpa:5000/coordinator/api/is_content_served";
        // Se define el parámetro con la dirección IP asociada al contenido a comprobar.
        String ipAddrParameter = "?ip_addr="+ip_addr;
        // Con la URL base y los parámetros, se construye la URL completa para realizar la llamada HTTP GET a la API REST del Coordinador.
        String urlString = coordinatorEndPoint+ipAddrParameter;
        // Cadena de caracteres para almacenar el resultado de la respuesta.
        String responseBody = "";
        try {
            // Se construye el objeto URL a partir de la URL completa.
            URL url = new URL(urlString);
            /**
             * Se abre la conexión a partir del objeto URL, se establecen las
             * propiedades de la conexión (método GET y respuesta esperada en texto plano) y se espera
             * el cuerpo de la respuesta, el cual se escribe línea a línea en la cadena de caracteres
             * anterior si la respuesta al HTTP GET devuelve el código 200 OK.
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("accept", "text/plain");
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
         * Se comprueba el cuerpo de la respuesta.
         * Si el resultado devuelto es "served", el contenido se sirve y se devolverá true.
         * Si el resultado devuelto es "not_served", el contenido no se sirve y se devolverá false.
         */
        if (responseBody.contains("served")) {
            check = true;
        }
        if (responseBody.contains("not_served")) {
            check = false;
        }
        return check;
    }

    /**
     * Envía un paquete por el puerto FLOOD definido en la especificación de OpenFlow.
     * Este método auxiliar se empleará, típicamente, para tráfico broadcast.
     * @param context - Contexto de paquetes de tráfico en ONOS.
     * Tomado del ejemplo de programación de ONOS accesible en la siguiente referencia:
     * [1]: https://wiki.onosproject.org/display/ONOS11/Application+tutorial#Applicationtutorial-Writingtheapplication
     */
    private void flood(PacketContext context) {
        log.info("[Orchestrator][Reactive packet processing] Flooding packet");
        // Si se permite tráfico broadcast en el dispositivo que envió el paquete (punto de conexión):
        if (topologyService.isBroadcastPoint(topologyService.currentTopology(), context.inPacket().receivedFrom())) {
            // Se configura el tratamiento del tráfico para sacar el tráfico por el puerto FLOOD (OpenFlow).
            context.treatmentBuilder().setOutput(PortNumber.FLOOD);
            // Se envía el tráfico asociado al contexto.
            context.send();
        }
        // En caso de que no se permita:
        else {
            // Se bloquea el contexto.
            context.block();
        }
    }

    /**
     * Envía un paquete al Host destino que se especifica como parámetro.
     * @param packet - Paquete a enviar al destino.
     * @param dst - Host destino del paquete.
     * Tomado del ejemplo de programación de ONOS accesible en la siguiente referencia:
     * [1]: https://wiki.onosproject.org/display/ONOS11/Application+tutorial#Applicationtutorial-Writingtheapplication
     */
    private void forwardPacketToDst(ByteBuffer packet, Host dst) {
        log.info("[Orchestrator][Reactive packet processing] Forwarding packet to destination "+dst.toString());
        // Se construye el tratamiento del tráfico, que consiste en enviar el tráfico por el puerto al que está conectado el destino.
        TrafficTreatment treatment = DefaultTrafficTreatment.builder().setOutput(dst.location().port()).build();
        // Se construye el paquete de salida con el conmutador al que está conectado el destino, el tratamiento de tráfico y el paquete a reenviar.
        OutboundPacket outPacket = new DefaultOutboundPacket(dst.location().deviceId(), treatment, packet);
        // Se emite el paquete.
        packetService.emit(outPacket);
    }

}
