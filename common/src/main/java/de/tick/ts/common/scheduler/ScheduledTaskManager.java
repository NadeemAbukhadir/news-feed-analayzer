package de.tick.ts.common.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages periodic execution of a task using single thread ScheduledExecutorService.
 * Ensures thread-safety and prevents duplicate scheduling via AtomicBoolean running flag.
 */
public class ScheduledTaskManager {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledTaskManager.class);
    public static final int SHUTDOWN_AWAIT_TIME = 5;

    private final ScheduledExecutorService scheduler;
    private final Runnable task;
    private final long initialDelay;
    private final long period;
    private final TimeUnit periodTimeUnit;
    private final AtomicBoolean running;

    public ScheduledTaskManager(Runnable task, long initialDelay, long period, TimeUnit periodTimeUnit) {

        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.task = task;
        this.initialDelay = initialDelay;
        this.period = period;
        this.periodTimeUnit = periodTimeUnit;
        this.running = new AtomicBoolean(false);
    }

    /**
     * Starts the scheduled task if it is not already running.
     */
    public void start() {

        if (running.compareAndSet(false, true)) {
            try {
                scheduler.scheduleAtFixedRate(task, initialDelay, period, periodTimeUnit);
                logger.info("Scheduled task started (Initial delay: {} {}, Period: {} {})",
                        initialDelay, periodTimeUnit, period, periodTimeUnit);
            } catch (RejectedExecutionException e) {

                logger.error("Failed to start scheduled task: ", e);
                running.set(false);
            }
        } else {
            logger.warn("Scheduled task is already running.");
        }
    }

    /**
     * Stops the scheduled task gracefully. Also, prevents duplicate shutdown.
     */
    public void stop() {

        if (running.compareAndSet(true, false)) {

            logger.info("Stopping scheduled task...");
            scheduler.shutdown();

            try {
                if (!scheduler.awaitTermination(SHUTDOWN_AWAIT_TIME, TimeUnit.SECONDS)) {
                    logger.warn("Forcing shutdown of scheduled task...");
                    scheduler.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                scheduler.shutdownNow();
            }
            logger.info("Scheduled task stopped.");
        } else {
            logger.warn("Scheduled task is not running.");
        }
    }
}
