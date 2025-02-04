package de.tick.ts.mocknewsfeed.config;

import de.tick.ts.mocknewsfeed.client.MockNewsClient;

import java.util.concurrent.TimeUnit;

/**
 * Immutable configuration class for {@link MockNewsClient}.
 * Stores configurable properties such as server host, port, and message interval.
 */
public final class MockNewsClientConfig {

    private final String serverHost;
    private final int serverPort;
    private final int messageInterval;
    private final TimeUnit messageIntervalTimeUnit;

    public MockNewsClientConfig(String serverHost, int serverPort, int messageInterval,
                                TimeUnit messageIntervalTimeUnit) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;
        this.messageInterval = messageInterval;
        this.messageIntervalTimeUnit = messageIntervalTimeUnit;
    }

    public String getServerHost() {
        return serverHost;
    }

    public int getServerPort() {
        return serverPort;
    }

    public int getMessageInterval() {
        return messageInterval;
    }

    public TimeUnit getMessageIntervalTimeUnit() {
        return messageIntervalTimeUnit;
    }
}
