package org.services;

import org.utility.Connectivity;
import org.utility.DistributedServerConfiguration;
import org.utility.GetIPAddress;
import org.utility.Security;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Random;
import java.util.TreeSet;

/**
 * The type Random number generator.
 */
public class RandomNumberGenerator extends Thread {

    private final int port;
    private final int bufferSize;
    private final String secret;

    /**
     * The Random numbers.
     */
    public final HashMap<String, LinkedList<Integer>> randomNumbers = new HashMap<>();


    /**
     * The enum Rng request type.
     */
    public enum RNGRequestType {
        /**
         * Get rng request type.
         */
        GET,
        /**
         * Clear rng request type.
         */
        CLEAR,
        /**
         * Create rng request type.
         */
        CREATE,
    }


    /**
     * The entry point of application.
     *
     * @param args the input arguments
     */
    public static void main(String[] args) {
        if(args.length != 2) {
            System.out.println("RNG <port> <config file>");
            return;
        }
        DistributedServerConfiguration configuration = null;
        int port;
        try {
            port = Integer.parseInt(args[0]);
            String configFile = args[1];
            configuration = DistributedServerConfiguration.getDistributedServerConfiguration(configFile);
        } catch (IOException e) {
            throw new RuntimeException("Could not load configuration file: " + e.getMessage());
        }
        int bufferSize = configuration.getBufferSize();
        String secret = configuration.getSecret();

        RandomNumberGenerator rng = new RandomNumberGenerator(port, bufferSize, secret);
        rng.start();

        System.out.println("System started on the following specs:\n" +
                "Public IP: "+ GetIPAddress.getPublicIPAddress() +
                "Network IP: "+GetIPAddress.getLocalIPAddress() +"\n" +
                "System port: " + port);
    }


    /**
     * Instantiates a new Random number generator.
     *
     * @param port       the port
     * @param bufferSize the buffer size
     * @param secret     the secret
     */
    public RandomNumberGenerator(int port, int bufferSize, String secret) {
        this.port = port;
        this.bufferSize = bufferSize;
        this.secret = secret;
        System.out.println("[RNG] Initializing on port " + port + " with bufferSize=" + bufferSize);
    }

    private int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(100) + 1; //[1,100]
    }

    @Override
    public void run() {
        System.out.println("[RNG] Thread started, pre-filling buffer...");


        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("[RNG] Listening on port " + port);
        } catch (IOException e) {
            throw new RuntimeException("RNG service failed to open port: " + port, e);
        }

        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[RNG] Accepted connection from " + clientSocket.getRemoteSocketAddress());
                new RandomNumberGeneratorThread(clientSocket).start();
            }
        } catch (IOException e) {
            throw new RuntimeException("RNG service connection error on port: " + port, e);
        }
    }

    /**
     * The type Random number generator thread.
     */
    class RandomNumberGeneratorThread extends Thread {

        private final Socket clientSocket;

        /**
         * Instantiates a new Random number generator thread.
         *
         * @param clientSocket the client socket
         */
        RandomNumberGeneratorThread(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }



        @Override
        public void run() {
            String requestData = Connectivity.receiveStringData(clientSocket);
            if(requestData == null) {
                return;
            }

            String response;

            String data[] = requestData.split(":");
            String gameName = data[0].toLowerCase();
            RNGRequestType requestType = RNGRequestType.valueOf(data[1]);
            switch (requestType) {
                case CREATE -> {
                    LinkedList<Integer> list;
                    synchronized (randomNumbers) {
                        if(randomNumbers.containsKey(gameName)) {
                            System.out.println("[RNG] Game " + gameName + " already exists");
                            return;
                        }
                        list = new LinkedList<>();
                        randomNumbers.put(gameName, list);

                    }
                    synchronized(list) {
                        for(int i = 0; i< bufferSize; i++) {
                            list.add(getRandomNumber());
                        }
                        System.out.println("[RNG] Created " + gameName + " with " + list.size() + " random numbers.");
                    }
                }
                case GET -> {
                    //extract number
                    LinkedList<Integer> list;
                    synchronized (randomNumbers) {
                        if(!randomNumbers.containsKey(gameName)) {
                            response = -1 + ":Game not found.";
                            Connectivity.sendStringData(clientSocket,response);
                            System.out.println("[RNG] Game " + gameName + " not found.");
                            return;
                        }
                        list = randomNumbers.get(gameName);
                    }
                    int randomNumber;
                    synchronized (list) {
                        while(list.isEmpty()) {
                            try {
                                list.wait();
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        randomNumber = list.removeFirst();
                    }
                    System.out.println("[RNG] Game " + gameName + " gets " + randomNumber);
                    //send number
                    Connectivity.sendStringData(clientSocket, randomNumber + ":" + Security.convertToSha256(String.valueOf(randomNumber)+ secret ));

                    //add number
                    int newRandomNumber = getRandomNumber();
                    synchronized (list) {
                        list.addLast(newRandomNumber);
                        list.notifyAll();
                    }
                }
                case CLEAR -> {
                    synchronized (randomNumbers) {
                        randomNumbers.remove(gameName);
                    }
                    System.out.println("[RNG] Game " + gameName + " cleared.");

                }
            }



        }
    }

    /**
     * Extract number int.
     *
     * @param RNGResponse the rng response
     * @return the int
     */
    public static int extractNumber(String RNGResponse) {
        String[] data = RNGResponse.split(":");
        return Integer.parseInt(data[0]);

    }

    /**
     * Extract hash string.
     *
     * @param RNGResponse the rng response
     * @return the string
     */
    public static String extractHash(String RNGResponse) {
        String[] data = RNGResponse.split(":");
        return data[1];
    }

    /**
     * Validate rng response boolean.
     *
     * @param number the number
     * @param secret the secret
     * @param hash   the hash
     * @return the boolean
     */
    public static boolean validateRNGResponse(Integer number, String secret, String hash) {
        String producedHash = Security.convertToSha256(number.toString() + secret);
        return producedHash.equals(hash);
    }

    /**
     * Retrieve value int.
     *
     * @param RNG_IP   the rng ip
     * @param port     the port
     * @param secret   the secret
     * @param gameName the game name
     * @return the int
     */
    public static int retrieveValue(String RNG_IP, int port, String secret,String gameName) {
        while (true) {
            try (Socket randomNumberRetrievalSocket = Connectivity.connect(RNG_IP, port)) {

                Connectivity.sendStringData(randomNumberRetrievalSocket, gameName+":"+RNGRequestType.GET);
                String rawData = Connectivity.receiveStringData(randomNumberRetrievalSocket);
                int num = extractNumber(rawData);
                if(num == -1) {
                    return -1;
                }
                String hash = extractHash(rawData);
                if (validateRNGResponse(num, secret, hash)) {
                    return num;
                } else {
                    System.err.println("[RNG-Client] Invalid hash from RNG, retrying...");
                }
            } catch (Exception e) {
                System.err.println("[RNG-Client] Error connecting to RNG " +
                        RNG_IP + ":" + port + " - " + e.getMessage());
                try { Thread.sleep(200); } catch (InterruptedException ignored) {}
            }
        }
    }
}
