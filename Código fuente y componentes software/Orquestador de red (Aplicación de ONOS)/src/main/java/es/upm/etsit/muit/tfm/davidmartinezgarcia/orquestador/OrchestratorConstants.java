package es.upm.etsit.muit.tfm.davidmartinezgarcia.orquestador;

/**
 * Constantes utilizadas por el Orquestador.
 * @author David Martínez García
 */
public final class OrchestratorConstants {

    /**
     * Constante utilizada para identificar que la aplicación se usa con redirección no especificada.
     * Valor por defecto.
     */
    public static final int UNSPECIFIED_REDIRECTION = 0;

    /**
     * Constante utilizada para identificar si la aplicación se usa con redirección de tráfico TCP/IPv4.
     * En este caso, se hace uso del procesador de tráfico ARP + TCP/IPv4.
     */
    public static final int TCP_IPV4_REDIRECTION = 1;

    /**
     * Constante utilizada para identificar si la aplicación se usa con redirección por DNS.
     * En este caso, se hace uso del procesador de tráfico ARP.
     */
    public static final int DNS_REDIRECTION = 2;
    
}
