package ImplementationModes;

import Utils.MessageParser;
import Utils.ReviewCounter;
import Utils.SentimentAnalyzer;
import Utils.WebSocketConnection;

import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;

import static Utils.MessageParser.extractTopics;
import  static Utils.Config.*;

/**
 * Manages a sequential sentiment analysis system using WebSocket for real-time data.
 *
 * Functionality:
 * - Connects to a WebSocket server and subscribes to specified topics.
 * - Performs sentiment analysis on incoming review messages sequentially.
 * - Tracks and logs the number of reviews analyzed per second.
 *
 * Usage:
 * - Invoke `connectAndSubscribe(String message)` to start the connection and processing.
 */
public class Sequential {
    private static SentimentAnalyzer sentimentAnalyzer;
    private static final ReviewCounter analyzedReviewsCounter = new ReviewCounter(S_RESULT_FILE);
    private static AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    /**
     * Connects to the WebSocket server and subscribes to topics specified in the provided message.
     * Initializes the sentiment analyzer and sets up a shutdown hook to close the WebSocket connection.
     *
     * @param message Subscription message containing the topics to be analyzed.
     */
    public static void connectAndSubscribe(String message) {
        sentimentAnalyzer = new SentimentAnalyzer();
        WebSocketConnection connection = new WebSocketConnection(WEB_SOCKET_URL);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            WebSocket webSocket = webSocketRef.get();
            if (webSocket != null) {
                try {
                    System.out.println("Closing WebSocket connection...");
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutdown").join();
                } catch (Exception e) {
                    System.err.println("Error closing WebSocket connection: " + e.getMessage());
                }
            } else {
                System.out.println("No WebSocket to close.");
            }
        }));
        analyzedReviewsCounter.startCounting();
        connection.connect(new WebSocketListener(message));
    }
    /**
     * WebSocket listener that handles incoming messages and performs sentiment analysis.
     */
    public static class WebSocketListener implements WebSocket.Listener {
        private final String message;
        public WebSocketListener(String message) {
            this.message = message;
        }
        @Override
        public void onOpen(WebSocket webSocket) {
            System.out.println("WebSocket opened");
            webSocketRef.set(webSocket);
            String[] topics = extractTopics(message);

            for (String topic : topics) {
                webSocket.sendText("topic:" + topic.trim(), true);
            }
            webSocket.request(1);
        }

        @Override
        public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
            String reviewText = MessageParser.extractReviewText(data.toString());
            System.out.println("Review: "+ reviewText);
            if (reviewText != null) {
                String sentiment = sentimentAnalyzer.analyzeSentiment(reviewText);
                System.out.println(GREEN + "ImplementationModes.Sequential Sentiment Analysis: " + RESET + sentiment + GREEN + " Review text: " +RESET + reviewText);
                analyzedReviewsCounter.increment();
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
}