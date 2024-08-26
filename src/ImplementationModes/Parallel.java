package ImplementationModes;

import Utils.MessageParser;
import Utils.ReviewCounter;
import Utils.SentimentAnalyzer;
import Utils.WebSocketConnection;

import java.net.http.WebSocket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static Utils.MessageParser.extractTopics;
import static Utils.Config.*;
/**
 * Manages a parallel sentiment analysis system using WebSocket for real-time data.
 *
 * Functionality:
 * - Connects to a WebSocket server and subscribes to one or several topics.
 * - Analyzes review sentiments in parallel using a thread pool.
 * - Tracks and logs the number of reviews analyzed per second.
 */
public class Parallel{
    private static SentimentAnalyzer sentimentAnalyzer;
    private static final ExecutorService threadPool = createThreadPool();
    private static final ReviewCounter analyzedReviewsCounter = new ReviewCounter(P_RESULT_FILE);
    private static AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();

    /**
     * Connects to the WebSocket server and starts listening for messages.
     *
     * @param message Initial subscription message containing topics.
     */
    public static void connectAndSubscribe(String message) {
        sentimentAnalyzer = new SentimentAnalyzer();
        WebSocketConnection connection = new WebSocketConnection(WEB_SOCKET_URL);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            WebSocket webSocket = webSocketRef.get();
            if (webSocket != null) {
                synchronized (webSocketRef) {
                    try {
                        System.out.println("Closing WebSocket connection...");
                        webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown").join();
                    } catch (Exception e) {
                        System.err.println("Error closing WebSocket connection: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("No WebSocket to close.");
            }
        }));
        analyzedReviewsCounter.startCounting();
        connection.connect(new WebSocketListener(message));
    }

    /**
     * WebSocket listener for handling incoming messages and processing sentiment analysis.
     */
    public static class WebSocketListener implements WebSocket.Listener {
        private final String message;
        public WebSocketListener(String message){
            this.message = message;
        }
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket opened");
            webSocketRef.set(webSocket);
            String[] topics = extractTopics(message);
            synchronized (webSocketRef) {
                for (String topic : topics) {
                    webSocket.sendText("topic:" + topic.trim(), true);
                }
            }
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String reviewText = MessageParser.extractReviewText(data.toString());
            System.out.println("Review: "+reviewText);
            if (reviewText != null) {
                threadPool.submit(() -> {
                    String sentiment = sentimentAnalyzer.analyzeSentiment(reviewText);
                    System.out.println(GREEN + "ImplementationModes.Parallel Sentiment Analysis: " + RESET + sentiment + GREEN + " Review text: " +RESET + reviewText);
                    analyzedReviewsCounter.increment();

                });
            } else {
                System.out.println("Review text not found in the message.");
            }
            webSocket.request(1);
            return null;
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("WebSocket error: " + error.getMessage());
        }
    }

    /**
     * Creates a thread pool with a core and maximum pool size based on available processors.
     *
     * The pool uses a `SynchronousQueue` for task handling and `CallerRunsPolicy` for rejected tasks.
     *
     * @return An `ExecutorService` instance, specifically a `ThreadPoolExecutor`.
     */
    private static ExecutorService createThreadPool() {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        System.out.println("Available processors: "+availableProcessors);
        int corePoolSize = availableProcessors - 1;
        int maxPoolSize = availableProcessors * 2 - 1;

        System.out.println("Max pool size: "+maxPoolSize);

        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                60L,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
