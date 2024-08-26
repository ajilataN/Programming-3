import ImplementationModes.Distributed;

import java.util.Timer;
import java.util.TimerTask;

public class MainDistributed {

    private static Timer timer = new Timer();

    public static void main(String[] args) {
        int timeoutMinutes = 5;

        if (args.length == 0) {
            System.out.println("Please enter topics and timeout arguments.");
            System.out.println("Example: \"topics=music sport\" timeout=10");
            return;
        }

        String topics = null;
        for (String arg : args) {
            if (arg.startsWith("topics=")) {
                topics = arg.substring("topics=".length()).trim();
            } else if (arg.startsWith("timeout=")) {
                try {
                    timeoutMinutes = Integer.parseInt(arg.substring("timeout=".length()).trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid timeout value. Please enter a valid integer.");
                    return;
                }
            }
        }
        System.out.println("I have this timeout: "+timeoutMinutes);

        if (topics == null) {
            System.out.println("Topics not specified. Please provide topics in the format: \"topics=music sport\"");
            return;
        }

        TimerTask timeoutTask = new TimerTask() {
            @Override
            public void run() {
                System.out.println("Operation timed out. Terminating...");
                Distributed.shutdown();
                System.exit(0);
            }
        };
        timer.schedule(timeoutTask, timeoutMinutes * 60 * 1000L);

        Distributed.webConnectionAndTopicSubscription(topics, args);
        timer.cancel();
    }
}
