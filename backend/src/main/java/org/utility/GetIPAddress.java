package org.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;

/**
 * The type Get ip address.
 */
public class GetIPAddress {

    /**
     * Gets local ip address.
     *
     * @return the local ip address
     */
//Method to get local IP address
    public static String getLocalIPAddress() {
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            return localHost.getHostAddress();
        } catch (UnknownHostException e) {
            return "Unable to determine local IP: " + e.getMessage();
        }
    }

    /**
     * Gets public ip address.
     *
     * @return the public ip address
     */
//Method to get public IP address (requires internet connection)
    public static String getPublicIPAddress() {
        String publicIPService = "https://api.ipify.org"; //public IP API
        try {
            URL url = new URL(publicIPService);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000); //5 seconds timeout
            conn.setReadTimeout(5000);

            try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                return in.readLine();
            }
        } catch (IOException e) {
            return "Unable to determine public IP: " + e.getMessage();
        }
    }

}
