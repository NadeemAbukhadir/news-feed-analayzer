package com.github.nadeemabukhadir.news_analyzer.mocknewsfeed;

import com.github.nadeemabukhadir.news_analyzer.common.config.PropertiesManager;
import com.github.nadeemabukhadir.news_analyzer.common.mapper.NewsItemMapper;
import com.github.nadeemabukhadir.news_analyzer.mocknewsfeed.client.MockNewsClient;
import com.github.nadeemabukhadir.news_analyzer.mocknewsfeed.config.MockNewsClientConfig;
import com.github.nadeemabukhadir.news_analyzer.mocknewsfeed.generator.NewsContentGenerator;

import java.util.concurrent.TimeUnit;

public class MockNewsServiceApplication {

    public static void main(String[] args) {

        PropertiesManager.initialize("config.properties");
        final int serverPort = PropertiesManager.getInt("news.analyze.server.port", 8080);
        final String serverHost = PropertiesManager.get("news.analyze.server.host", "localhost");
        final int messageSendIntervalInMs = PropertiesManager.getInt("scheduler.message-send.intervalInMs", 200);

        NewsItemMapper mapper = new NewsItemMapper();
        NewsContentGenerator generator = new NewsContentGenerator();
        MockNewsClientConfig config = new MockNewsClientConfig(serverHost, serverPort, messageSendIntervalInMs, TimeUnit.MILLISECONDS);
        MockNewsClient client = new MockNewsClient(config, mapper, generator);
        client.start();
    }
}
