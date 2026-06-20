package org.services;

import com.google.gson.Gson;
import org.domain.CasinoPlayer;
import org.requests.Request;
import org.requests.serverside.*;
import org.utility.Connectivity;
import org.utility.DistributedServerConfiguration;
import org.utility.Pair;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

/**
 * The type Master.
 */
public class Master extends Thread {

    private final DistributedServerConfiguration config;
    private final int workerCount;
    private final Gson gson = new Gson();
    private final HashMap<String, CasinoPlayer> players = new HashMap<>();
    private int requestIdGenerator = 1;
    private final HashMap<Integer, PendingRequest> pendingMapReduce = new HashMap<>();


    private static class PendingRequest {
        /**
         * The Client socket.
         */
        Socket clientSocket;
        /**
         * The Created at.
         */
        long createdAt;

        /**
         * Instantiates a new Pending request.
         *
         * @param socket the socket
         */
        PendingRequest(Socket socket) {
            this.clientSocket = socket;
            this.createdAt = System.currentTimeMillis();
        }
    }

    private synchronized int generateRequestId() {
        requestIdGenerator = requestIdGenerator % 1000000000;
        return requestIdGenerator++;
    }

    /**
     * Instantiates a new Master.
     *
     * @param configFile the config file
     */
    public Master(String configFile) {
        try {
            config = DistributedServerConfiguration.getDistributedServerConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Error loading config: " + configFile, e);
        }
        workerCount = config.getWorkers().size();
    }

    private int getPrimaryWorkerIndex(String gameName) {
        return Math.abs(gameName.hashCode()) % workerCount;
    }

    private int getBackupWorkerIndex(String gameName) {
        int primary = getPrimaryWorkerIndex(gameName);
        if (workerCount == 1) return primary;
        return (primary + 1) % workerCount;
    }

    private String sendToWorker(int workerIndex, String jsonData) {
        Pair<String, Integer> worker = config.getWorkers().get(workerIndex);
        try {
            Socket socket = Connectivity.connect(worker.getFirst(), worker.getSecond());
            Connectivity.sendStringData(socket, jsonData);
            String response = Connectivity.receiveStringData(socket);
            socket.close();
            return response;
        } catch (Exception e) {
            System.err.println("[Master] Worker-" + workerIndex + " unreachable: " + e.getMessage());
            throw new RuntimeException("Worker disabled", e);
        }
    }

    private String sendWithFailover(String gameName, String data) {
        int primaryIdx = getPrimaryWorkerIndex(gameName);
        int backupIdx = getBackupWorkerIndex(gameName);

        try {
            System.out.println("[Master] Sending to primary Worker-" + primaryIdx);
            return sendToWorker(primaryIdx, data);
        } catch (RuntimeException e) {
            System.err.println("[Master] Primary Worker-" + primaryIdx +
                    " failed, trying backup Worker-" + backupIdx);
            return sendToWorker(backupIdx, data);
        }
    }

    private void broadcastToWorkers(String jsonData) {
        for (int i = 0; i < workerCount; i++) {
            final int idx = i;
            new Thread(() -> {
                Pair<String, Integer> worker = config.getWorkers().get(idx);
                try {
                    Socket socket = Connectivity.connect(worker.getFirst(), worker.getSecond());
                    Connectivity.sendStringData(socket, jsonData);
                    socket.close();
                } catch (Exception e) {
                    System.err.println("[Master] Broadcast: Worker-" + idx +
                            " unreachable: " + e.getMessage());
                }
            }, "Master-Broadcast-" + i).start();
        }
    }



    private CasinoPlayer getOrCreatePlayer(String playerId) {
        synchronized (players) {
            CasinoPlayer p = players.get(playerId);
            if (p == null) {
                p = new CasinoPlayer(playerId, 0);
                players.put(playerId, p);
            }
            return p;
        }
    }

    private void initializeSystemServices() {
        System.out.println("[Master] Initializing services...");
        System.out.println("[Master] All services configured. Workers: " + workerCount);
    }

    @Override
    public void run() {
        initializeSystemServices();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }

