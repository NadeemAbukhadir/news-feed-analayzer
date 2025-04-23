package com.github.nadeemabukhadir.news_analyzer.server;

import com.github.nadeemabukhadir.news_analyzer.common.dto.NewsItem;
import com.github.nadeemabukhadir.news_analyzer.common.mapper.NewsItemMapper;
import com.github.nadeemabukhadir.news_analyzer.server.storage.NewsItemStorage;
import com.github.nadeemabukhadir.news_analyzer.server.util.NewsHeadlineUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

/**
 * Handles an individual client connection, processing incoming news items.
 * Responsible for reading messages, validating headlines, and storing relevant news items.
 */
public class ClientHandler implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private final Socket clientSocket;
    private final NewsItemMapper mapper;
    private final NewsItemStorage storage;

    public ClientHandler(Socket clientSocket, NewsItemMapper mapper, NewsItemStorage storage) {

        this.clientSocket = clientSocket;
        this.mapper = mapper;
        this.storage = storage;
    }

    @Override
    public void run() {

        logger.info("Handling new client connection: {}", clientSocket.getInetAddress());

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
            String receivedMessage;
            while ((receivedMessage = reader.readLine()) != null) {
                processNewsItem(receivedMessage);
            }
        } catch (SocketException e) {
            logger.warn("Client {} disconnected forcefully.", clientSocket.getInetAddress());
        } catch (IOException e) {
            logger.error("Client connection error: ", e);
        } finally {
            closeConnection();
        }
    }

    /**
     * Processes an incoming news message.
     * Converts it into a NewsItem, validates it, and stores it if positive.
     *
     * @param receivedMessage the raw message received from the client.
     */
    private void processNewsItem(String receivedMessage) {

        try {
            NewsItem newsItem = mapper.fromString(receivedMessage);
            if (NewsHeadlineUtil.isPositive(newsItem.getHeadline())) {
                storage.add(newsItem);
                logger.debug("Stored positive news item: {}", newsItem);
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid message received: '{}'. Reason: {}", receivedMessage, e.getMessage());
        }
    }


    /**
     * Closes the client connection gracefully.
     */
    private void closeConnection() {

        try {
            clientSocket.close();
            logger.info("Client disconnected: {}", clientSocket.getInetAddress());
        } catch (IOException e) {
            logger.error("Error closing client socket: ", e);
        }
    }
}
