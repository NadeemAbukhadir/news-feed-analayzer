package com.github.nadeemabukhadir.news_analyzer.common.mapper;

import com.github.nadeemabukhadir.news_analyzer.common.dto.NewsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.github.nadeemabukhadir.news_analyzer.common.Constants.NEWS_HEADLINE_SEPARATOR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class NewsItemMapperTest {

    private NewsItemMapper mapper;

    @BeforeEach
    void setUp() {

        mapper = new NewsItemMapper();
    }

    @Test
    @DisplayName("Should correctly map a NewsItem to a formatted string")
    void toString_ValidNewsItem_ReturnsFormattedString() {

        NewsItem newsItem = new NewsItem("up rise success", 5);
        String result = mapper.toString(newsItem);

        assertThat(result).isEqualTo("up rise success" + NEWS_HEADLINE_SEPARATOR + "5");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when mapping null NewsItem to string")
    void toString_NullNewsItem_ThrowsException() {

        assertThatThrownBy(() -> mapper.toString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("NewsItem cannot be null");
    }

    @Test
    @DisplayName("Should correctly parse a valid formatted string into a NewsItem object")
    void fromString_ValidString_ReturnsNewsItem() {

        String input = "up rise success" + NEWS_HEADLINE_SEPARATOR + "5";
        NewsItem newsItem = mapper.fromString(input);

        assertThat(newsItem).isNotNull();
        assertThat(newsItem.getHeadline()).isEqualTo("up rise success");
        assertThat(newsItem.getPriority()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input string is null")
    void fromString_NullInput_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid message format");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input string is empty")
    void fromString_EmptyInput_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid message format");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input string does not contain separator")
    void fromString_MissingSeparator_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString("up rise success 5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid message format");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when priority is not a number")
    void fromString_InvalidPriority_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString("up rise success" + NEWS_HEADLINE_SEPARATOR + "abc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid priority value");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when input string contains multiple separators")
    void fromString_TooManySeparators_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString("up" + NEWS_HEADLINE_SEPARATOR + "rise" + NEWS_HEADLINE_SEPARATOR + "success" + NEWS_HEADLINE_SEPARATOR + "5"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid message format");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when priority is greater than 9")
    void fromString_PriorityGreaterThan9_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString("good rise success" + NEWS_HEADLINE_SEPARATOR + "10"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Priority must be between 0 and 9");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when priority is negative")
    void fromString_PriorityLessThan0_ThrowsException() {

        assertThatThrownBy(() -> mapper.fromString("failure bad fall" + NEWS_HEADLINE_SEPARATOR + "-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Priority must be between 0 and 9");
    }

    @Test
    @DisplayName("Should correctly parse a NewsItem with edge priority values (0 and 9)")
    void fromString_ValidEdgePriorities_ReturnsNewsItem() {

        NewsItem lowPriority = mapper.fromString("low success fall" + NEWS_HEADLINE_SEPARATOR + "0");
        NewsItem highPriority = mapper.fromString("high rise good" + NEWS_HEADLINE_SEPARATOR + "9");

        assertThat(lowPriority.getPriority()).isZero();
        assertThat(highPriority.getPriority()).isEqualTo(9);
    }
}
