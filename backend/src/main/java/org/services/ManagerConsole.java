package org.services;

import com.google.gson.Gson;
import org.domain.*;
import org.requests.Request;
import org.requests.serverside.*;
import org.utility.Connectivity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Scanner;

/**
 * The type Manager console.
 */
public class ManagerConsole {

    private final String masterHost;
    private final int masterPort;
    private final Gson gson = new Gson();
    private final Scanner scanner = new Scanner(System.in);

    /**
     * Instantiates a new Manager console.
     *
     * @param masterHost the master host
     * @param masterPort the master port
     */
    public ManagerConsole(String masterHost, int masterPort) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
    }

    private String sendRequest(String jsonData) {
        Socket socket = Connectivity.connect(masterHost, masterPort);
        Connectivity.sendStringData(socket, jsonData);
        String response = Connectivity.receiveStringData(socket);
        try {
            socket.close();
        }
        catch (IOException e) {
        }
        return response;
    }

    /**
     * Run.
     */
    public void run() {
        System.out.println("Manager Console connected to " + masterHost + ":" + masterPort);

        boolean running = true;
        while (running) {
            System.out.println("\n========== MANAGER CONSOLE ==========");
            System.out.println("1. Add Game (from JSON file)");
            System.out.println("2. Remove Game");
            System.out.println("3. Re-Activate Game");
            System.out.println("4. Modify Game (change risk level or bet limits)");
            System.out.println("5. Stats by Provider/Game (MapReduce)");
            System.out.println("6. Stats by Player (MapReduce)");
            System.out.println("7. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": addGameFromFile(); break;
                    case "2": removeGame(); break;
                    case "3": reActivateGame(); break;
                    case "4": modifyGame(); break;
                    case "5": statsByProvider(); break;
                    case "6": statsByPlayer(); break;
                    case "7": running = false; break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private Game parseGameJson(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IOException("File not found: " + filePath);
        }
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();

        //simple JSON parsing using Gson
        GameJson gj = gson.fromJson(sb.toString(), GameJson.class);
        return new Game(gj.GameName, gj.ProviderName, gj.Stars, gj.NoOfVotes,
                gj.GameLogo, gj.MinBet, gj.MaxBet,
                RiskLevel.valueOf(gj.RiskLevel.toLowerCase()), gj.HashKey);
    }

    //helper class for JSON parsing
    private static class GameJson {
        /**
         * The Game name.
         */
        String GameName,
        /**
         * The Provider name.
         */
        ProviderName,
        /**
         * The Game logo.
         */
        GameLogo,
        /**
         * The Risk level.
         */
        RiskLevel,
        /**
         * The Hash key.
         */
        HashKey;
        /**
         * The Stars.
         */
        int Stars,
        /**
         * The No of votes.
         */
        NoOfVotes;
        /**
         * The Min bet.
         */
        float MinBet,
        /**
         * The Max bet.
         */
        MaxBet;
    }

    private void addGameFromFile() throws IOException {
        System.out.print("Enter JSON file path: ");
        String path = scanner.nextLine().trim();

        Game game;
        try {
            game = parseGameJson(path);
        }
        catch (IOException e) {
            System.out.println("Error: Cannot read file '" + path + "'. " + e.getMessage());
            return;
        }
        catch (Exception e) {
            System.out.println("Error: Invalid game JSON format: " + e.getMessage());
            return;
        }
        System.out.println("Parsed: " + game.getGameName() + " (provider: " + game.getProviderName() +
                ", risk: " + game.getRiskLevel() + ", bet: " + game.getBetLevel() + ")");

        AddGameRequest req = new AddGameRequest(0, game);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));

        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        System.out.println("Response: " + respShell.getAddGameRequest().getStatus() +
                (respShell.getAddGameRequest().getErrorMessage() != null ?
                        " - " + respShell.getAddGameRequest().getErrorMessage() : ""));
    }

    private void removeGame() {
        System.out.print("Game Name to remove: ");
        String name = scanner.nextLine().trim();

        RemoveGameRequest req = new RemoveGameRequest(0, name);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));
        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        System.out.println("Response: " + respShell.getRemoveGameRequest().getStatus() +
                (respShell.getRemoveGameRequest().getErrorMessage() != null ?
                        " - " + respShell.getRemoveGameRequest().getErrorMessage() : ""));
    }

    private void reActivateGame() {
        System.out.println("Game Name to re-activate game: ");
        String name = scanner.nextLine().trim();

        ReActivateGameRequest req = new ReActivateGameRequest(0, name);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));
        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        System.out.println("Response: " + respShell.getReActivateGameRequest().getStatus() +
                (respShell.getReActivateGameRequest().getErrorMessage() != null ?
                        " - " + respShell.getReActivateGameRequest().getErrorMessage() : ""));
    }


    private void modifyGame() {
        System.out.print("Game Name to modify: ");
        String name = scanner.nextLine().trim();

        //risk
        System.out.print("new risk level (low/medium/high or Enter to skip): ");
        String riskInput = scanner.nextLine().trim();
        String risk = null;
        if (!riskInput.isEmpty()) {
            if (!riskInput.equals("low") && !riskInput.equals("medium") && !riskInput.equals("high")) {
                System.out.println("Invalid risk level please type: low, medium, or high.");
                return;
            }
            risk = riskInput;
        }

        //min bet
        System.out.print("New Min Bet (0.1, 1, 5 or Enter to skip): ");
        String minInput = scanner.nextLine().trim();
        Float newMinBet = null;
        if (!minInput.isEmpty()) {
            try {
                newMinBet = Float.parseFloat(minInput);
            } catch (NumberFormatException e) {
                System.out.println("invalid min bet.");
                return;
            }
            if (newMinBet != 0.1f && newMinBet != 1.0f && newMinBet != 5.0f) {
                System.out.println("Min bet must be one of: 0.1, 1, 5.");
                return;
            }
        }

        //max bet proeretiko
        System.out.print("new max bet or enter to skip: ");
        String maxInput = scanner.nextLine().trim();
        Float newMaxBet = null;
        if (!maxInput.isEmpty()) {
            try {
                newMaxBet = Float.parseFloat(maxInput);
            } catch (NumberFormatException e) {
                System.out.println("invalid max bet.");
                return;
            }
        }

        if (risk == null && newMinBet == null && newMaxBet == null) {
            System.out.println("Nothing to modify.");
            return;
        }

        ModifyGameRequest req = new ModifyGameRequest(0, name, risk, newMinBet, newMaxBet);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));
        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        System.out.println("Response: " + respShell.getModifyGameRequest().getStatus() +
                (respShell.getModifyGameRequest().getErrorMessage() != null ?
                        " - " + respShell.getModifyGameRequest().getErrorMessage() : ""));
    }


    private void statsByProvider() {
        System.out.print("Provider Name: ");
        String provider = scanner.nextLine().trim();

        StatsProviderRequest req = new StatsProviderRequest(0, provider);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));

        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        StatsProviderRequest result = respShell.getStatsProviderRequest();

        System.out.println("\n===== Stats for Provider: " + provider + " =====");
        double total = 0;
        if (result.getGameStats() != null && !result.getGameStats().isEmpty()) {
            for (String gameName : result.getGameStats().keySet()) {
                double profit = result.getGameStats().get(gameName);
                total += profit;
                System.out.printf("  %-30s : %+.2f FUN%n", gameName, profit);
            }
            System.out.println("  ----------------------------------------");
        }
        System.out.printf("  %-30s : %+.2f FUN%n", "TOTAL", total);
    }

    private void statsByPlayer() {
        System.out.print("Player ID: ");
        String playerId = scanner.nextLine().trim();

        StatsPlayerRequest req = new StatsPlayerRequest(0, playerId);
        RequestShell shell = new RequestShell(req);
        String resp = sendRequest(gson.toJson(shell));

        RequestShell respShell = gson.fromJson(resp, RequestShell.class);
        StatsPlayerRequest result = respShell.getStatsPlayerRequest();

        if (result.getStatus() == Request.RequestStatus.ERROR) {
            System.out.println("Error: " + result.getErrorMessage());
            return;
        }

        System.out.println("\n===== Stats for Player: " + playerId + " =====");
        if (result.getGameBreakdown() != null && !result.getGameBreakdown().isEmpty()) {
            for (String gameName : result.getGameBreakdown().keySet()) {
                double profit = result.getGameBreakdown().get(gameName);
                System.out.printf("  %-30s : %+.2f FUN%n", gameName, profit);
            }
            System.out.println("  ----------------------------------------");
        }
        System.out.printf("  %-30s : %+.2f FUN%n", "TOTAL Profit/Loss", result.getTotalProfitLoss());
    }


    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 3333;
        if (args.length >= 2) {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }
        new ManagerConsole(host, port).run();
    }
}
