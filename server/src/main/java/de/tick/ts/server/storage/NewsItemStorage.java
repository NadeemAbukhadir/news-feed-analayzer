package de.tick.ts.server.storage;

import de.tick.ts.common.dto.NewsItem;

import java.util.Deque;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Comparator.reverseOrder;

public class NewsItemStorage {

    // Store the map in an AtomicReference to swap it easily.
    private final AtomicReference<ConcurrentSkipListMap<Integer, Deque<NewsItem>>> storageRef;

    public NewsItemStorage() {
        // Descending order for priorities
        // Key  : News Priority
        // Value: News Aggregated by priority in a LIFO Deque where the newest stored in the front
        ConcurrentSkipListMap<Integer, Deque<NewsItem>> initialMap = new ConcurrentSkipListMap<>(reverseOrder());
        this.storageRef = new AtomicReference<>(initialMap);
    }

    /**
     * Adds a NewsItem to the thread-safe data structure.
     */
    public void add(NewsItem item) {

        if (item == null) {
            return; // avoid adding null values
        }

        ConcurrentSkipListMap<Integer, Deque<NewsItem>> map = storageRef.get();
        map.computeIfAbsent(item.getPriority(), priority -> new ConcurrentLinkedDeque<>())
                .push(item);
    }

    /**
     * Atomically replaces the internal map with a brand-new empty one
     * and returns the old map (snapshot).
     */
    public SortedMap<Integer, Deque<NewsItem>> resetAndGetAll() {

        // Map Cleanup: Assign a new empty map, and hand it over to the task for reporting
        ConcurrentSkipListMap<Integer, Deque<NewsItem>> newMap = new ConcurrentSkipListMap<>(reverseOrder());
        // Atomically swap: getAndSet returns the old map.
        return storageRef.getAndSet(newMap);
    }
}
