package de.tick.ts.mocknewsfeed.generator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Generates random news-related attributes such as headlines and priority.
 */
public class NewsContentGenerator {

    public static final List<String> WORDS = Arrays.asList("up", "down", "rise", "fall", "good", "bad", "success",
            "failure", "high", "low", "über", "unter");
    private static final int FALLBACK_PRIORITY = 9;
    private static final int RANDOMNESS_BOUND = 1000;
    /**
     * Cumulative Distribution Function (CDF) thresholds, scaled by 10 to avoid floating-point operations.
     * Index represents priority, and values define the upper bound (exclusive) for respective priority.
     * The probability of selecting each priority follows the predefined distribution:
     * <p>
     * P(0)=29.3%, P(1)=19.3%, P(2)=14.3%, P(3)=10.9%, P(4)=8.4%,
     * P(5)=6.5%, P(6)=4.8%, P(7)=3.4%, P(8)=2.1%, P(9)=1.0%
     */
    private static final int[] CUMULATIVE_THRESHOLDS = {
            293, // P0: 0-292   (29.3%)
            486, // P1: 293-485 (19.3%)
            629, // P2: 486-628 (14.3%)
            738, // P3: 629-737 (10.9%)
            822, // P4: 738-821 (8.4%)
            887, // P5: 822-886 (6.5%)
            935, // P6: 887-934 (4.8%)
            969, // P7: 935-968 (3.4%)
            990, // P8: 969-989 (2.1%)
            1000 // P9: 990-999 (1.0%)
    };

    /**
     * Generates a random headline using 3-5 words from the predefined list.
     * This method randomly selects a subset of words, shuffles them, and joins them into a headline.
     *
     * @return A randomly generated news headline.
     */
    public String generateHeadline() {

        ThreadLocalRandom random = ThreadLocalRandom.current();
        int randomWordCount = random.nextInt(3, 6);
        Collections.shuffle(WORDS, random);
        return String.join(" ", WORDS.subList(0, randomWordCount));
    }


    /**
     * Generates a priority value between 0-9 based on a probability distribution.
     * Uses the Cumulative Distribution Function (CDF) approach to map a randomly generated number
     * to the respective probability range in the predefined distribution.
     *
     * @return An integer priority value between 0 and 9.
     */
    public int generatePriority() {

        int randomNumber = ThreadLocalRandom.current().nextInt(RANDOMNESS_BOUND);
        for (int priority = 0; priority < CUMULATIVE_THRESHOLDS.length; priority++) {

            if (randomNumber < CUMULATIVE_THRESHOLDS[priority]) {
                return priority;
            }
        }
        return FALLBACK_PRIORITY;
    }
}
