package com.example.taketwo_partb.network;

/**
 * The type Internal config.
 */
public class InternalConfig {

    static final private String BackendIp = "192.168.2.3";
    static final private int port = 3333;

    /**
     * Get backend ip string.
     *
     * @return the string
     */
    public static String getBackendIp(){
        return BackendIp;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public  static int getPort() {
        return port;
    }


}
