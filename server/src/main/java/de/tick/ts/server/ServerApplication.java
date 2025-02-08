package de.tick.ts.server;

import de.tick.ts.common.config.PropertiesManager;
import de.tick.ts.common.mapper.NewsItemMapper;
import de.tick.ts.common.scheduler.ScheduledTaskManager;
import de.tick.ts.server.config.NewsAnalyzerServerConfig;
import de.tick.ts.server.storage.NewsItemStorage;
import de.tick.ts.server.task.NewsSummaryReporter;

import java.util.concurrent.TimeUnit;

public class ServerApplication {

    public static void main(String[] args) {

        PropertiesManager.initialize("config.properties");
        final int port = PropertiesManager.getInt("server.port", 5000);
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
