package consoclient;

/// Runs a loading animation hiding a background process
/// Call `start` before beginning the process, then `stop` when process is complete
public class CosmeticOutputs {
    /// Thread to run the animation on
    private Thread thread;
    /// Boolean determining if animation should continue or keep going
    private volatile boolean running = false;

    /// Starts the animation
    /// @param message message to be shown alongside the animation
    public void loadingStart(String message) {
        running = true;
        thread = new Thread(() -> {
            String[] frames = {"    ", " .  ", " .. ", " ..."};
            int i = 0;
            while (running) {
                System.out.print("\033[2K\r" + message + frames[i % frames.length] + " ");
                i++;
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.start();
    }

    /// Starts the animation with custom loading frames
    /// @param message message to be shown alongside the animation
    /// @param frames custom animation frames
    public void loadingStart(String message, String[] frames) {
        running = true;
        thread = new Thread(() -> {
            int i = 0;
            while (running) {
                System.out.print("\033[2K\r" + message + frames[i % frames.length] + " ");
                i++;
                try {
                    Thread.sleep(400);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        thread.start();
    }

    /// Stops the running animation
    /// @param finalMessage message to be displayed when the animation is complete
    public void stop(String finalMessage) {
        running = false;
        try {
            thread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("\033[2K\r" + finalMessage);
    }

    /// Writes out a String to the console with a set delay between characters
    /// @param text the String to be written
    /// @param delay the delay between characters in milliseconds
    public void typeText(String text, int delay) {
        for (char c : text.toCharArray()) {
            System.out.print(c);
            System.out.flush();
            try {
                Thread.sleep(delay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println();
    }

    /// Cleans out everything currently on the console output
    public void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}
