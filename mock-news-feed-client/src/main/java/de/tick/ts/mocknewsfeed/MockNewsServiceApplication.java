package de.tick.ts.mocknewsfeed;

import de.tick.ts.common.config.PropertiesManager;
import de.tick.ts.common.mapper.NewsItemMapper;
import de.tick.ts.mocknewsfeed.client.MockNewsClient;
import de.tick.ts.mocknewsfeed.config.MockNewsClientConfig;
import de.tick.ts.mocknewsfeed.generator.NewsContentGenerator;

import java.util.concurrent.TimeUnit;

public class MockNewsServiceApplication {

    public static void main(String[] args) {

        PropertiesManager.initialize("config.properties");
        final int serverPort = PropertiesManager.getInt("news.analyze.server.port", 5000);
        final String serverHost = PropertiesManager.get("news.analyze.server.host", "localhost");
        final int messageSendIntervalInMs = PropertiesManager.getInt("scheduler.message-send.intervalInMs", 3000);

        NewsItemMapper mapper = new NewsItemMapper();
        NewsContentGenerator generator = new NewsContentGenerator();
        MockNewsClientConfig config = new MockNewsClientConfig(serverHost, serverPort, messageSendIntervalInMs, TimeUnit.MILLISECONDS);
        MockNewsClient client = new MockNewsClient(config, mapper, generator);
        client.start();
    }
}
