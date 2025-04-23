package com.github.nadeemabukhadir.news_analyzer.server.config;

import com.github.nadeemabukhadir.news_analyzer.server.NewsAnalyzerServer;

/**
 * Immutable configuration class for {@link NewsAnalyzerServer}.
 * Stores configurable properties such as server port, server connections pool size.
 */
public class NewsAnalyzerServerConfig {

    private final int port;
    private final int connectionsPoolSize;

    public NewsAnalyzerServerConfig(int port, int connectionsPoolSize) {
        this.port = port;
        this.connectionsPoolSize = connectionsPoolSize;
    }

    public int getPort() {
        return port;
    }

    public int getConnectionsPoolSize() {
        return connectionsPoolSize;
    }

    @Override
    public String toString() {
        return "NewsAnalyzerServerConfig{" +
                "port=" + port +
                ", connectionsPoolSize=" + connectionsPoolSize +
                '}';
    }
}
