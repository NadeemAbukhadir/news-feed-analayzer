package de.tick.ts.server.task;

import de.tick.ts.common.dto.NewsItem;
import de.tick.ts.server.storage.NewsItemStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * {@code NewsSummaryReporter} is responsible for aggregating and reporting
 * recent positive news items stored in {@link NewsItemStorage}.
 *
 * <h3>Usage:</h3>
 * This class should be used as part of a scheduled execution
 * where it is periodically triggered to generate news reports.
 */
public class NewsSummaryReporter {

    private static final Logger logger = LoggerFactory.getLogger(NewsSummaryReporter.class);
    private static final int TOP_HEADLINES_MAX_COUNT = 3;
    private final NewsItemStorage storage;
    private final int periodInSeconds;

    public NewsSummaryReporter(NewsItemStorage storage, int periodInSeconds) {

        this.storage = storage;
        this.periodInSeconds = periodInSeconds;
    }

    /**
     * Generates a summary report of recent positive news items.
     * <p>
     * This method retrieves stored news items, filters out the top 3 unique
     * high-priority positive headlines, and logs them along with the total
     * number of processed items.
     * </p>
     * <h3>Process:</h3>
     * <ol>
     *   <li>Retrieves and clears all stored news from {@link NewsItemStorage}.</li>
     *   <li>Iterates through the news items map/dequeue, maintaining priority order.</li>
     *   <li>Extracts up to 3 unique positive headlines (ignoring word order).</li>
     *   <li>Logs the time window and top 3 headlines.</li>
     * </ol>
     *
     * @return totalNewsCount - Total Reported News in current time window.
     */
    public int report() {

        Instant now = Instant.now();
        SortedMap<Integer, Deque<NewsItem>> oldMap = storage.resetAndGetAll();
        int totalNewsCount = oldMap.values().stream().mapToInt(Deque::size).sum();
        Set<Set<String>> seenHeadlineSignatures = new HashSet<>();
        List<NewsItem> topUniqueHeadlines = new ArrayList<>();

        outerLoop:
        for (Map.Entry<Integer, Deque<NewsItem>> entry : oldMap.entrySet()) {

            for (NewsItem item : entry.getValue()) {

                if (topUniqueHeadlines.size() >= TOP_HEADLINES_MAX_COUNT) {
                    break outerLoop;
                }

                Set<String> headlineSignature = buildHeadlineSignature(item.getHeadline());

                if (seenHeadlineSignatures.add(headlineSignature)) {
                    topUniqueHeadlines.add(item); // If headline signature haven't seen yet, cache it.
                }
            }
        }

        Instant start = now.minus(periodInSeconds, ChronoUnit.SECONDS);
        logger.info("Window [{} - {}] -> Total News: {}", start, now, totalNewsCount);
        for (NewsItem newsItem : topUniqueHeadlines) {
            logger.info("Priority: {}, Headline: {}", newsItem.getPriority(), newsItem.getHeadline());
        }

        return totalNewsCount;
    }

    /**
     * Creates a signature Set<String> for a headline, ignoring order of words.
     * e.g. "up rise success" -> {"success","rise","up"}
     */
    public static Set<String> buildHeadlineSignature(String headline) {

        String[] words = headline.split("\\s+");
        return new HashSet<>(Arrays.asList(words));
    }
}
