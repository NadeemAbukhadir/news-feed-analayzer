package de.tick.ts.server;

import de.tick.ts.common.mapper.NewsItemMapper;
import de.tick.ts.server.config.NewsAnalyzerServerConfig;
import de.tick.ts.server.storage.NewsItemStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * News Analyzer Server - Listens for client connections, processes and store incoming news,
 */
public class NewsAnalyzerServer {

    private static final Logger logger = LoggerFactory.getLogger(NewsAnalyzerServer.class);
    private static final int AWAIT_TERMINATION_TIMEOUT = 5;
    private boolean running = true;
    private final NewsAnalyzerServerConfig config;
    private final ExecutorService clientHandlerPool;
    private final NewsItemMapper mapper;
    private final NewsItemStorage storage;

    public NewsAnalyzerServer(NewsAnalyzerServerConfig config, NewsItemMapper mapper, NewsItemStorage storage) {

        this.clientHandlerPool = Executors.newFixedThreadPool(config.getConnectionsPoolSize());
        this.config = config;
        this.mapper = mapper;
        this.storage = storage;
    }

    /**
     * Starts the server and listens for client connections.
     */
    public void start() {

        logger.info("News analyzer server is starting on port {}...", config.getPort());

        try (ServerSocket serverSocket = new ServerSocket(config.getPort())) {
            while (running) {
                acceptConnection(serverSocket);
            }
        } catch (IOException e) {
            logger.error("Server failed to start: ", e);
        } finally {
            shutdown();
        }
    }

    /**
     * Stops the server gracefully.
     */
    public void stop() {
        running = false;
        shutdown();
    }

    /**
     * Gracefully shuts down the server and its components.
     */
    private void shutdown() {

        logger.info("Shutting down the news analyzer server...");
        clientHandlerPool.shutdown();
        try {
            if (!clientHandlerPool.awaitTermination(AWAIT_TERMINATION_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)) {
                logger.warn("Forcing shutdown as some tasks are still running...");
                clientHandlerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.error("Error during shutdown: ", e);
            Thread.currentThread().interrupt();
            clientHandlerPool.shutdownNow();
        }
    }

    /**
     * Accepts new client connections and assigns them to a handler.
     */
    private void acceptConnection(ServerSocket serverSocket) {

        if (!running) {
            return;
        }

        try {
            Socket clientSocket = serverSocket.accept();
            logger.info("New client connected: {}", clientSocket.getInetAddress());
            clientHandlerPool.execute(new ClientHandler(clientSocket, mapper, storage));
        } catch (IOException e) {
            logger.error("Error accepting client connection: ", e);
        }
    }
}