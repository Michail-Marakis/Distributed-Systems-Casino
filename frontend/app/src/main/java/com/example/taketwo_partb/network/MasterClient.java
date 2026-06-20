package com.example.taketwo_partb.network;

import com.example.taketwo_partb.protocol.RequestShell;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * The type Master client.
 */
public class MasterClient {

    private final String host;
    private final int port;
    private final Gson gson = new Gson();

    /**
     * Instantiates a new Master client.
     *
     * @param host the host
     * @param port the port
     */
    public MasterClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Send raw request string.
     *
     * @param json the json
     * @return the string
     * @throws Exception the exception
     */
    public String sendRawRequest(String json) throws Exception {
        Socket socket = new Socket(host, port);

        BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())
        );
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(socket.getInputStream())
        );

        writer.write(json);
        writer.newLine();
        writer.flush();

        String response = reader.readLine();

        reader.close();
        writer.close();
        socket.close();

        return response;
    }

    /**
     * Send object string.
     *
     * @param requestObj the request obj
     * @return the string
     * @throws Exception the exception
     */
    public String sendObject(Object requestObj) throws Exception {
        String json = gson.toJson(requestObj);
        return sendRawRequest(json);
    }

    /**
     * Send request shell.
     *
     * @param shell the shell
     * @return the request shell
     * @throws Exception the exception
     */
    public RequestShell send(RequestShell shell) throws Exception {
        String json = gson.toJson(shell);
        String responseJson = sendRawRequest(json);
        return gson.fromJson(responseJson, RequestShell.class);
    }
}