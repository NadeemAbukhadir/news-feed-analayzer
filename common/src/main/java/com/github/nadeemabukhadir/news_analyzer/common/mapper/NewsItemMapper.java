package com.github.nadeemabukhadir.news_analyzer.common.mapper;

import com.github.nadeemabukhadir.news_analyzer.common.dto.NewsItem;

import static com.github.nadeemabukhadir.news_analyzer.common.Constants.NEWS_HEADLINE_SEPARATOR;
import static java.util.Objects.isNull;

/**
 * Maps between {@link NewsItem} objects and their string representation.
 */
public class NewsItemMapper {

    /**
     * Maps a {@link NewsItem} object to its string representation.
     * Format: "headline;priority"
     *
     * @param item the NewsItem to map
     * @return a string representation of the NewsItem
     * @throws IllegalArgumentException if the input item is null
     */
    public String toString(NewsItem item) {

        if (item == null) {
            throw new IllegalArgumentException("NewsItem cannot be null");
        }

        return item.getHeadline() + NEWS_HEADLINE_SEPARATOR + item.getPriority();
    }

    /**
     * Parses a string into a {@link NewsItem} object.
     * Expected format: "headline;priority"
     *
     * @param message the string representation of a NewsItem
     * @return a NewsItem object
     * @throws IllegalArgumentException if the input message is null or incorrectly formatted
     */
    public NewsItem fromString(String message) {

        if (isNull(message) || !message.contains(NEWS_HEADLINE_SEPARATOR)) {
            throw new IllegalArgumentException("Invalid message format: " + message);
        }

        String[] parts = message.split(NEWS_HEADLINE_SEPARATOR);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid message format: " + message);
        }

        try {
            int priority = Integer.parseInt(parts[1].trim());

            if (priority < 0 || priority > 9) {
                throw new IllegalArgumentException("Priority must be between 0 and 9: " + message);
            }

            return new NewsItem(parts[0].trim(), priority);
        } catch (NumberFormatException e) {

            throw new IllegalArgumentException("Invalid priority value in message: " + message, e);
        }
    }
}
