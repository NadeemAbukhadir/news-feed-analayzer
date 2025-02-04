package de.tick.ts.server;

import de.tick.ts.server.util.NewsHeadlineUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NewsHeadlineUtilTest {

    @Test
    @DisplayName("Should return true for a headline with all positive words")
    void isPositive_AllPositiveWords() {

        assertThat(NewsHeadlineUtil.isPositive("up rise success")).isTrue();
    }

    @Test
    @DisplayName("Should return false for a headline with no positive words")
    void isPositive_AllNegativeWords() {

        assertThat(NewsHeadlineUtil.isPositive("fall bad failure")).isFalse();
    }

    @Test
    @DisplayName("Should return true when more than 50% of words are positive")
    void isPositive_MoreThan50PercentPositive() {

        assertThat(NewsHeadlineUtil.isPositive("up rise bad")).isTrue(); // around 66%
        assertThat(NewsHeadlineUtil.isPositive("success good low failure")).isTrue(); // 50%
    }

    @Test
    @DisplayName("Should return true when exactly 50% of words are positive")
    void isPositive_Exactly50PercentPositive() {

        assertThat(NewsHeadlineUtil.isPositive("rise success failure low")).isTrue(); // 50%
        assertThat(NewsHeadlineUtil.isPositive("good fall bad high")).isTrue(); // 50%
    }

    @Test
    @DisplayName("Should return false when less than 50% of words are positive")
    void isPositive_LessThan50PercentPositive() {

        assertThat(NewsHeadlineUtil.isPositive("fall success bad")).isFalse(); // around 33%
        assertThat(NewsHeadlineUtil.isPositive("low failure rise")).isFalse(); // around 33%
    }

    @Test
    @DisplayName("Should return false if headline contains only unknown words")
    void isPositive_UnknownWords() {

        assertThat(NewsHeadlineUtil.isPositive("random words test")).isFalse();
        assertThat(NewsHeadlineUtil.isPositive("hello world java")).isFalse();
    }

    @Test
    @DisplayName("Should return true if headline contains unknown words but still meets positivity threshold")
    void isPositive_UnknownWordsButPositive() {

        assertThat(NewsHeadlineUtil.isPositive("rise foo bar success")).isTrue(); // 50%
        assertThat(NewsHeadlineUtil.isPositive("up unknown test good")).isTrue(); // 50%
    }

    @Test
    @DisplayName("Should correctly classify headlines of different lengths")
    void isPositive_ValidWordCounts() {

        assertThat(NewsHeadlineUtil.isPositive("up rise success")).isTrue(); // 3 words
        assertThat(NewsHeadlineUtil.isPositive("low rise failure success")).isTrue(); // 4 words
        assertThat(NewsHeadlineUtil.isPositive("up rise success fall bad")).isTrue(); // 5 words
    }

    @Test
    @DisplayName("Should return false for null, empty, or whitespace-only headlines")
    void isPositive_NullOrEmpty() {

        assertThat(NewsHeadlineUtil.isPositive(null)).isFalse();
        assertThat(NewsHeadlineUtil.isPositive("")).isFalse();
        assertThat(NewsHeadlineUtil.isPositive("    ")).isFalse();
    }
}
