package Utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Tracks the number of reviews analyzed per second and logs the count a file.
 *
 * Methods:
 * - startCounting: Starts tracking and logging the review count every second.
 * - increment: Increments the review count.
 * - stop: Stops the scheduled counting task.
 */
public class ReviewCounter {
    private final AtomicInteger reviewCount = new AtomicInteger(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final String outputFilePath;
    public ReviewCounter(String outputFilePath) {
        this.outputFilePath = outputFilePath;
    }
    /**
     * Starts counting reviews and logs the rate every second.
     */
    public void startCounting() {
        scheduler.scheduleAtFixedRate(this::printAndSaveRate, 1, 1, TimeUnit.SECONDS);
    }
    /**
     * Increments the review count.
     */
    public void increment() {
        reviewCount.incrementAndGet();
    }
    private void printAndSaveRate() {
        int count = reviewCount.getAndSet(0);
        String message = "Analyzed Reviews per Second: " + count;
        System.out.println(message);
        saveToFile(message);
    }
    private synchronized void saveToFile(String message) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath, true))) {
            writer.write(message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
    /**
     * Stops the counting process.
     */
    public void stop() {
        scheduler.shutdown();
    }
}
