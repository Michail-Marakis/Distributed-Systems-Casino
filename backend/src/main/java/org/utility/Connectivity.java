package org.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * The type Connectivity.
 */
public class Connectivity {

    /**
     * Connect socket.
     *
     * @param host the host
     * @param port the port
     * @return the socket
     */
    public static Socket connect(String host, int port) {
        try {
            return new Socket(host, port);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Error connecting to " + host + ":" + port, e);
        } catch (IOException e) {
            throw new RuntimeException("Error binding a socket to: " + host + ":" + port, e);
        }
    }

    /**
     * Send string data.
     *
     * @param socket the socket
     * @param data   the data
     */
    public static void sendStringData(Socket socket, String data) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(data);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException("Error on data transfer to " + socket.getRemoteSocketAddress() + ".", e);
        }
    }

    /**
     * Receive string data string.
     *
     * @param socket the socket
     * @return the string
     */
    public static String receiveStringData(Socket socket) {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            return in.readLine();
        } catch (IOException e) {
            throw new RuntimeException("Error on data transfer from " + socket.getRemoteSocketAddress() + ".", e);
        }
    }
}
