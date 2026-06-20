package org.services;

import com.google.gson.Gson;
import org.domain.CasinoPlayer;
import org.domain.Game;
import org.requests.Request;
import org.requests.serverside.*;
import org.utility.Connectivity;
import org.utility.DistributedServerConfiguration;
import org.utility.GetIPAddress;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.utility.Pair;

/**
 * The type Worker.
 */
public class Worker extends Thread {

    private final String host;
    private final int port;
    private final String RNG_IP;
    private final int RNG_PORT;
    private final String reducerIP;
    private final int reducerPORT;
    private final String secret;
    private final Gson gson = new Gson();

    /**
     * The Casino players.
     */
    final HashMap<String, CasinoPlayer> casinoPlayers = new HashMap<>();
    /**
     * The Casino games.
     */
    final HashMap<String, Game> casinoGames = new HashMap<>();
    /**
     * The Player bet history.
     */
    final HashMap<String, List<Object[]>> playerBetHistory = new HashMap<>();

    private final List<Pair<String, Integer>> allWorkers;


    /**
     * Instantiates a new Worker.
     *
     * @param host        the host
     * @param port        the port
     * @param RNG_IP      the rng ip
     * @param RNG_Port    the rng port
     * @param reducerIP   the reducer ip
     * @param reducerPort the reducer port
     * @param secret      the secret
     * @param allWorkers  the all workers
     */
    public Worker(String host, int port,
                  String RNG_IP, int RNG_Port,
                  String reducerIP, int reducerPort,
                  String secret,
                  List<Pair<String, Integer>> allWorkers) {
        this.host = host;
        this.port = port;
        this.RNG_IP = RNG_IP;
        this.RNG_PORT = RNG_Port;
        this.reducerIP = reducerIP;
        this.reducerPORT = reducerPort;
        this.secret = secret;
        this.allWorkers = allWorkers;
    }


