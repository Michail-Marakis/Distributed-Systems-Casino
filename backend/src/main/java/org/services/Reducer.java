package org.services;

import com.google.gson.Gson;
import org.requests.serverside.*;
import org.utility.Connectivity;
import org.utility.DistributedServerConfiguration;
import org.utility.GetIPAddress;
import org.utility.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * The type Reducer.
 */
public class Reducer extends Thread {

    private final Gson gson = new Gson();
    private final String masterIP;
    private final int masterPort;
    private final int reducerPort;
    private Integer workerCount;

    private static final long REQUEST_TIMEOUT_MS = 100000;

    private final HashMap<Integer, RequestShell> pendingRequests = new HashMap<>();
    private final HashMap<Integer, Long> firstArrivalTime = new HashMap<>();

    /**
     * Instantiates a new Reducer.
     *
     * @param masterIP    the master ip
     * @param masterPort  the master port
     * @param reducerPort the reducer port
     * @param workerCount the worker count
     */
    public Reducer(String masterIP, int masterPort, int reducerPort, int workerCount) {
        this.masterIP = masterIP;
        this.masterPort = masterPort;
        this.reducerPort = reducerPort;
        this.workerCount = workerCount;
    }

    public void run() {
        System.out.println("[Reducer] Started on port " + reducerPort + " (expecting " + workerCount + " workers)");

        new Thread(this::timeoutWatcher, "Reducer-TimeoutWatcher").start();

        try {
            ServerSocket serverSocket = new ServerSocket(reducerPort);
            while (true) {
                Socket socket = serverSocket.accept();
                new RequestHandler(socket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("[Reducer] Failed on port " + reducerPort, e);
        }
    }

    private void timeoutWatcher() {
        while (true) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }

            getWorkerState();

            long now = System.currentTimeMillis();
            RequestShell request;
            synchronized (pendingRequests) {
                HashMap<Integer, RequestShell> completed = new HashMap<>();
                for (Integer id : pendingRequests.keySet()) {
                    request = pendingRequests.get(id);
                    Long start = firstArrivalTime.get(id);
                    switch (request.getRequestType()) {
                        case GET_GAMES -> {
                            if (now - start >= REQUEST_TIMEOUT_MS || request.getGetGamesRequest().getPacketsMerged() >= workerCount ) {
                                completed.put(id, pendingRequests.get(id));
                            }
                        }
                        case STATS_BY_PROVIDER -> {
                            if (now - start >= REQUEST_TIMEOUT_MS || request.getStatsProviderRequest().getPacketsMerged() >= workerCount ) {
                                completed.put(id, pendingRequests.get(id));
                            }

                        }
                        case STATS_BY_PLAYER -> {
                            if (now - start >= REQUEST_TIMEOUT_MS || request.getStatsPlayerRequest().getPacketsMerged() >= workerCount ) {
                                completed.put(id, pendingRequests.get(id));
                            }

                        }
                    }
                }

                for (Integer id : completed.keySet()) {
                    RequestShell shell = pendingRequests.remove(id);
                    firstArrivalTime.remove(id);

                    if (shell != null) {
                        System.out.println("[Reducer] Timeout reached for request " + id +
                                ", forwarding partial aggregated result to Master");
                        forwardToMaster(shell);
                    }
                }
            }
        }
    }

    private void forwardToMaster(RequestShell resultShell) {
        try {
            Socket masterSocket = new Socket(masterIP, masterPort);
            Connectivity.sendStringData(masterSocket, gson.toJson(resultShell));
            masterSocket.close();
        } catch (Exception e) {
            System.err.println("[Reducer] Error forwarding to Master: " + e.getMessage());
        }
    }

    /**
     * The type Request handler.
     */
    class RequestHandler extends Thread {
        private final Socket requestSocket;

        /**
         * Instantiates a new Request handler.
         *
         * @param socket the socket
         */
        RequestHandler(Socket socket) {
            this.requestSocket = socket;
        }

