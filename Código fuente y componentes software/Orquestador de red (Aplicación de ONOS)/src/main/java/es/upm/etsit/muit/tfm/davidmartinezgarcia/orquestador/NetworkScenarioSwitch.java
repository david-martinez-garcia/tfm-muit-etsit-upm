package es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador;

/**
 * Clase NetworkScenarioSwitch.
 * Representa un dispositivo de red (switch o conmutador) en el escenario de red.
 * @author David Martínez García
 */
public class NetworkScenarioSwitch {

    /**
     * Nombre del dispositivo.
     */
    private String name;

    /**
     * Identificador del dispositivo.
     * Será el Datapath ID. Ejemplo: of:0000000000000001.
     */
    private String identifier;

    /**
     * Dirección fisica del dispositivo.
     */
    private String hardwareAddress;

    /**
     * Localización (nodo) en el que se encuentra el dispositivo.
     */
    private String location;

    /**
     * Constructor público. Instancia un objeto con los atributos especificados.
     * @param name - Nombre.
     * @param identifier - Identificador.
     * @param hardwareAddress - Dirección física.
     * @param location - Localización (nodo).
     */
    public NetworkScenarioSwitch(String name, String identifier, String hardwareAddress, String location) {
        this.name = name;
        this.identifier = identifier;
        this.hardwareAddress = hardwareAddress;
        this.location = location;
    }

    /**
     * Devuelve el nombre del dispositivo.
     * @return Cadena de caracteres con el nombre.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el identificador del dispositivo.
     * @return Cadena de caracteres con el identificador.
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Devuelve la dirección física del dispositivo.
     * @return Cadena de caracteres con la dirección física.
     */
    public String getHardwareAddress() {
        return hardwareAddress;
    }

    /**
     * Devuelve la localización (nodo) en el que se encuentra el dispositivo.
     * @return Cadena de caracteres con la localización (nodo).
     */
    public String getLocation() {
        return location;
    }

    /**
     * Establece el nombre del dispositivo.
     * @param name - Nuevo nombre.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Establece el identificador del dispositivo.
     * @param identifier - Nuevo identificador.
     */
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    /**
     * Establece la dirección física del dispositivo.
     * @param hardwareAddress - Nueva dirección física.
     */
    public void setHardwareAddress(String hardwareAddress) {
        this.hardwareAddress = hardwareAddress;
    }

    /**
     * Establece la localización (nodo) en el que se encuentra el dispositivo.
     * @param location - Nueva localización.
     */
    public void setLocation(String location) {
        this.location = location;
    }
    
}
