package org.utility;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

/**
 * The type Distributed server configuration.
 */
public class DistributedServerConfiguration {

    /**
     * Gets distributed server configuration.
     *
     * @param filePath the file path
     * @return the distributed server configuration
     * @throws IOException the io exception
     */
    public static DistributedServerConfiguration getDistributedServerConfiguration(String filePath) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        StringBuilder configurationString = new StringBuilder();
        while (reader.ready()) {
            configurationString.append(reader.readLine());
        }
        reader.close();
        Gson gson = new Gson();
        return gson.fromJson(configurationString.toString(), DistributedServerConfiguration.class);
    }

    private final Pair<String, Integer> Master;
    private final LinkedList<Pair<String, Integer>> Workers;
    private final Pair<String, Integer> RNG;
    private final Pair<String, Integer> Reducer;
    private final String Secret;
    private final int bufferSize;

    /**
     * Instantiates a new Distributed server configuration.
     *
     * @param Master     the master
     * @param workers    the workers
     * @param RNG        the rng
     * @param reducer    the reducer
     * @param Secret     the secret
     * @param bufferSize the buffer size
     */
    public DistributedServerConfiguration(Pair<String, Integer> Master,
                                          LinkedList<Pair<String, Integer>> workers,
                                          Pair<String, Integer> RNG,
                                          Pair<String, Integer> reducer,
                                          String Secret,int bufferSize) {
        this.Master = Master;
        this.Workers = workers;
        this.RNG = RNG;
        this.Reducer = reducer;
        this.Secret = Secret;
        this.bufferSize = bufferSize;
    }

    /**
     * Gets master public side.
     *
     * @return the master public side
     */
//PublicSide = Master port (clients connect here)
    public Pair<String, Integer> getMasterPublicSide() {
        return Master;
    }

    /**
     * Gets master server side.
     *
     * @return the master server side
     */
//ServerSide = Master port + 50 (Reducer sends results here)
    public Pair<String, Integer> getMasterServerSide() {
        return new Pair<>(Master.getFirst(), Master.getSecond() + 50);
    }

    /**
     * Gets master.
     *
     * @return the master
     */
    public Pair<String, Integer> getMaster() {
        return Master;
    }

    /**
     * Gets workers.
     *
     * @return the workers
     */
    public LinkedList<Pair<String, Integer>> getWorkers() {
        return Workers;
    }

    /**
     * Gets rng.
     *
     * @return the rng
     */
    public Pair<String, Integer> getRNG() {
        return RNG;
    }

    /**
     * Gets reducer.
     *
     * @return the reducer
     */
    public Pair<String, Integer> getReducer() {
        return Reducer;
    }

    /**
     * Gets secret.
     *
     * @return the secret
     */
    public String getSecret() {
        return Secret;
    }

    /**
     * Gets buffer size.
     *
     * @return the buffer size
     */
    public int getBufferSize() {
        return bufferSize;
    }
}