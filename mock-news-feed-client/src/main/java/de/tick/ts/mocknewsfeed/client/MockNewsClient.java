package de.tick.ts.mocknewsfeed.client;

import de.tick.ts.common.dto.NewsItem;
import de.tick.ts.common.mapper.NewsItemMapper;
import de.tick.ts.common.scheduler.ScheduledTaskManager;
import de.tick.ts.mocknewsfeed.config.MockNewsClientConfig;
import de.tick.ts.mocknewsfeed.generator.NewsContentGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Mock News Client - Periodically sends news items to the News Analyzer server.
 * Uses a configurable interval and handles reconnection on failure.
 */
public class MockNewsClient {

    private static final Logger logger = LoggerFactory.getLogger(MockNewsClient.class);
    private final MockNewsClientConfig config;
    private final NewsItemMapper mapper;
    private final NewsContentGenerator generator;
    private final ScheduledTaskManager scheduledTaskManager;
    private Socket socket;
    private PrintWriter writer;
    private boolean connected = false;

    /**
     * Constructs a MockNewsClient with an injected configuration.
     *
     * @param config Configuration object containing host, port, and message interval.
     */
    public MockNewsClient(MockNewsClientConfig config, NewsItemMapper mapper, NewsContentGenerator generator) {

        this.config = config;
        this.mapper = mapper;
        this.generator = generator;
        Runnable task = () -> {
            if (connected) {
                sendMessage();
            } else {
                retryConnection();
            }
        };
        this.scheduledTaskManager = new ScheduledTaskManager(task, 0, config.getMessageInterval(), config.getMessageIntervalTimeUnit());
    }

    /**
     * Starts the client and attempts to connect to the server.
     */
    public void start() {

        while (!connected) {
            try {
                connectToServer();
            } catch (IOException e) {
                logger.error("Failed to connect to the server. Retrying in {} {}...", config.getMessageInterval(), config.getMessageIntervalTimeUnit());
                sleep(config.getMessageInterval());
            }
        }
    }

    /**
     * Establishes a connection to the News Analyzer server.
     */
    private void connectToServer() throws IOException {
        logger.info("Attempting to connect to News Analyzer Server at {}:{}", config.getServerHost(), config.getServerPort());
        socket = new Socket(config.getServerHost(), config.getServerPort());
        writer = new PrintWriter(socket.getOutputStream(), true);
        connected = true;
        logger.info("Successfully connected to the server.");
        // Once connected, we start scheduling the periodic sendTask
        // TODO: Make sure calling the start method at the correct place.
        scheduledTaskManager.start();
    }

    /**
     * Generates and sends a news item to the server.
     */
    private void sendMessage() {

        NewsItem newsItem = new NewsItem(generator.generateHeadline(), generator.generatePriority());
        String asString = mapper.toString(newsItem);

        try {
            writer.println(asString);
            logger.info("Sent news item: {}", asString);
        } catch (Exception e) {
            logger.error("Error sending news item: ", e);
            connected = false;
        }
    }

    /**
     * Handles reconnection logic in case of failure.
     */
    private void retryConnection() {

        logger.warn("Lost connection. Retrying...");
        stop(); // Clean up before reconnecting

        while (!connected) {
            try {
                connectToServer();
            } catch (IOException e) {
                logger.error("Retry failed: Unable to connect. Retrying in {} ms...", config.getMessageInterval());
                sleep(config.getMessageInterval());
            }
        }
    }

    /**
     * Gracefully shuts down the client and releases resources.
     */
    public void stop() {

        try {
            scheduledTaskManager.stop();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            connected = false;
            logger.info("Mock News Client stopped.");
        } catch (IOException e) {
            logger.error("Error while closing client: ", e);
        }
    }

    /**
     * Utility method to pause execution.
     *
     * @param millis milliseconds to sleep
     */
    private void sleep(int millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
