package de.tick.ts.server.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.isNull;

public final class NewsHeadlineUtil {

    private static final Set<String> POSITIVE_WORDS;

    static {
        Set<String> words = new HashSet<>(6);
        Collections.addAll(words, "up", "rise", "good", "success", "high", "über");
        POSITIVE_WORDS = Collections.unmodifiableSet(words);
    }

    private NewsHeadlineUtil() {
    }

    /**
     * Determines whether a news headline is positive based on predefined positive words.
     *
     * @param headline the news headline string to analyze
     * @return true if more than 50% of words are positive, false otherwise
     */
    public static boolean isPositive(String headline) {

        if (isNull(headline) || headline.trim().isEmpty()) {
            return false;
        }

        String[] words = headline.split("\\s+");
        int positiveWordsCount = 0;

        for (String word : words) {
            if (POSITIVE_WORDS.contains(word)) {
                positiveWordsCount++;
            }
        }

        return (positiveWordsCount * 100 / words.length) >= 50;
    }
}
