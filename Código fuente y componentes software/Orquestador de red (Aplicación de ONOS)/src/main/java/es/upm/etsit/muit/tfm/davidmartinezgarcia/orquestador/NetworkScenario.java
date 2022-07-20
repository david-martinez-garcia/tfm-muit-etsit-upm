package es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador;

// Importación de paquetes y clases requeridas.

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Representación del escenario de red de VNX.
 * Esta clase tiene información sobre los hosts y switches en el escenario de red para
 * su gestión dentro del contexto de la aplicación.
 * @author David Martínez García
 */
public class NetworkScenario {

    // Switch del nodo MEC 1:

    public static final String sw_mec1_name = "sw_mec1";
    public static final String sw_mec1_id = "of:0000000000000001";
    public static final String sw_mec1_hw_addr = "00:00:00:00:00:01";
    public static final String sw_mec1_location = "mec1";

    // Switch del nodo MEC 2:

    public static final String sw_mec2_name = "sw_mec2";
    public static final String sw_mec2_id = "of:0000000000000002";
    public static final String sw_mec2_hw_addr = "00:00:00:00:00:02";
    public static final String sw_mec2_location = "mec2";

    // Switch de interconexión:

    public static final String sw_itc_name = "sw_itc";
    public static final String sw_itc_id = "of:0000000000000003";
    public static final String sw_itc_hw_addr = "00:00:00:00:00:03";
    public static final String sw_itc_location = "none";

    // Switch del nodo central:

    public static final String sw_central_name = "sw_central";
    public static final String sw_central_id = "of:0000000000000004";
    public static final String sw_central_hw_addr = "00:00:00:00:00:04";
    public static final String sw_central_location = "central";

    // Router:
    
    public static final String router_hostname = "router";
    public static final String router_location = "central";
    public static final String router_ip_addr = "10.0.0.1";
    public static final String router_eth_addr = "00:00:00:FF:00:01";

    // Controlador ONOS:

    public static final String onos_hostname = "onos";
    public static final String onos_location = "central";
    public static final String onos_ip_addr = "10.0.0.50";
    public static final String onos_eth_addr = "00:00:00:00:05:00";

    // Coordinador:

    public static final String coordinador_hostname = "coordinador";
    public static final String coordinador_location = "central";
    public static final String coordinador_ip_addr = "10.0.0.40";
    public static final String coordinador_eth_addr = "00:00:00:00:04:00";

    // Servidor de DNS (nodo central):

    public static final String dns_central_hostname = "dns-central";
    public static final String dns_central_location = "central";
    public static final String dns_central_ip_addr = "10.0.0.60";
    public static final String dns_central_eth_addr = "00:00:00:00:06:00";

    // Servidor de DNS (nodo mec1, para escenarios con redirección por DNS):

    public static final String dns_mec1_hostname = "dns-mec1";
    public static final String dns_mec1_location = "mec1";
    public static final String dns_mec1_ip_addr = "10.0.0.61";
    public static final String dns_mec1_eth_addr = "00:00:00:00:06:01";

    // Servidor de DNS (nodo mec2, para escenarios con redirección por DNS):

    public static final String dns_mec2_hostname = "dns-mec2";
    public static final String dns_mec2_location = "mec2";
    public static final String dns_mec2_ip_addr = "10.0.0.62";
    public static final String dns_mec2_eth_addr = "00:00:00:00:06:02";

    // Contenidos - Representación y direcciones flotantes para el acceso a los contenidos:

    public static final String contenidos_hostname = "contenidos";
    public static final String contenidos_location = "central";
    public static final String contenidos_ip_addr = "10.0.0.254";
    public static final String contenidos_eth_addr = "00:00:00:FF:00:01";

    // Cliente 1:

    public static final String cliente1_hostname = "cliente1";
    public static final String cliente1_location = "mec1";
    public static final String cliente1_ip_addr = "10.0.0.11";
    public static final String cliente1_eth_addr = "00:00:00:00:01:01";

    // Cliente 2:

    public static final String cliente2_hostname = "cliente2";
    public static final String cliente2_location = "mec2";
    public static final String cliente2_eth_addr = "00:00:00:00:01:02";
    public static final String cliente2_ip_addr = "10.0.0.12";

