package consoclient;

import consoclient.data.Colours;

import java.util.Scanner;

/// Collection of various methods for providing pretty outputs to the user
/// As many methods as possible are static.
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

    /// Writes out a String to the console with a set delay between characters (uses default value of 30ms delay)
    /// @param text the String to be written
    public void typeText(String text) {
        typeText(text, 20);
    }

    /// Cleans out everything currently on the console output
    public void clearConsole() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /// Pauses the execution for the given number of milliseconds
    /// @param time pause time in milliseconds
    public void pause(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /// prints the game's title to the console
    /// credits to [here](https://patorjk.com/software/taag/#p=display&f=Doom&t=ConsoQuest&x=none&v=4&h=4&w=80&we=false) for the design
    public void showGameName() {
        System.out.println(Colours.CYAN + " _____                       _____                 _   ");
        System.out.println("/  __ \\                     |  _  |               | |  ");
        System.out.println("| /  \\/ ___  _ __  ___  ___ | | | |_   _  ___  ___| |_ ");
        System.out.println("| |    / _ \\| '_ \\/ __|/ _ \\| | | | | | |/ _ \\/ __| __|");
        System.out.println("| \\__/\\ (_) | | | \\__ \\ (_) \\ \\/' / |_| |  __/\\__ \\ |_");
        System.out.println(" \\____/\\___/|_| |_|___/\\___/ \\_/\\_\\\\__,_|\\___||___/\\__|");
        System.out.println("\n" + Colours.RESET);
    }

    /// builds and returns a string using the colours in the Colours class
    /// @param toColour String to have the colour applied to
    /// @param colour String containing the ANSI code of the desired colour
    /// @return built string with colour applied
    public String buildColouredString(String toColour, String colour) {
        return colour + toColour + Colours.RESET;
    }

    public void pressToContinue(boolean clearConsole) {
        typeText(buildColouredString("Press ENTER to continue...", Colours.GREY));

        System.console().readLine();

        if (clearConsole)
            clearConsole();
    }
}