        new Thread(this::listenServerSide, "Master-ServerSide").start();
        listenPublicSide();
    }

    private void listenPublicSide() {
        int port = config.getMasterPublicSide().getSecond();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[Master] Public side listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("[Master] Public side error on port " + port, e);
        }
    }

    private void listenServerSide() {
        int port = config.getMasterServerSide().getSecond();
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("[Master] Server side listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                new Thread(() -> handleReducerResult(socket), "Master-ReducerResult").start();
            }
        } catch (IOException e) {
            throw new RuntimeException("[Master] Server side error on port " + port, e);
        }
    }

    private void handleReducerResult(Socket reducerSocket) {
        String data;
        try {
            data = Connectivity.receiveStringData(reducerSocket);
        } catch (Exception e) {
            closeSocketQuietly(reducerSocket);
            return;
        }

        closeSocketQuietly(reducerSocket);

        if (data == null) return;

        RequestShell shell = gson.fromJson(data, RequestShell.class);
        int requestId = -1;

        switch (shell.getRequestType()) {
            case GET_GAMES:
                requestId = shell.getGetGamesRequest().getIdentifier();
                break;
            case STATS_BY_PROVIDER:
                requestId = shell.getStatsProviderRequest().getIdentifier();
                break;
            case STATS_BY_PLAYER:
                requestId = shell.getStatsPlayerRequest().getIdentifier();
                break;
            default:
                return;
        }

        PendingRequest pending;
        synchronized (pendingMapReduce) {
            pending = pendingMapReduce.remove(requestId);
        }

        if (pending != null) {
            try {
                Connectivity.sendStringData(pending.clientSocket, data);
            } catch (Exception ignored) {
            } finally {
                closeSocketQuietly(pending.clientSocket);
            }
        }
    }

    /**
     * The type Client handler.
     */
    class ClientHandler extends Thread {
        private final Socket clientSocket;

        /**
         * Instantiates a new Client handler.
         *
         * @param socket the socket
         */
        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            String data;
            try {
                data = Connectivity.receiveStringData(clientSocket);
            } catch (Exception e) {
                closeSocket(clientSocket);
                return;
            }

            if (data == null || data.isEmpty()) {
                closeSocket(clientSocket);
                return;
            }

            RequestShell shell = gson.fromJson(data, RequestShell.class);
            System.out.println("[Master] Received: " + shell.getRequestType());

            switch (shell.getRequestType()) {
                case ADD_GAME: {
                    String gameName = shell.getAddGameRequest().getGameToAdd().getGameName();
                    try {
                        String resp = sendWithFailover(gameName, data);
                        Connectivity.sendStringData(clientSocket, resp);
                    } catch (RuntimeException e) {
                        shell.getAddGameRequest().setStatus(Request.RequestStatus.ERROR);
                        shell.getAddGameRequest().setErrorMessage("No available worker for ADD_GAME.");
                        Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    }
                    closeSocket(clientSocket);
                    break;
                }

                case REMOVE_GAME: {
                    String gameName = shell.getRemoveGameRequest().getGameName();
                    try {
                        String resp = sendWithFailover(gameName, data);
                        Connectivity.sendStringData(clientSocket, resp);
                    } catch (RuntimeException e) {
                        shell.getRemoveGameRequest().setStatus(Request.RequestStatus.ERROR);
                        shell.getRemoveGameRequest().setErrorMessage("No available worker for REMOVE_GAME.");
                        Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    }
                    closeSocket(clientSocket);
                    break;
                }

                case REACTIVATE_GAME: {
                    String gameName = shell.getReActivateGameRequest().getGameName();
                    try {
                        String resp = sendWithFailover(gameName, data);
                        Connectivity.sendStringData(clientSocket, resp);
                    } catch (RuntimeException e) {
                        shell.getReActivateGameRequest().setStatus(Request.RequestStatus.ERROR);
                        shell.getReActivateGameRequest().setErrorMessage("No available worker for REACTIVATE_GAME.");
                        Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    }
                    closeSocket(clientSocket);
                    break;
                }

                case MODIFY_GAME: {
                    String gameName = shell.getModifyGameRequest().getGameName();
                    try {
                        String resp = sendWithFailover(gameName, data);
                        Connectivity.sendStringData(clientSocket, resp);
                    } catch (RuntimeException e) {
                        shell.getModifyGameRequest().setStatus(Request.RequestStatus.ERROR);
                        shell.getModifyGameRequest().setErrorMessage("No available worker for MODIFY_GAME.");
                        Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    }
                    closeSocket(clientSocket);
                    break;
                }

                case RATE_GAME: {
                    String gameName = shell.getRateGameRequest().getGameName();
                    try {
                        String resp = sendWithFailover(gameName, data);
                        Connectivity.sendStringData(clientSocket, resp);
                    } catch (RuntimeException e) {
                        shell.getRateGameRequest().setStatus(Request.RequestStatus.ERROR);
                        shell.getRateGameRequest().setErrorMessage("No available worker for RATE_GAME.");
                        Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    }
                    closeSocket(clientSocket);
                    break;
                }

                case BET_GAME: {
                    handleBet(shell, data);
                    break;
                }

                case ADD_BALANCE: {
                    AddBalanceRequest req = shell.getAddBalanceRequest();
                    CasinoPlayer player = getOrCreatePlayer(req.getPlayerId());

                    if (req.getAmount() <= 0) {
                        req.setStatus(Request.RequestStatus.ERROR);
                        req.setErrorMessage("Amount must be greater than 0");
                    } else {
                        player.addTokens(req.getAmount());
                        req.setStatus(Request.RequestStatus.OK);
                        req.setErrorMessage(String.format(
                                "Added %.2f FUN to %s. Balance: %.2f FUN",
                                req.getAmount(),
                                req.getPlayerId(),
                                player.getTokens()
                        ));
                    }

                    Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    closeSocket(clientSocket);
                    break;
                }

                case GET_PLAYER: {
                    GetPlayerRequest req = shell.getGetPlayerRequest();
                    CasinoPlayer player = getOrCreatePlayer(req.getUsername());
                    req.setUser(player);
                    req.setStatus(Request.RequestStatus.OK);

                    Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    closeSocket(clientSocket);
                    break;
                }

                case GET_GAMES: {
                    int requestId = generateRequestId();
                    shell.getGetGamesRequest().setIdentifier(requestId);

                    synchronized (pendingMapReduce) {
                        pendingMapReduce.put(requestId, new PendingRequest(clientSocket));
                    }

                    broadcastToWorkers(gson.toJson(shell));
                    break;
                }

                case STATS_BY_PROVIDER: {
                    int requestId = generateRequestId();
                    shell.getStatsProviderRequest().setIdentifier(requestId);

                    synchronized (pendingMapReduce) {
                        pendingMapReduce.put(requestId, new PendingRequest(clientSocket));
                    }

                    broadcastToWorkers(gson.toJson(shell));
                    break;
                }

                case STATS_BY_PLAYER: {
                    String pid = shell.getStatsPlayerRequest().getPlayerId();

                    synchronized (players) {
                        if (!players.containsKey(pid)) {
                            shell.getStatsPlayerRequest().setStatus(Request.RequestStatus.ERROR);
                            shell.getStatsPlayerRequest().setErrorMessage("Player '" + pid + "' not found.");
                            Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                            closeSocket(clientSocket);
                            break;
                        }
                    }

                    int requestId = generateRequestId();
                    shell.getStatsPlayerRequest().setIdentifier(requestId);

                    synchronized (pendingMapReduce) {
                        pendingMapReduce.put(requestId, new PendingRequest(clientSocket));
                    }

                    broadcastToWorkers(gson.toJson(shell));
                    break;
                }

                default: {
                    System.err.println("[Master] Unknown request type: " + shell.getRequestType());
                    closeSocket(clientSocket);
                    break;
                }
            }
        }

        private void handleBet(RequestShell shell, String data) {
            BetGameRequest req = shell.getBetGameRequest();
            String playerId = req.getPlayerId();
            float betAmount = req.getBetAmount();
            String gameName = req.getGameName();

            System.out.println("[Master] BET_GAME received from player '" + playerId +
                    "' on game '" + gameName + "' amount " + betAmount);

            CasinoPlayer player = getOrCreatePlayer(playerId);

            synchronized (player) {
                System.out.println("[Master] Current balance of '" + playerId + "': " + player.getTokens());

                if (player.getTokens() < betAmount) {
                    System.out.println("[Master] Insufficient balance, sending ERROR back to client");
                    req.setStatus(Request.RequestStatus.ERROR);
                    req.setErrorMessage(String.format(
                            "Insufficient balance. Current: %.2f FUN, Required: %.2f FUN",
                            player.getTokens(),
                            betAmount
                    ));
                    Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    closeSocket(clientSocket);
                    return;
                }

                System.out.println("[Master] Deducting bet " + betAmount + " from player '" + playerId + "'");
                player.deductTokens(betAmount);
            }

            int primaryIdx = getPrimaryWorkerIndex(gameName);
            int backupIdx = getBackupWorkerIndex(gameName);

            String resp;
            int activeIdx = primaryIdx;

            try {
                System.out.println("[Master] Forwarding BET_GAME for '" + gameName +
                        "' to primary Worker-" + primaryIdx);
                resp = sendToWorker(primaryIdx, data);
            } catch (RuntimeException e) {
                System.err.println("[Master] Primary Worker-" + primaryIdx +
                        " is down. Failing over to backup Worker-" + backupIdx);
                resp = sendToWorker(backupIdx, data);
                activeIdx = backupIdx;
            }

            if (resp == null || resp.isEmpty()) {
                synchronized (player) {
                    player.addTokens(betAmount);
                }
                req.setStatus(Request.RequestStatus.ERROR);
                req.setErrorMessage("No response from worker");
                Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                closeSocket(clientSocket);
                return;
            }

            System.out.println("[Master] Received BET_GAME response from Worker-" + activeIdx + ": " + resp);

            RequestShell respShell = gson.fromJson(resp, RequestShell.class);
            BetGameRequest respReq = respShell.getBetGameRequest();

            if (respReq != null && respReq.getStatus() == Request.RequestStatus.OK) {
                float winnings = (float) respReq.getWinnings();
                float profit = (float) respReq.getPlayerProfit();

                System.out.println("[Master] BET_GAME OK. Winnings=" + winnings + " Profit=" + profit);

                synchronized (player) {
                    player.addTokens(winnings);
                    player.addNetProfit(profit);
                    System.out.println("[Master] New balance of '" + playerId + "': " + player.getTokens());
                }

                respReq.setErrorMessage(String.format("Balance: %.2f FUN", player.getTokens()));

                int replicaIdx;
                if(activeIdx == primaryIdx){
                    replicaIdx = backupIdx;
                }else{
                    replicaIdx = primaryIdx;
                }

                if (replicaIdx != activeIdx && workerCount > 1) {
                    try {
                        System.out.println("[Master] Replicating BET result to Worker-" + replicaIdx);

                        ReplicateBetResultRequest replicateReq = new ReplicateBetResultRequest(
                                gameName,
                                playerId,
                                betAmount,
                                respReq.getMultiplier(),
                                respReq.getWinnings(),
                                respReq.getPlayerProfit()
                        );

                        RequestShell replicateShell = new RequestShell(replicateReq);
                        String replJson = gson.toJson(replicateShell);

                        sendToWorker(replicaIdx, replJson);
                    } catch (Exception e) {
                        System.err.println("[Master] Failed to replicate BET result: " + e.getMessage());
                    }
                }

            } else {
                String workerError;

                if(respReq != null){
                    workerError = respReq.getErrorMessage();
                }else{
                    workerError = "Unknown worker error";
                }
                System.out.println("[Master] BET_GAME ERROR from worker: " + workerError);

                synchronized (player) {
                    System.out.println("[Master] Refunding bet " + betAmount + " to '" + playerId + "'");
                    player.addTokens(betAmount);
                }

                if (respReq == null) {
                    req.setStatus(Request.RequestStatus.ERROR);
                    req.setErrorMessage("Invalid BET_GAME response from worker");
                    Connectivity.sendStringData(clientSocket, gson.toJson(shell));
                    closeSocket(clientSocket);
                    return;
                }
            }

            System.out.println("[Master] Sending BET_GAME response back to player '" + playerId + "'");
            Connectivity.sendStringData(clientSocket, gson.toJson(respShell));
            closeSocket(clientSocket);
        }

        private void closeSocket(Socket s) {
            try {
                s.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void closeSocketQuietly(Socket s) {
        try {
            s.close();
        } catch (IOException ignored) {
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        String configFile = "folder/config.json";
        if (args.length >= 1) configFile = args[0];
        Master master = new Master(configFile);
        master.start();
    }
}