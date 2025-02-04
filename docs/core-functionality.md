# Core Functionality Details

* [Core Functionality Details](#core-functionality-details)
    * [News Item Storage](#news-item-storage)
        * [Purpose & Overview](#purpose--overview)
        * [Data Structure Choice](#data-structure-choice)
        * [Why These Data Structures?](#why-these-data-structures)
        * [Concurrency & Read/Write Mechanism](#concurrency--readwrite-mechanism)
            * [Writes (Producers)](#writes-producers)
            * [Reads (Consumers)](#reads-consumers)
        * [Trade-Offs & Design Considerations](#trade-offs--design-considerations)
    * [Cumulative Distribution Function (CDF)](#cumulative-distribution-function-cdf)
        * [Code Explanation (Client Module -
          `NewsContentGenerator` Class)](#code-explanation-client-module---newscontentgenerator-class)
            * [Cumulative Threshold Array (Integer-Based)](#cumulative-threshold-array-integer-based)
            * [Priority Generation Using CDF](#priority-generation-using-cdf)

---

## News Item Storage

### Purpose & Overview

The **News Item Storage** is designed to efficiently store incoming news items from clients while ensuring safe
concurrent access. The goal is to:

- Organize news items by **priority**.
- Maintain **the newest items at the front** for quick access.
- Allow **multiple producers (clients) to write** simultaneously.
- Provide **safe and structured reads** for the scheduled reporter task.

### Data Structure Choice

To achieve efficient storage and retrieval, the implementation is based on a **thread-safe Sorted Map**:

- **Key:** `Integer` → Represents the **priority** of the news item (`0-9` where `9` is the highest priority).
- **Value:** `Deque<NewsItem>` → Represents a **deque (queue-like structure)** storing **news items in order**, where
  the **newest item is always at the front**.

### Why These Data Structures?

1. **ConcurrentSkipListMap (Thread-Safe Sorted Map)**
    - Ensures **news items are always retrieved in priority order** (`P9` → `P0`).
    - Allows **fast lookups** for specific priorities.
    - Supports **safe concurrent access**, as multiple `ClientHandler` instances may modify it simultaneously.

2. **ConcurrentLinkedDeque (Thread-Safe Deque)**
    - Provides **efficient insertion at the front** (newest items first).
    - Supports **fast retrieval** of the most recent items.
    - Prevents thread contention during reads/writes.

### Concurrency & Read/Write Mechanism

The system follows a **Producer-Consumer Model**:

#### Writes (Producers)

- **ClientHandler** instances (one per TCP connection) **continuously add news items** to the storage.
- Each news item is **grouped by priority** and stored in its respective `Deque<NewsItem>`.
- **ConcurrentLinkedDeque ensures fast & safe concurrent insertions** at the front.

#### Reads (Consumers)

- The `NewsSummaryReporter` **aggregates and reports news every 10 seconds**.
- The reporter **fetches all stored news items** using `resetAndGetAll()`:
    1. Takes a **snapshot** of the current `newsStorage` (atomic reference switch).
    2. Assigns a **new empty map** to `newsStorage` (write operations continue uninterrupted).
    3. Iterates over the **priority-order map pairs (`P9` → `P0`)** and **retrieves items** in **newest-first order** (
       from the `Deque`).

### Trade-Offs & Design Considerations

**Advantages**

- Ensures **safe concurrent read/write operations** without blocking threads.
- Provides **priority-based retrieval in natural order (`P9` → `P0`)**.
- Keeps **newest items readily available** for reporting.
- Prevents **risky simultaneous add/remove operations** during reporting.

**Trade-Offs**

- **Higher memory usage:** The storage holds **all received news items** until reporting occurs.
- **No fixed limit per priority:** If too many news items arrive, memory consumption may grow (a configurable limit
  could be introduced later).

---

## Cumulative Distribution Function (CDF)

In order to efficiently generate **priority weights (0-9)** based on a predefined **probability distribution**, I
implemented a **Cumulative Distribution Function (CDF)** approach. This method ensures that each priority level is
assigned based on its respective probability without costly floating-point operations.

### Code Explanation (Client Module - `NewsContentGenerator` Class)

#### Cumulative Threshold Array (Integer-Based)

```java
private static final int[] CUMULATIVE_THRESHOLDS = {
        293,  // P0: 0-292   (29.3%)
        486,  // P1: 293-485 (19.3%)
        629,  // P2: 486-628 (14.3%)
        738,  // P3: 629-737 (10.9%)
        822,  // P4: 738-821 (8.4%)
        887,  // P5: 822-886 (6.5%)
        935,  // P6: 887-934 (4.8%)
        969,  // P7: 935-968 (3.4%)
        990,  // P8: 969-989 (2.1%)
        1000  // P9: 990-999 (1.0%)
};
```

**Purpose:**

- This array defines **upper bounds** for each priority level.
- The thresholds are scaled by **10** (e.g., `29.3% → 293`)
  to **eliminate floating-point calculations**, making it **CPU-efficient**.
- **Interpretation:** If a **random number falls within the range of a threshold**, the corresponding priority is
  assigned.

#### Priority Generation Using CDF

```java
public int generatePriority() {
    int randomNumber = ThreadLocalRandom.current().nextInt(1000);
    for (int priority = 0; priority < CUMULATIVE_THRESHOLDS.length; priority++) {
        if (randomNumber < CUMULATIVE_THRESHOLDS[priority]) {
            return priority;
        }
    }
    return FALLBACK_PRIORITY;
}
```

**How It Works:**

1. A **random number (0-999)** is generated.
2. The loop iterates through `CUMULATIVE_THRESHOLDS` to find the **smallest threshold that the random number is less
   than**.
3. The **corresponding priority index is returned**.
4. If for some reason no priority is matched, a **fallback priority is returned** (although this should never happen).

---

[⬅ Back to Main README](../README.md)