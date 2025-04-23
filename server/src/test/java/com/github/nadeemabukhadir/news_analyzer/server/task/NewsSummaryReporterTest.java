package com.github.nadeemabukhadir.news_analyzer.server.task;

import com.github.nadeemabukhadir.news_analyzer.server.storage.NewsItemStorage;
import com.github.nadeemabukhadir.news_analyzer.common.dto.NewsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsSummaryReporterTest {

    @Mock
    private NewsItemStorage storage;
    private NewsSummaryReporter reporter;

    @BeforeEach
    void setUp() {

        reporter = new NewsSummaryReporter(storage, 10);
    }

    @Test
    @DisplayName("Should handle empty data without errors and return 0")
    void testEmptyData() {

        when(storage.resetAndGetAll()).thenReturn(new TreeMap<>());

        int totalReported = reporter.report();

        verify(storage).resetAndGetAll();
        verifyNoMoreInteractions(storage);

        assertThat(totalReported).isZero();
    }

    @Test
    @DisplayName("Should limit to top 3 unique headlines but return total of 5 if 5 items exist")
    void testWithGivenData() {

        SortedMap<Integer, Deque<NewsItem>> data = new TreeMap<>(Comparator.reverseOrder());
        Deque<NewsItem> p9 = new ArrayDeque<>(4);
        p9.add(new NewsItem("headline 1", 9));
        p9.add(new NewsItem("headline 2", 9));
        p9.add(new NewsItem("headline 3", 9));
        p9.add(new NewsItem("headline 4", 9));
        Deque<NewsItem> p5 = new ArrayDeque<>(1);
        p5.add(new NewsItem("headline 5", 5));
        data.put(9, p9);
        data.put(5, p5);

        when(storage.resetAndGetAll()).thenReturn(data);

        int totalReported = reporter.report();

        verify(storage).resetAndGetAll();
        verifyNoMoreInteractions(storage);

        assertThat(totalReported).isEqualTo(5);
    }
}
