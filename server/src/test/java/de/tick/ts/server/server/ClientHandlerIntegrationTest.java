package de.tick.ts.server.server;

import de.tick.ts.common.dto.NewsItem;
import de.tick.ts.common.mapper.NewsItemMapper;
import de.tick.ts.server.ClientHandler;
import de.tick.ts.server.storage.NewsItemStorage;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Deque;
import java.util.SortedMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ClientHandlerIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandlerIntegrationTest.class);
    private static final int TEST_PORT = 6001;
    private ServerSocket testServer;
    private ExecutorService executor;
    private NewsItemStorage newsStorage;
    private NewsItemMapper newsMapper;

    @BeforeAll
    void setUp() throws IOException {

        logger.info("Starting integration test setup...");
        testServer = new ServerSocket(TEST_PORT);  // Start the mock TCP server
        executor = Executors.newCachedThreadPool();
        newsStorage = new NewsItemStorage();
        newsMapper = new NewsItemMapper();
        logger.info("Test server listening on port {}", TEST_PORT);
    }

    @AfterAll
    void tearDown() throws IOException {

        testServer.close();
        executor.shutdown();
        logger.info("Integration test cleanup complete.");
    }

    private void startClientHandler() {

        executor.execute(() -> {
            try {
                Socket socket = testServer.accept();
                new ClientHandler(socket, newsMapper, newsStorage).run();
            } catch (IOException e) {
                logger.error("Test server error", e);
            }
        });
    }

    private void sendMessageToServer(String message) throws IOException {

        try (Socket clientSocket = new Socket("localhost", TEST_PORT);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {

            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {

            logger.error("Error occurred while sending message to server: ", e);
        }
    }

    @Test
    @Order(1)
    @DisplayName("Should process and store a valid positive news item")
    void shouldProcessValidNewsItem() throws IOException {

        startClientHandler();
        sendMessageToServer("up rise success;5");

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS) // Timeout to allow processing
                .pollInterval(250, TimeUnit.MILLISECONDS)
                .untilAsserted(() -> {
                    SortedMap<Integer, Deque<NewsItem>> storedNews = newsStorage.resetAndGetAll();
                    logger.info("Storage state: {}", storedNews);
                    assertThat(storedNews).containsKey(5);
                    NewsItem newsItems = storedNews.get(5).getFirst();
                    assertThat(newsItems.getHeadline()).isEqualTo("up rise success");
                    assertThat(newsItems.getPriority()).isEqualTo(5);
                });


        logger.info("Test passed: Valid news item was processed correctly.");
    }

    @Test
    @Order(2)
    @DisplayName("Should ignore invalid message format")
    void shouldIgnoreInvalidMessageFormat() throws IOException {

        startClientHandler();

        sendMessageToServer("invalid;format");

        Awaitility.await()
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    SortedMap<Integer, Deque<NewsItem>> storedNews = newsStorage.resetAndGetAll();
                    assertThat(storedNews).isEmpty();
                });
    }

    @Test
    @Order(3)
    @DisplayName("Should handle forced client disconnect gracefully")
    void shouldHandleForcedClientDisconnect() throws IOException {

        startClientHandler();

        Socket clientSocket = new Socket("localhost", TEST_PORT);
        clientSocket.close(); // Simulate force disconnection

        Awaitility.await() // Ensure server didn't crash and the storage remains valid.
                .atMost(2, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    SortedMap<Integer, Deque<NewsItem>> storedNews = newsStorage.resetAndGetAll();
                    assertThat(storedNews).isNotNull();
                });
    }
}
