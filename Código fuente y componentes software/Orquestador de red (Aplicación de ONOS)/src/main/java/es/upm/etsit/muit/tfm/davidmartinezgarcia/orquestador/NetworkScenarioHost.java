package es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador;

/**
 * Clase NetworkScenarioHost.
 * Representa un host en el escenario de red.
 * @author David Martínez García
 */
public class NetworkScenarioHost {
    
    /**
     * Nombre de host.
     */
    private String hostname;

    /**
     * Localización: nodo al que se encuentra conectado.
     */
    private String location;

    /**
     * Dirección IP.
     */
    private String ipAddress;

    /**
     * Dirección Ethernet/MAC.
     */
    private String ethAddress;

    /**
     * Constructor público. Instancia un objeto con los atributos especificados.
     * @param hostname - Nombre de host.
     * @param location - Localización (nodo) al que se encuentra conectado.
     * @param ipAddress - Dirección IP.
     * @param ethAddress - Dirección Ethernet/MAC.
     */
    public NetworkScenarioHost(String hostname, String location, String ipAddress, String ethAddress) {
        this.hostname = hostname;
        this.location = location;
        this.ipAddress = ipAddress;
        this.ethAddress = ethAddress;
    }

    /**
     * Devuelve el nombre de host.
     * @return String con el hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Devuelve la localización (nodo) al que se encuentra conectado.
     * @return String con la localización.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Devuelve la dirección IP.
     * @return String con la dirección IP.
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Devuelve la dirección Ethernet/MAC.
     * @return String con la dirección Ethernet/MAC.
     */
    public String getEthAddress() {
        return ethAddress;
    }

    /**
     * Establece el nombre de host.
     * @param hostname - Nuevo hostname.
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * Establece la localización (nodo) al que se encuentra conectado.
     * @param mecLocation - Nueva localización.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Establece la dirección IP.
     * @param ipAddress - Nueva dirección IP.
     */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /**
     * Establece la dirección Ethernet.
     * @param ethAddress - Nueva dirección Ethernet.
     */
    public void setEthAddress(String ethAddress) {
        this.ethAddress = ethAddress;
    }

}
