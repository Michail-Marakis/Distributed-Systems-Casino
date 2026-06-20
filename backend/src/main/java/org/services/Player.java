package org.services;

import com.google.gson.Gson;
import org.domain.BetLevel;
import org.domain.Game;
import org.domain.RiskLevel;
import org.requests.Request;
import org.requests.serverside.*;
import org.utility.Connectivity;

import java.io.IOException;
import java.net.Socket;
import java.util.*;

/**
 * The type Player.
 */
public class Player {

    private List<Game> gamesSearchResult;

    private final String masterHost;
    private final int masterPort;
    private final Gson gson = new Gson();
    private final Scanner scanner = new Scanner(System.in);
    private final String Playerid;


    /**
     * Instantiates a new Player.
     *
     * @param masterHost the master host
     * @param masterPort the master port
     * @param Playerid   the playerid
     */
    Player(String masterHost, int masterPort, String Playerid) {
        this.masterHost = masterHost;
        this.masterPort = masterPort;
        this.Playerid = Playerid;
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
        System.out.println("Player " + Playerid + " connected to " + masterHost + ":" + masterPort);

        boolean running = true;
        while (running) {
            System.out.println("\n========== PLAYER CONSOLE ==========");
            System.out.println("1. Search Game");
            System.out.println("2. Play Game");
            System.out.println("3. Add Balance");
            System.out.println("4. Rate Game");
            System.out.println("5. Exit");
            System.out.print("Choice: ");

            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1": search(); break;
                    case "2": play(); break;
                    case "3": addBalance(); break;
                    case "4": rateGame(); break;
                    case "5": running = false; break;
                    default: System.out.println("Invalid choice.");
                }
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private void search() {

        System.out.println("Please enter the name of the game you want to search (or press Enter to skip): ");
        String name = scanner.nextLine().trim();

        System.out.println("Please enter the provider name (or press Enter to skip): ");
        String providerName = scanner.nextLine().trim();

        System.out.println("Enter stars lower bound (1-5) or press Enter to skip: ");
        String lowerInput = scanner.nextLine().trim();
        int starsLowerBound = lowerInput.isEmpty() ? 1 : Integer.parseInt(lowerInput);

        System.out.println("Enter stars upper bound (1-5) or press Enter to skip: ");
        String upperInput = scanner.nextLine().trim();
        int starsUpperBound = upperInput.isEmpty() ? 5 : Integer.parseInt(upperInput);

        System.out.println("Enter bidding levels ($,$$,$$$) comma separated or press Enter to skip: ");
        String betLevelsInput = scanner.nextLine().trim();

        System.out.println("Enter risk levels (low,medium,high) comma separated or press Enter to skip: ");
        String riskLevelsInput = scanner.nextLine().trim();


        HashSet<BetLevel> betLevels = new HashSet<>();
        if (betLevelsInput.isBlank()) {
            //pare ta ola
            betLevels.addAll(Arrays.asList(BetLevel.values()));
        } else {
            String[] betParts = betLevelsInput.split(",");
            for (String b : betParts) {
                if(!b.isBlank()) {
                    betLevels.add(BetLevel.valueOf(b.trim().toLowerCase()));
                }
            }
        }

        HashSet<RiskLevel> riskLevels = new HashSet<>();
        if (riskLevelsInput.isBlank()) {
            //pare ta ola
            riskLevels.addAll(Arrays.asList(RiskLevel.values()));
        } else {
            String[] riskParts = riskLevelsInput.split(",");
            for (String r : riskParts) {
                if(!r.isBlank()) {
                    riskLevels.add(RiskLevel.valueOf(r.trim().toLowerCase()));
                }
            }
        }

        GetGamesRequest request = new GetGamesRequest(
                0,
                name,
                providerName,
                starsLowerBound,
                starsUpperBound,
                betLevels,
                riskLevels
        );


        RequestShell shell = new RequestShell(request);

        String response = sendRequest(gson.toJson(shell));

        RequestShell responseShell = gson.fromJson(response, RequestShell.class);
        gamesSearchResult =  responseShell.getGetGamesRequest().getGrabbedGames();

        if(!gamesSearchResult.isEmpty()){
            displayGames();
        }else{
            System.out.println("No Games found with these filters.");
        }
    }

    private void play() {

        displayGames();

        System.out.println("Please enter the name of the game you want to play: ");
        String nameToPlay = scanner.nextLine().trim();
        Game found = null;
        for(Game game : gamesSearchResult){
            if(nameToPlay.equalsIgnoreCase(game.getGameName())){
                found = game;
            }
        }

        if(found == null){
            System.out.println("Game not found.");
            return;
        }

        System.out.println("Max Possible Bet: " + found.getMaxBet());
        System.out.println("Min Possible Bet: " + found.getMinBet());
        System.out.println("Enter bet amount: ");
        float betAmount = Float.parseFloat(scanner.nextLine().trim());

        BetGameRequest request = new BetGameRequest(0, found.getGameName(), Playerid, betAmount);
        RequestShell shell = new RequestShell(request);

        String response = sendRequest(gson.toJson(shell));
        RequestShell responseShell = gson.fromJson(response, RequestShell.class);

        BetGameRequest betGameResult = responseShell.getBetGameRequest();

        if(betGameResult.getStatus().equals(Request.RequestStatus.OK)){
            System.out.println("Bet Results");
            if(betGameResult.isJackpot()){
                System.out.println("Jackpot");
            }
            System.out.println("Game Played: " +  found.getGameName());
            System.out.println("Bet Amount: " + betAmount);
            System.out.println("Bet Game Multiplier: " + betGameResult.getMultiplier());
            System.out.println("Bet Game Winnings: " + betGameResult.getWinnings());

            System.out.println("Profit-Loss: " + betGameResult.getPlayerProfit());

        }
        else {
        System.out.println("Error: " + betGameResult.getStatus() +
                (betGameResult.getErrorMessage() != null
                        ? " - " + betGameResult.getErrorMessage()
                        : ""));
    }
}

    private void addBalance() {
        int balance = 0;

        while (true) {
            System.out.println("Please enter the balance you want to add: ");

            try {
                balance = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
                continue;
            }

            if (balance <= 0) {
                System.out.println("Balance must be greater than 0.");
                continue;
            }

            if (balance >= 1000000) {
                System.out.println("Balance must be less than 1,000,000.");
                continue;
            }

            break;
        }

        AddBalanceRequest req = new AddBalanceRequest(0, this.Playerid, balance);
        RequestShell shell = new RequestShell(req);

        String response = sendRequest(gson.toJson(shell));

        RequestShell responseShell = gson.fromJson(response, RequestShell.class);
        AddBalanceRequest addBalanceResult = responseShell.getAddBalanceRequest();

        System.out.println(addBalanceResult.getStatus());
    }

    private void rateGame() {
        System.out.println("Please enter the game you want to rate: ");
        String name = scanner.nextLine().trim();
        System.out.println("Please enter the rate(1-5) you want to rate: ");
        int rate =  Integer.parseInt(scanner.nextLine().trim());

        RateGameRequest req = new RateGameRequest(0,name,rate);
        RequestShell shell = new RequestShell(req);

        String response =  sendRequest(gson.toJson(shell));
        RequestShell responseShell = gson.fromJson(response, RequestShell.class);
        RateGameRequest rateGameResult = responseShell.getRateGameRequest();
        System.out.println(rateGameResult.getStatus());

    }


    private void displayGames() {
        System.out.println("Search Results:");

        Set<String> printedGameNames = new HashSet<>();

        for (Game g : gamesSearchResult) {
            if (!printedGameNames.add(g.getGameName())) {
                continue;
            }
            if (g.isAvailable()) {
                System.out.println(g.getGameName());
            } else {
                System.out.println(g.getGameName() + " -- this game is not available at this moment.");
            }
        }
    }

    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        String host = "localhost";
        int port = 3333;
        String PlayerId = "1";
        if (args.length >= 3) {
            host = args[0];
            port = Integer.parseInt(args[1]);
            PlayerId = args[2];
        }
        new Player(host, port, PlayerId).run();
    }


}
