package ImplementationModes;

import Utils.MessageParser;
import Utils.ReviewCounter;
import Utils.SentimentAnalyzer;
import Utils.WebSocketConnection;
import mpi.MPI;
import mpi.MPIException;
import mpi.Request;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicReference;
import static Utils.Config.*;
import static Utils.MessageParser.extractTopics;

public class Distributed {
    private static final int MASTER = 0;
    private static AtomicReference<WebSocket> webSocketRef = new AtomicReference<>();
    private static Thread webSocketThread;
    private static MasterWebSocketListener masterListener;

    /**
     * Initialization of the MPI environment, managing the websocket connection and
     * manages processes based on the MPI rank.
     *
     * @param message the message to send via the websocket
     * @param args the arguments for initializing the MPI environment
     *
     * @throws MPIException if error occurs during MPI initialization or finalization
     * @throws InterruptedException if the thread is interrupted while waiting for the WebSocket thread
     */
    public static void webConnectionAndTopicSubscription(String message, String[] args) {
        try {
            MPI.Init(args);
            int rank = MPI.COMM_WORLD.Rank();
            int size = MPI.COMM_WORLD.Size();

            if (rank == MASTER) {
                webSocketThread = new Thread(() -> {
                    WebSocketConnection connection = new WebSocketConnection(WEB_SOCKET_URL);
                    masterListener = new MasterWebSocketListener(message, size);
                    connection.connect(masterListener);
                });

                webSocketThread.start();
                webSocketThread.join();

            } else {
                new WorkerProcess().start();
            }
        } catch (MPIException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            try {
                MPI.Finalize();
                System.out.println("MPI finalized successfully.");
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles WebSocket communication for the master process.
     * Implements the {@link WebSocket.Listener} interface to process incoming messages,
     * distribute work to MPI workers, and collect results.
     */
    public static class MasterWebSocketListener implements WebSocket.Listener {
        private final String message;
        private final int numWorkerProcesses;
        private final ReviewCounter analyzedReviewsCounter = new ReviewCounter(D_RESULT_FILE);
        private final List<String> reviewBuffer = new ArrayList<>();

        public MasterWebSocketListener(String message, int numWorkers) {
            this.message = message;
            this.numWorkerProcesses = numWorkers;
            analyzedReviewsCounter.startCounting();
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
            if (reviewText != null) {
                reviewBuffer.add(reviewText);
                if (reviewBuffer.size() == numWorkerProcesses - 1) {
                    distributeWork();
                    collectResults();
                    reviewBuffer.clear();
                }
            } else {
                System.out.println("Review text not found in the message.");
            }
            webSocket.request(1);
            return null;
        }

        private void distributeWork() {
            try {
                System.out.println("Distributing " + reviewBuffer.size() + " reviews.");
                for (int i = 1; i < numWorkerProcesses; i++) {
                    String reviewText = reviewBuffer.get(i - 1);
                    MPI.COMM_WORLD.Send(reviewText.toCharArray(), 0, reviewText.length(), MPI.CHAR, i, 0);
                }
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }

        private void collectResults() {
            try {
                for (int i = 1; i < numWorkerProcesses; i++) {
                    char[] resultBuffer = new char[4096];
                    MPI.COMM_WORLD.Recv(resultBuffer, 0, 4096, MPI.CHAR, i, 0);
                    String result = new String(resultBuffer).trim();
                    if (!result.isEmpty()) {
                        System.out.println(GREEN + "Review and Sentiment (Worker " + i + "): " + RESET + result);
                        analyzedReviewsCounter.increment();
                    } else {
                        System.out.println("No result received from worker " + i);
                    }
                }
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onError(WebSocket webSocket, Throwable error) {
            System.out.println("WebSocket error: " + error.getMessage());
        }
        public void stopReviewCounter() {
            analyzedReviewsCounter.stop();
        }

        private void sendShutdownSignal() {
            try {
                for (int i = 1; i < numWorkerProcesses; i++) {
                    String shutdownMessage = "shutdown";
                    MPI.COMM_WORLD.Send(shutdownMessage.toCharArray(), 0, shutdownMessage.length(), MPI.CHAR, i, 0);
                }
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Starts the worker process that continuously listens for review texts from the master process,
     * analyzes their sentiment, and sends the results back to the master.
     * <p>
     * The worker process performs the following:
     * <ul>
     *     <li>Receives a review text from the master process using MPI.</li>
     *     <li>Uses {@link SentimentAnalyzer} to analyze the sentiment of the received review.</li>
     *     <li>Sends the analyzed sentiment, with the review text, back to the master process.</li>
     * </ul>
     * This loop runs indefinitely, processing reviews as long as they are received.
     * </p>
     *
     * @throws MPIException if an error occurs during MPI communication
     */
    public static class WorkerProcess {
        public void start() {
            try {
                SentimentAnalyzer sentimentAnalyzer = new SentimentAnalyzer();
                while (true) {
                    char[] reviewBuffer = new char[4096];
                    MPI.COMM_WORLD.Recv(reviewBuffer, 0, 4096, MPI.CHAR, MASTER, 0);

                    String reviewText = new String(reviewBuffer).trim();
                    if (reviewText.equals("shutdown")) {
                        System.out.println("Worker " + MPI.COMM_WORLD.Rank() + " received shutdown signal.");
                        break;
                    }

                    if (!reviewText.isEmpty()) {
                        String sentiment = sentimentAnalyzer.analyzeSentiment(reviewText);
                        String result = "Review: " + reviewText + " | Sentiment: " + sentiment;
                        MPI.COMM_WORLD.Send(result.toCharArray(), 0, result.length(), MPI.CHAR, MASTER, 0);
                    }
                }
            } catch (MPIException e) {
                e.printStackTrace();
            }
        }
    }

    public static void shutdown() {
        if (masterListener != null) {
            masterListener.stopReviewCounter();
        }

        WebSocket webSocket = webSocketRef.get();
        if (webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Shutting down").thenRun(() -> {
                System.out.println("WebSocket closed");
            });
        }

        try {
            if (MPI.COMM_WORLD.Rank() == MASTER) {
                System.out.println("Master is shutting down...");
                masterListener.sendShutdownSignal();

            } else {
                System.out.println("Worker " + MPI.COMM_WORLD.Rank() + " is shutting down...");
            }

            MPI.Finalize();
        } catch (MPIException e) {
            e.printStackTrace();
        }

        if (webSocketThread != null) {
            webSocketThread.interrupt();
            try {
                webSocketThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