    // vProxy 1:

    public static final String vProxy1_hostname = "vProxy1";
    public static final String vProxy1_location = "mec1";
    public static final String vProxy1_ip_addr = "10.0.0.21";
    public static final String vProxy1_eth_addr = "00:00:00:00:02:01";

    // vProxy 2:

    public static final String vProxy2_hostname = "vProxy2";
    public static final String vProxy2_location = "mec2";
    public static final String vProxy2_ip_addr = "10.0.0.22";
    public static final String vProxy2_eth_addr = "00:00:00:00:02:02";

    // vCache 1:

    public static final String vCache1_hostname = "vCache1";
    public static final String vCache1_location = "mec1";
    public static final String vCache1_ip_addr = "10.0.0.31";
    public static final String vCache1_eth_addr = "00:00:00:00:03:01";

    // vCache 2:
    
    public static final String vCache2_hostname = "vCache2";
    public static final String vCache2_location = "mec2";
    public static final String vCache2_ip_addr = "10.0.0.32";
    public static final String vCache2_eth_addr = "00:00:00:00:03:02";

    // vCache 3:

    public static final String vCache3_hostname = "vCache3";
    public static final String vCache3_location = "central";
    public static final String vCache3_ip_addr = "10.0.0.33";
    public static final String vCache3_eth_addr = "00:00:00:00:03:03";

    /**
     * Map con la asociación de direcciones IP y objeto NetworkScenarioHost.
     * Almacena el registro de los hosts en el escenario de red.
     */
    private Map<String, NetworkScenarioHost> hosts;

    /**
     * Map con la asociación de identificador y objeto NetworkScenarioSwitch.
     * Almacena el registro de dispositivos de red (switches/conmutadores) en el escenario de red.
     */
    private Map<String, NetworkScenarioSwitch> switches;

    /**
     * Instancia de NetworkScenario para patrón Singleton.
     * Permite asegurar que sólo existe una instancia de esta clase y, por tanto, de información
     * sobre el escenario de red.
     */
    private static NetworkScenario instance;

    /**
     * Constructor privado.
     * Inicializa los Map de hosts y switches del escenario de red.
     */
    private NetworkScenario () {
        this.hosts = new LinkedHashMap<String, NetworkScenarioHost>();
        this.switches = new LinkedHashMap<String, NetworkScenarioSwitch>();
        populateHostsMap();
        populateSwitchesMap();
    }

    /**
     * Devuelve la instancia de NetworkScenario, para patrón Singleton.
     * @return Instancia única de NetworkScenario.
     */
    public static NetworkScenario getInstance() {
        if (instance == null) {
            instance = new NetworkScenario();
        }
        return instance;
    }

    /**
     * Establece el Map de hosts en el escenario de red con el Map que se pasa como parámetro.
     * @param hosts - Map de hosts en el escenario. Típicamente, será un Map nuevo.
     */
    public void setHostsMap (Map<String, NetworkScenarioHost> hosts) {
        this.hosts = hosts;
    }

    /**
     * Establece el Map de switches en el escenario de red con el Map que se pasa como parámetro.
     * @param hosts - Map de switches en el escenario de red. Típicamente, será un Map nuevo.
     */
    public void setSwitchesMap (Map<String, NetworkScenarioSwitch> switches) {
        this.switches = switches;
    }

    /**
     * Devuelve el Map actual de hosts en el escenario de red.
     * @return Objeto de tipo Map con los hosts del escenario de red.
     */
    public Map<String, NetworkScenarioHost> getHostsMap () {
        return this.hosts;
    }

    /**
     * Devuelve el Map actual de switches en el escenario de red.
     * @return Objeto de tipo Map con los switches en el escenario de red.
     */
    public Map<String, NetworkScenarioSwitch> getSwitchesMap () {
        return this.switches;
    }

