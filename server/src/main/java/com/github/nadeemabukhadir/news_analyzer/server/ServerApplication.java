package com.github.nadeemabukhadir.news_analyzer.server;

import com.github.nadeemabukhadir.news_analyzer.common.config.PropertiesManager;
import com.github.nadeemabukhadir.news_analyzer.common.mapper.NewsItemMapper;
import com.github.nadeemabukhadir.news_analyzer.common.scheduler.ScheduledTaskManager;
import com.github.nadeemabukhadir.news_analyzer.server.config.NewsAnalyzerServerConfig;
import com.github.nadeemabukhadir.news_analyzer.server.storage.NewsItemStorage;
import com.github.nadeemabukhadir.news_analyzer.server.task.NewsSummaryReporter;

import java.util.concurrent.TimeUnit;

public class ServerApplication {

    public static void main(String[] args) {

        PropertiesManager.initialize("config.properties");
        final int port = PropertiesManager.getInt("server.port", 8080);
        final int threadPoolSize = PropertiesManager.getInt("server.connectionsPoolSize", 10);
        final int periodInSeconds = PropertiesManager.getInt("scheduler.news-summary-report.periodInSeconds", 10);

        NewsAnalyzerServer server = buildNewsAnalyzerServer(periodInSeconds, port, threadPoolSize);
        server.start();
    }

    private static NewsAnalyzerServer buildNewsAnalyzerServer(int periodInSeconds, int port, int threadPoolSize) {

        NewsItemStorage storage = new NewsItemStorage();

        NewsSummaryReporter newsSummaryReporter = new NewsSummaryReporter(storage, periodInSeconds);
        ScheduledTaskManager scheduledTaskManager = new ScheduledTaskManager(newsSummaryReporter::report, 10, periodInSeconds, TimeUnit.SECONDS);
        scheduledTaskManager.start();

        NewsAnalyzerServerConfig config = new NewsAnalyzerServerConfig(port, threadPoolSize);
        NewsItemMapper mapper = new NewsItemMapper();

        return new NewsAnalyzerServer(config, mapper, storage);
    }
}
