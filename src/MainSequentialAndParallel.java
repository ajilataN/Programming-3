import ImplementationModes.Parallel;
import ImplementationModes.Sequential;

import java.util.Timer;
import java.util.TimerTask;

import static Utils.Config.GREEN;
import static Utils.Config.RESET;

public class MainSequentialAndParallel {
    public static void main(String[] args) {
        // Default values
        String mode = null;
        String topics = null;
        int timeoutMinutes = 5;

        // Process command-line arguments
        for (String arg : args) {
            if (arg.startsWith("mode=")) {
                mode = arg.substring("mode=".length()).trim();
            } else if (arg.startsWith("topics=")) {
                topics = arg.substring("topics=".length()).trim();
            } else if (arg.startsWith("timeout=")) {
                try {
                    timeoutMinutes = Integer.parseInt(arg.substring("timeout=".length()).trim());
                } catch (NumberFormatException e) {
                    System.out.println("Invalid timeout value. Please enter a valid number for timeout.");
                    return;
                }
            }
            else {
                System.out.println("Unknown argument: " + arg);
                return;
            }
        }


        // Validate and process mode and topics
        if (mode == null || topics == null) {
            System.out.println("Mode options:");
            System.out.println(GREEN + "parallel" + RESET + " - ImplementationModes.Parallel processing mode");
            System.out.println(GREEN + "sequential" + RESET + " - ImplementationModes.Sequential processing mode");
            System.out.println("Example: mode=parallel topics=\"sport music\" timeout=10");
            return;
        }

        Timer timer = new Timer();
        int finalTimeoutMinutes = timeoutMinutes;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("Time's up! The program has reached the timeout of " + finalTimeoutMinutes + " minutes.");
                System.exit(0); // Exit the program
            }
        }, timeoutMinutes * 60 * 1000);

        switch (mode.toLowerCase()) {
            case "sequential":
                System.out.println("Running ImplementationModes.Sequential Mode");
                Sequential.connectAndSubscribe(topics);
                break;
            case "parallel":
                System.out.println("Running ImplementationModes.Parallel Mode");
                Parallel.connectAndSubscribe(topics);
                break;
            default:
                System.out.println("Invalid mode. Please use 'sequential' or 'parallel'.");
                break;
        }
    }
}
