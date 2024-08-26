package Utils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
/**
 * Manages a WebSocket connection to a specified URI.
 *
 * Methods:
 * - connect: Establishes an asynchronous WebSocket connection with the given listener.
 * - keepAlive: Keeps the connection alive by blocking the current thread indefinitely using a CountDownLatch.
 */
public class WebSocketConnection {
    private final URI uri;
    private final CountDownLatch latch = new CountDownLatch(1);
    public WebSocketConnection(String uri) {
        this.uri = URI.create(uri);
    }
    /**
     * Connects to the WebSocket server using the provided listener.
     *
     * @param listener WebSocket.Listener that handles incoming messages and events.
     * @return The established WebSocket connection.
     */
    public WebSocket connect(WebSocket.Listener listener) {
        HttpClient client = HttpClient.newHttpClient();
        CompletableFuture<WebSocket> webSocketFuture = client.newWebSocketBuilder()
                .buildAsync(uri, listener);
        keepAlive();
        return webSocketFuture.join();
    }
    public void keepAlive() {
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