    /**
     * Puebla el Map de hosts a partir de la información sobre los mismos.
     */
    public void populateHostsMap() {
        NetworkScenarioHost cliente1 = new NetworkScenarioHost(cliente1_hostname, cliente1_location, cliente1_ip_addr, cliente1_eth_addr);
        NetworkScenarioHost cliente2 = new NetworkScenarioHost(cliente2_hostname, cliente2_location, cliente2_ip_addr, cliente2_eth_addr);
        NetworkScenarioHost vProxy1 = new NetworkScenarioHost(vProxy1_hostname, vProxy1_location, vProxy1_ip_addr, vProxy1_eth_addr);
        NetworkScenarioHost vProxy2 = new NetworkScenarioHost(vProxy2_hostname, vProxy2_location, vProxy2_ip_addr, vProxy2_eth_addr);
        NetworkScenarioHost vCache1 = new NetworkScenarioHost(vCache1_hostname, vCache1_location, vCache1_ip_addr, vCache1_eth_addr);
        NetworkScenarioHost vCache2 = new NetworkScenarioHost(vCache2_hostname, vCache2_location, vCache2_ip_addr, vCache2_eth_addr);
        NetworkScenarioHost vCache3 = new NetworkScenarioHost(vCache3_hostname, vCache3_location, vCache3_ip_addr, vCache3_eth_addr);
        NetworkScenarioHost router = new NetworkScenarioHost(router_hostname, router_location, router_ip_addr, router_eth_addr);
        NetworkScenarioHost onos = new NetworkScenarioHost(onos_hostname, onos_location, onos_ip_addr, onos_eth_addr);
        NetworkScenarioHost coordinador = new NetworkScenarioHost(coordinador_hostname, coordinador_location, coordinador_ip_addr, coordinador_eth_addr);
        NetworkScenarioHost dns_central = new NetworkScenarioHost(dns_central_hostname, dns_central_location, dns_central_ip_addr, dns_central_eth_addr);
        NetworkScenarioHost dns_mec1 = new NetworkScenarioHost(dns_mec1_hostname, dns_mec1_location, dns_mec1_ip_addr, dns_mec1_eth_addr);
        NetworkScenarioHost dns_mec2 = new NetworkScenarioHost(dns_mec2_hostname, dns_mec2_location, dns_mec2_ip_addr, dns_mec2_eth_addr);
        NetworkScenarioHost contenidos = new NetworkScenarioHost(contenidos_hostname, contenidos_location, contenidos_ip_addr, contenidos_eth_addr);

        this.hosts.put(cliente1_ip_addr, cliente1);
        this.hosts.put(cliente2_ip_addr, cliente2);
        this.hosts.put(vProxy1_ip_addr, vProxy1);
        this.hosts.put(vProxy2_ip_addr, vProxy2);
        this.hosts.put(vCache1_ip_addr, vCache1);
        this.hosts.put(vCache2_ip_addr, vCache2);
        this.hosts.put(vCache3_ip_addr, vCache3);
        this.hosts.put(router_ip_addr, router);
        this.hosts.put(onos_ip_addr, onos);
        this.hosts.put(coordinador_ip_addr, coordinador);
        this.hosts.put(dns_central_ip_addr, dns_central);
        this.hosts.put(dns_mec1_ip_addr, dns_mec1);
        this.hosts.put(dns_mec2_ip_addr, dns_mec2);
        this.hosts.put(contenidos_ip_addr, contenidos);
    }

    /**
     * Puebla el mapa de switches a partir de la información sobre los mismos.
     */
    public void populateSwitchesMap() {
        NetworkScenarioSwitch sw_mec1 = new NetworkScenarioSwitch(sw_mec1_name, sw_mec1_id, sw_mec1_hw_addr, sw_mec1_location);
        NetworkScenarioSwitch sw_mec2 = new NetworkScenarioSwitch(sw_mec2_name, sw_mec2_id, sw_mec2_hw_addr, sw_mec2_location);
        NetworkScenarioSwitch sw_itc = new NetworkScenarioSwitch(sw_itc_name, sw_itc_id, sw_itc_hw_addr, sw_itc_location);
        NetworkScenarioSwitch sw_central = new NetworkScenarioSwitch(sw_central_name, sw_central_id, sw_central_hw_addr, sw_central_location);

        this.switches.put(sw_mec1_id, sw_mec1);
        this.switches.put(sw_mec2_id, sw_mec2);
        this.switches.put(sw_itc_id, sw_itc);
        this.switches.put(sw_central_id, sw_central);
    }

