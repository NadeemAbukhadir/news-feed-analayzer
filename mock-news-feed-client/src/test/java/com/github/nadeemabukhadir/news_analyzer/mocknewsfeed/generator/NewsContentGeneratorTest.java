package com.github.nadeemabukhadir.news_analyzer.mocknewsfeed.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

@ExtendWith(MockitoExtension.class)
class NewsContentGeneratorTest {

    private NewsContentGenerator newsContentGenerator;

    @BeforeEach
    void setUp() {

        this.newsContentGenerator = new NewsContentGenerator();
    }

    @Test
    @DisplayName("Should generate a headline containing only valid words from the predefined list")
    void generateHeadline_ValidWordsOnly() {

        String headline = newsContentGenerator.generateHeadline();
        assertThat(headline).isNotBlank();

        String[] words = headline.split(" ");
        assertThat(words).allMatch(NewsContentGenerator.WORDS::contains);
    }

    @Test
    @DisplayName("Should generate a headline with 3 to 5 words")
    void generateHeadline_WordCountInRange() {

        String headline = newsContentGenerator.generateHeadline();
        int wordCount = headline.split(" ").length;
        assertThat(wordCount).isBetween(3, 5);
    }

    @RepeatedTest(10)
    @DisplayName("Should generate different headlines in repeated calls")
    void generateHeadline_ShouldBeRandomized() {

        Set<String> uniqueHeadlines = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            uniqueHeadlines.add(newsContentGenerator.generateHeadline());
        }

        assertThat(uniqueHeadlines).hasSizeGreaterThan(1);
    }

    @Test
    @DisplayName("Should never return an empty headline")
    void generateHeadline_ShouldNeverBeEmpty() {

        String headline = newsContentGenerator.generateHeadline();
        assertThat(headline).isNotEmpty();
    }

    @Test
    @DisplayName("Should never generate a headline with more than 5 words")
    void generateHeadline_ShouldNotExceed5Words() {
        String headline = newsContentGenerator.generateHeadline();
        int wordCount = headline.split(" ").length;
        assertThat(wordCount).isLessThanOrEqualTo(5);
    }

    @Test
    @DisplayName("Should never generate a headline with less than 3 words")
    void generateHeadline_ShouldNotBeLessThan3Words() {
        String headline = newsContentGenerator.generateHeadline();
        int wordCount = headline.split(" ").length;
        assertThat(wordCount).isGreaterThanOrEqualTo(3);
    }

    @RepeatedTest(100)
    @DisplayName("Should generate a priority value between 0 and 9")
    void generatePriority_AlwaysInValidRange() {

        int priority = newsContentGenerator.generatePriority();
        assertThat(priority).isBetween(0, 9);
    }

    @Test
    @DisplayName("Should generate priorities with correct probability distribution")
    void generatePriority_ShouldFollowProbabilityDistribution() {

        final int totalSamples = 1000000;
        final int prioritiesSize = 10;
        int[] frequencyArray = new int[prioritiesSize];

        for (int i = 0; i < totalSamples; i++) {
            int priority = newsContentGenerator.generatePriority();
            frequencyArray[priority]++;
        }

        // Expected probability distribution (scaled to 1 Million samples)
        double[] expectedDistribution = new double[prioritiesSize];
        expectedDistribution[0] = 29.3;
        expectedDistribution[1] = 19.3;
        expectedDistribution[2] = 14.3;
        expectedDistribution[3] = 10.9;
        expectedDistribution[4] = 8.4;
        expectedDistribution[5] = 6.5;
        expectedDistribution[6] = 4.8;
        expectedDistribution[7] = 3.4;
        expectedDistribution[8] = 2.1;
        expectedDistribution[9] = 1.0;

        // Validate the observed distribution is within a small error margin (±0.3%)
        for (int priority = 0; priority < prioritiesSize; priority++) {

            double expectedPercentage = expectedDistribution[priority];
            double observedPercentage = (frequencyArray[priority] / (double) totalSamples) * 100;

            assertThat(observedPercentage)
                    .withFailMessage("Priority %d: Expected ~%.1f%% but got %.2f%%", priority, expectedPercentage, observedPercentage)
                    .isCloseTo(expectedPercentage, withinPercentage(3.0));
        }
    }
}