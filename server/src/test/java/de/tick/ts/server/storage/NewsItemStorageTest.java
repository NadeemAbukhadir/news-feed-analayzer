package de.tick.ts.server.storage;

import de.tick.ts.common.dto.NewsItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Deque;
import java.util.SortedMap;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class NewsItemStorageTest {

    private NewsItemStorage storage;

    @BeforeEach
    void setUp() {

        storage = new NewsItemStorage();
    }

    @Test
    @DisplayName("Should ignore null NewsItem in add method")
    void shouldIgnoreNullNewsItem() {

        storage.add(null);
        SortedMap<Integer, Deque<NewsItem>> snapshot = storage.resetAndGetAll();
        assertThat(snapshot).isEmpty();
    }

    @Test
    @DisplayName("Should add NewsItem to the correct priority stack")
    void shouldAddNewsItemToCorrectPriorityStack() {

        NewsItem item1 = new NewsItem("up rise success", 5);
        NewsItem item2 = new NewsItem("good high fall", 5);
        NewsItem item3 = new NewsItem("bad low", 3);

        storage.add(item1);
        storage.add(item2);
        storage.add(item3);

        SortedMap<Integer, Deque<NewsItem>> snapshot = storage.resetAndGetAll();
        assertThat(snapshot).hasSize(2); // Priorities: 5 and 3 should exist
        assertThat(snapshot.get(5)).containsExactly(item2, item1); // LIFO (Stack-like)
        assertThat(snapshot.get(3)).containsExactly(item3);
    }

    @Test
    @DisplayName("Should retrieve and reset the storage atomically")
    void shouldResetAndGetAllAtomically() {

        NewsItem item1 = new NewsItem("über rise success", 7);
        NewsItem item2 = new NewsItem("good fall", 2);
        storage.add(item1);
        storage.add(item2);

        SortedMap<Integer, Deque<NewsItem>> snapshot = storage.resetAndGetAll();

        assertThat(snapshot).hasSize(2);
        assertThat(snapshot.get(7)).containsExactly(item1);
        assertThat(snapshot.get(2)).containsExactly(item2);

        assertThat(storage.resetAndGetAll()).isEmpty();
    }
}