    /**
     * Comprueba si el identificador de dispositivo (Datapath ID) que se pasa como parámetro se corresponde con el de un switch de un nodo MEC.
     * Devuelve un boolean con el resultado de la comprobación.
     * @param deviceId - Identificador de dispositivo a comprobar (Datapath ID).
     * @return Boolean con el resultado de la comprobación: true si corresponde y false en caso contrario.
     */
    public boolean isMecSwitch(String deviceId) {
        // Si el parámetro "deviceId" es null se lanza una excepción y se finaliza la invocación del método.
        if (deviceId == null) throw new IllegalArgumentException("deviceId");

        // Variable para almacenar el resultado de la comprobación.
        boolean check = false;

        // Se obtiene la referencia al switch (en la clase auxiliar) cuyo identificador (Datapath ID) se ha pasado como parámetro.
        NetworkScenarioSwitch sw = this.switches.get(deviceId);

        // Si el switch no existe y el objeto es null, directamente se devuelve false.
        if (sw == null) {
            return false;
        }
        // En caso contrario, si la localización del switch contiene la cadena "mec", es un switch de un nodo MEC y se devuelve true.
        else {
            if (sw.getLocation().toLowerCase().contains("mec")) {
                check = true;
            }
        }

        return check;
    }

    /**
     * Comprueba si la dirección IP que se pasa como parámetro se corresponde con la de un cliente.
     * Devuelve un boolean con el resultado de la comprobación.
     * @param ip - Dirección IP a comprobar.
     * @return Boolean con el resultado de la comprobación: true si corresponde y false en caso contrario.
     */
    public boolean isClient(String ip) {
        // Si el parámetro "ip" es null se lanza una excepción y se finaliza la invocación del método.
        if (ip == null) throw new IllegalArgumentException("ip");

        // Variable para almacenar el resultado de la comprobación.
        boolean check = false;

        // Se obtiene la referencia al host (en la clase auxiliar) cuya IP se ha pasado como parámetro.
        NetworkScenarioHost host = this.hosts.get(ip);

        // Si el host no existe y el objeto es null, directamente se devuelve false.
        if (host == null) {
            return false;
        }
        // En caso contrario, si el hostname del host contiene la cadena "cliente", es un cliente y se devuelve true.
        else {
            if (host.getHostname().toLowerCase().contains("cliente")) {
                check = true;
            }
        }

        return check;
    }

    /**
     * Comprueba si la dirección IP que se pasa como parámetro se corresponde con la de un proxy.
     * Devuelve un boolean con el resultado de la comprobación.
     * @param ip - Dirección IP a comprobar.
     * @return Boolean con el resultado de la comprobación: true si corresponde y false en caso contrario.
     */
    public boolean isProxy(String ip) {
        // Si el parámetro "ip" es null se lanza una excepción y se finaliza la invocación del método.
        if (ip == null) throw new IllegalArgumentException("ip");

        // Variable para almacenar el resultado de la comprobación. El valor por defecto es false.
        boolean check = false;

        // Se obtiene la referencia al host (en la clase auxiliar) cuya IP se ha pasado como parámetro.
        NetworkScenarioHost host = this.hosts.get(ip);

        // Si el host no existe y el objeto es null, directamente se devuelve false.
        if (host == null) {
            return false;
        }
        // En caso contrario, si el hostname del host contiene la cadena "proxy", es un proxy y se devuelve true.
        else {
            if (host.getHostname().toLowerCase().contains("proxy")) {
                check = true;
            }
        }
        
        return check;
    }

    /**
     * Devuelve la dirección MAC/Ethernet de un host cuya dirección IP se pasa como parámetro.
     * @param ip - Dirección IP del host.
     * @return String con la dirección MAC/Ethernet del host.
     */
    public String getMacFromIp(String ip) {
        // Si el parámetro "ip" es null se lanza una excepción y se finaliza la invocación del método.
        if (ip == null) throw new IllegalArgumentException("ip");

        // Se obtiene la referencia al host (en la clase auxiliar) cuya IP se ha pasado como parámetro.
        NetworkScenarioHost host = this.hosts.get(ip);

        // Si el host no existe (es null), se devuelve null.
        if (host == null) {
            return null;
        }
        // En caso contrario, se devuelve la dirección MAC/Ethernet.
        else {
            return host.getEthAddress();
        }
    }
    
}