    public void run() {
        System.out.println("[Worker] Started on " + host + ":" + port);
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                Socket socket = serverSocket.accept(); //perimenei sundesh apo master
                new RequestHandler(socket).start(); //neo thread ana aithma
            }
        } catch (IOException e) {
            throw new RuntimeException("[Worker] Failed on port " + port, e);
        }
    }

    /**
     * The type Request handler.
     */
    class RequestHandler extends Thread {
        private final Socket connectionSocket;

        /**
         * Instantiates a new Request handler.
         *
         * @param socket the socket
         */
        RequestHandler(Socket socket) {
            this.connectionSocket = socket;
        }

        public void run() {
            String data = Connectivity.receiveStringData(connectionSocket);
            if (data == null || data.isEmpty()) {
                System.out.println("[Worker] Empty request, closing connection");
                try { connectionSocket.close(); } catch (IOException e) {}
                return;
            }

            RequestShell requestShell = gson.fromJson(data, RequestShell.class);
            System.out.println("[Worker] Received request type: " + requestShell.getRequestType());
            boolean forwardToReducer = false;

            switch (requestShell.getRequestType()) {
                case PING:
                    return;
                case ADD_GAME: {
                    System.out.println("[Worker] Handling ADD_GAME" +
                            (requestShell.isReplica() ? " [REPLICA]" : ""));
                    AddGameRequest req = requestShell.getAddGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);

                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));

                    if (!requestShell.isReplica()) {
                        String gameName = req.getGameToAdd().getGameName();
                        replicateToBackup(requestShell, gameName);
                    }
                    break;
                }
                case REMOVE_GAME: {
                    System.out.println("[Worker] Handling REMOVE_GAME" +
                            (requestShell.isReplica() ? " [REPLICA]" : ""));
                    RemoveGameRequest req = requestShell.getRemoveGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));

                    if (!requestShell.isReplica()) {
                        String gameName = req.getGameName();
                        replicateToBackup(requestShell, gameName);
                    }
                    break;
                }
                case REACTIVATE_GAME: {
                    System.out.println("[Worker] Handling REACTIVATE_GAME" +
                            (requestShell.isReplica() ? " [REPLICA]" : ""));
                    ReActivateGameRequest req = requestShell.getReActivateGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));

                    if (!requestShell.isReplica()) {
                        String gameName = req.getGameName();
                        replicateToBackup(requestShell, gameName);
                    }
                    break;
                }
                case MODIFY_GAME: {
                    System.out.println("[Worker] Handling MODIFY_GAME" +
                            (requestShell.isReplica() ? " [REPLICA]" : ""));
                    ModifyGameRequest req = requestShell.getModifyGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));

                    if (!requestShell.isReplica()) {
                        String gameName = req.getGameName();
                        replicateToBackup(requestShell, gameName);
                    }
                    break;
                }
                case RATE_GAME: {
                    System.out.println("[Worker] Handling RATE_GAME" +
                            (requestShell.isReplica() ? " [REPLICA]" : ""));
                    RateGameRequest req = requestShell.getRateGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));

                    if (!requestShell.isReplica()) {
                        String gameName = req.getGameName();
                        replicateToBackup(requestShell, gameName);
                    }
                    break;
                }
                case GET_GAME: {
                    System.out.println("[Worker] Handling GET_GAME");
                    GetGameRequest req = requestShell.getGetGameRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));
                    break;
                }
                case BET_GAME: {
                    BetGameRequest req = requestShell.getBetGameRequest();
                    System.out.println("[Worker] Handling BET_GAME for player '" + req.getPlayerId() +
                            "' on game '" + req.getGameName() + "' amount " + req.getBetAmount());

                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    System.out.println("[Worker] BET_GAME processed. Status=" + req.getStatus() +
                            ", profit=" + req.getPlayerProfit() + ", winnings=" + req.getWinnings());

                    if (req.getStatus() == Request.RequestStatus.OK) {
                        synchronized (playerBetHistory) {
                            List<Object[]> history = playerBetHistory.get(req.getPlayerId());
                            if (history == null) {
                                history = new ArrayList<>();
                                playerBetHistory.put(req.getPlayerId(), history);
                            }
                            history.add(new Object[]{req.getGameName(), req.getPlayerProfit()});
                        }
                    } else {
                        System.out.println("[Worker] BET_GAME ended with ERROR: " + req.getErrorMessage());
                    }

                    System.out.println("[Worker] Sending BET_GAME response back to Master");
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));
                    break;
                }
                case GET_GAMES: {
                    System.out.println("[Worker] Handling GET_GAMES (MapReduce)");
                    GetGamesRequest req = requestShell.getGetGamesRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    forwardToReducer = true;
                    break;
                }
                case STATS_BY_PROVIDER: {
                    System.out.println("[Worker] Handling STATS_BY_PROVIDER (MapReduce)");
                    StatsProviderRequest req = requestShell.getStatsProviderRequest();
                    req.processRequest(casinoPlayers, casinoGames, RNG_IP, RNG_PORT, secret);
                    forwardToReducer = true;
                    break;
                }
                case STATS_BY_PLAYER: {
                    System.out.println("[Worker] Handling STATS_BY_PLAYER (MapReduce)");
                    StatsPlayerRequest req = requestShell.getStatsPlayerRequest();
                    double total = 0;
                    synchronized (playerBetHistory) {
                        List<Object[]> history = playerBetHistory.get(req.getPlayerId());
                        if (history != null) {
                            for (Object[] entry : history) {
                                String gameName = (String) entry[0];
                                double profit = ((Number) entry[1]).doubleValue();
                                total += profit;
                                req.getGameBreakdown().merge(gameName, profit, Double::sum);
                            }
                        }
                    }
                    req.setTotalProfitLoss(total);
                    forwardToReducer = true;
                    break;
                }
                case REPLICATE_BET_RESULT: {
                    System.out.println("[Worker] Handling REPLICATE_BET_RESULT");
                    ReplicateBetResultRequest req = requestShell.getReplicateBetResultRequest();
                    req.processRequest(casinoPlayers, casinoGames, playerBetHistory);
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));
                    break;
                }
                default: {
                    System.err.println("[Worker] Unknown request type: " + requestShell.getRequestType());
                    Connectivity.sendStringData(connectionSocket, gson.toJson(requestShell));
                    break;
                }
            }

            if (forwardToReducer) {
                try {
                    System.out.println("[Worker] Forwarding result to Reducer at " + reducerIP + ":" + reducerPORT);
                    Socket reducerSocket = new Socket(reducerIP, reducerPORT);
                    Connectivity.sendStringData(reducerSocket, gson.toJson(requestShell));
                    reducerSocket.close();
                } catch (IOException e) {
                    System.err.println("[Worker] Error forwarding to Reducer: " + e.getMessage());
                }
            }

            try {
                connectionSocket.close();
            } catch (IOException e) {
                System.err.println("[Worker] Error closing connection socket: " + e.getMessage());
            }
        }

    }

    private void replicateToBackup(RequestShell shell, String gameName) {
        int thisIdx = getThisWorkerIndex();
        if (thisIdx == -1) return;

        int primaryIdx = getPrimaryWorkerIndex(gameName);
        int backupIdx = getBackupWorkerIndex(gameName);

        if (thisIdx != primaryIdx || primaryIdx == backupIdx) {
            return;
        }

        Pair<String, Integer> backup = allWorkers.get(backupIdx);
        try {
            String originalJson = gson.toJson(shell);
            RequestShell replicaShell = gson.fromJson(originalJson, RequestShell.class);
            replicaShell.setReplica(true);

            String json = gson.toJson(replicaShell);
            System.out.println("[Worker] Replicating to backup Worker-" + backupIdx +
                    " (" + backup.getFirst() + ":" + backup.getSecond() + ")");

            Socket s = Connectivity.connect(backup.getFirst(), backup.getSecond());
            Connectivity.sendStringData(s, json);
            s.close();
        } catch (Exception e) {
            System.err.println("[Worker] Failed to replicate to backup: " + e.getMessage());
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: Worker <port> <config file>");
            return;
        }

        DistributedServerConfiguration configuration;
        int port;
        try {
            port = Integer.parseInt(args[0]);
            String configFile = args[1];
            configuration = DistributedServerConfiguration.getDistributedServerConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + e.getMessage());
        }

        String hostInfo = GetIPAddress.getPublicIPAddress() + " / " + GetIPAddress.getLocalIPAddress();
        String rngIP = configuration.getRNG().getFirst();
        int rngPort = configuration.getRNG().getSecond();
        String reducerIP = configuration.getReducer().getFirst();
        int reducerPort = configuration.getReducer().getSecond();
        String secret = configuration.getSecret();

        List<Pair<String, Integer>> workers = configuration.getWorkers();

        Worker worker = new Worker(hostInfo, port, rngIP, rngPort, reducerIP, reducerPort, secret, workers);
        worker.start();

        System.out.println("Worker started on the following specs:\n" +
                "Public IP: " + GetIPAddress.getPublicIPAddress() + "\n" +
                "Network IP: " + GetIPAddress.getLocalIPAddress() + "\n" +
                "Listening port: " + port);
    }

    // index tou trexontos worker sth list allWorkers (me vash to port)
    private int getThisWorkerIndex() {
        for (int i = 0; i < allWorkers.size(); i++) {
            if (allWorkers.get(i).getSecond() == port) {
                return i;
            }
        }
        return -1;
    }

    private int getPrimaryWorkerIndex(String gameName) {
        return Math.abs(gameName.hashCode()) % allWorkers.size();
    }

    private int getBackupWorkerIndex(String gameName) {
        int primary = getPrimaryWorkerIndex(gameName);
        if (allWorkers.size() == 1) return primary;
        return (primary + 1) % allWorkers.size();
    }

    private Pair<String, Integer> getBackupWorker(String gameName) {
        int backupIdx = getBackupWorkerIndex(gameName);
        return allWorkers.get(backupIdx);
    }
}