        public void run() {
            String data;
            try {
                data = Connectivity.receiveStringData(requestSocket);
            } catch (Exception e) {
                try { requestSocket.close(); } catch (IOException ignored) {}
                return;
            }

            try {
                requestSocket.close();
            } catch (IOException ignored) {
            }

            if (data == null || data.isEmpty()) return;

            RequestShell incoming = gson.fromJson(data, RequestShell.class);
            boolean complete = false;
            RequestShell resultShell = null;

            switch (incoming.getRequestType()) {
                case GET_GAMES: {
                    int id = incoming.getGetGamesRequest().getIdentifier();
                    synchronized (pendingRequests) {
                        RequestShell existing = pendingRequests.get(id);
                        if (existing == null) {
                            pendingRequests.put(id, incoming);
                            firstArrivalTime.put(id, System.currentTimeMillis());
                        } else {
                            existing.getGetGamesRequest().merge(incoming.getGetGamesRequest());
                        }

                        RequestShell updated = pendingRequests.get(id);
                        if (updated != null &&
                                updated.getGetGamesRequest().getPacketsMerged() >= getWorkerCount()) {
                            resultShell = pendingRequests.remove(id);
                            firstArrivalTime.remove(id);
                            complete = true;
                        }
                    }
                    break;
                }

                case STATS_BY_PROVIDER: {
                    int id = incoming.getStatsProviderRequest().getIdentifier();
                    synchronized (pendingRequests) {
                        RequestShell existing = pendingRequests.get(id);
                        if (existing == null) {
                            pendingRequests.put(id, incoming);
                            firstArrivalTime.put(id, System.currentTimeMillis());
                        } else {
                            existing.getStatsProviderRequest().merge(incoming.getStatsProviderRequest());
                        }

                        RequestShell updated = pendingRequests.get(id);
                        if (updated != null &&
                                updated.getStatsProviderRequest().getPacketsMerged() >= getWorkerCount()) {
                            resultShell = pendingRequests.remove(id);
                            firstArrivalTime.remove(id);
                            complete = true;
                        }
                    }
                    break;
                }

                case STATS_BY_PLAYER: {
                    int id = incoming.getStatsPlayerRequest().getIdentifier();
                    synchronized (pendingRequests) {
                        RequestShell existing = pendingRequests.get(id);
                        if (existing == null) {
                            pendingRequests.put(id, incoming);
                            firstArrivalTime.put(id, System.currentTimeMillis());
                        } else {
                            existing.getStatsPlayerRequest().merge(incoming.getStatsPlayerRequest());
                        }

                        RequestShell updated = pendingRequests.get(id);
                        if (updated != null &&
                                updated.getStatsPlayerRequest().getPacketsMerged() >= getWorkerCount()) {
                            resultShell = pendingRequests.remove(id);
                            firstArrivalTime.remove(id);
                            complete = true;
                        }
                    }
                    break;
                }

                default: {
                    System.err.println("[Reducer] Unexpected type: " + incoming.getRequestType());
                    return;
                }
            }

            if (complete && resultShell != null) {
                forwardToMaster(resultShell);
            }
        }
    }

    private static DistributedServerConfiguration configuration;

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Reducer <port> <config file>");
            return;
        }

        int port;
        try {
            port = Integer.parseInt(args[0]);
            String configFile = args[1];
            configuration = DistributedServerConfiguration.getDistributedServerConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + e.getMessage());
        }

        String masterIP = configuration.getMasterServerSide().getFirst();
        int masterPort = configuration.getMasterServerSide().getSecond();
        int workerCount = configuration.getWorkers().size();

        Reducer reducer = new Reducer(masterIP, masterPort, port, workerCount);
        reducer.start();

        System.out.println("Reducer started on the following specs:\n" +
                "Public IP: " + GetIPAddress.getPublicIPAddress() + "\n" +
                "Network IP: " + GetIPAddress.getLocalIPAddress() + "\n" +
                "Listening port: " + port);
    }

    /**
     * Gets worker state.
     */
    synchronized void getWorkerState() {
        int count = 0;
        RequestShell shell;
        for(Pair<String,Integer> worker : configuration.getWorkers()) {
            try {
                Socket workerSocket = Connectivity.connect(worker.getFirst(), worker.getSecond());
                shell =new RequestShell(new PingRequest(0));
                Connectivity.sendStringData(workerSocket, gson.toJson(shell));
                workerSocket.close();
                count++;
            } catch (Exception e) {
                //Runtime error grabbing worker , ignore state
            }
        }
        synchronized (workerCount) {
            workerCount = count;
        }
        System.out.println("Worker count: " + workerCount);
    }

    /**
     * Gets worker count.
     *
     * @return the worker count
     */
    int getWorkerCount() {
        synchronized (workerCount) {
            return workerCount;
        }
    }





}