package consoclient;

import consoclient.data.Colours;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class App {
    static CosmeticOutputs cosOuts = new CosmeticOutputs();
    static Scanner scanner = new Scanner(System.in);
    static boolean connected = false;

    public static void main(String[] args) {
        //<editor-fold desc="GAME INIT">

        // GAME INITIALISATION =============================================================================================
        cosOuts.clearConsole();
        System.out.println(Colours.FONT_BLUE + "Initialising ConsoQuest..." + Colours.RESET);

        // GET SERVER URL
        String workingDir = getJarDirectory(App.class);
        boolean foundUrl = false;

        if (workingDir != null) {
            System.out.println(Colours.FONT_GREEN + "· Working directory: " + workingDir + Colours.RESET);

            cosOuts.loadingStart("· Getting server URL");
            File scf = new File(workingDir + "settings.config");

            try (Scanner fileReader = new Scanner(scf)) {
                while (fileReader.hasNextLine()) {
                    String data = fileReader.nextLine();
                    if (data.contains("server-url")) {
                        APIHandler.setServerUrl(data.split("=")[1]);
                        foundUrl = true;
                    }
                }
                if (foundUrl) {
                    cosOuts.stop(Colours.FONT_GREEN + "· Server URL loaded!" + Colours.RESET);
                } else {
                    cosOuts.stop(Colours.FONT_RED + "· No server URL found!" + Colours.RESET);
                }
            } catch (FileNotFoundException e) {
                cosOuts.stop(Colours.FONT_RED + "· No server URL found!" + Colours.RESET);
            }
        } else {
            System.out.println(Colours.FONT_RED + "· Failed to get working directory!" + Colours.RESET);
        }

        // RUN SERVER CONNECTION TEST IF URL FOUND
        if (foundUrl) {
            cosOuts.loadingStart("· Checking server status");
            connected = APIHandler.checkStatus();
            cosOuts.stop(connected ? Colours.FONT_GREEN + "· Server connected!" + Colours.RESET : Colours.FONT_RED + "· No server connection!" + Colours.RESET);
        } else {
            System.out.println(Colours.FONT_YELLOW + "· Skipping server connection test!" + Colours.RESET);
        }

        System.out.println("Initialisation complete!");
        // PAUSE SO USER CAN SEE LOADING RESULTS
        cosOuts.pause(1500);

        //</editor-fold>

        //<editor-fold desc="MAIN MENU">

        // MAIN MENU INIT ==============================================================================================
        int input = -1; // the user's actual keyboard input

        // MAIN MENU DISPLAY ===========================================================================================
        // loop til valid input
        do {
            boolean validInput = false;

            cosOuts.clearConsole();
            // create an arraylist with all the menu options with a corresponding function to execute
            ArrayList<Map.Entry<String, Runnable>> menuOptions = buildMenuOptions();

            do {
                cosOuts.showGameName();

                // print the options
                for (int i = 0; i < menuOptions.size(); i++) {
                    System.out.println((i + 1) + ": " + menuOptions.get(i).getKey());
                }

                try {
                    System.out.print(Colours.FONT_CYAN + "\nPick an option: " + Colours.RESET);
                    input = scanner.nextInt();

                    if (input >= 1 && input <= 9) {
                        validInput = true;
                    }
                } catch (Exception ex) {
                    System.out.println(Colours.FONT_RED + "Invalid input!" + Colours.RESET);
                    cosOuts.pause(1000);
                    cosOuts.clearConsole();
                    scanner.next();
                }
            } while (!validInput);

            scanner.nextLine();
            menuOptions.get(input - 1).getValue().run();
        } while (true); // if we're leaving this loop the user closed the game
        //</editor-fold>
    }

    /// Generated the ArrayList of main menu options and corresponding method calls
    /// @return the ArrayList of menu options
    private static @NonNull ArrayList<Map.Entry<String, Runnable>> buildMenuOptions() {
        LinkedHashMap<String, Runnable> menuOptions = new LinkedHashMap<>(); // use a LinkedHashMap as it's to start like this and insert values then convert later

        if (connected) { // only happens if server connection was established
            menuOptions.put("New Game", () -> System.out.println("New game picked")); // start a new game
            menuOptions.put("Load Game", () -> System.out.println("Load game picked")); // load an existing game (prompts user to log in)

        } else { // only happens if server connection failed
            menuOptions.put("Configure Server", App::setServerURL); // quick option for users to enter a server address
        }

        // these options are always available
        menuOptions.put("Settings", () -> System.out.println("Settings picked"));
        menuOptions.put("Quit", () -> System.exit(0));

        return new ArrayList<>(menuOptions.entrySet()); // convert the LinkedHashMap to an ArrayList and return
    }

    /// Gets the directory of the currently running JAR file
    /// @param callingClass calling class (usually `App.class`)
    /// @return String containing the JAR directory
    public static String getJarDirectory(Class<?> callingClass) {
        try {
            return new File(URLDecoder.decode(callingClass.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8)).getParentFile().getPath();
        } catch (Exception e) {
            System.out.println(Colours.FONT_RED + "Error decoding JAR path: " + e.getMessage() + Colours.RESET);
            return null;
        }
    }

    /// Lets the user configure the server URL
    private static void setServerURL() {
        String url = "";

        do {
            System.out.print("Enter the server URL or 'exit' " + Colours.FONT_GREY + "[https://conso.akilgour.com/api]" + Colours.RESET + ": ");
            url = scanner.nextLine();
            if (url.isEmpty()) {
                url = "https://conso.akilgour.com/api";
            }

            if (Objects.equals(url, "exit")) {
                return;
            }

            APIHandler.setServerUrl(url);

            cosOuts.loadingStart("Checking server status");
            connected = APIHandler.checkStatus();
            cosOuts.stop(connected ? Colours.FONT_GREEN + "Server connected!" + Colours.RESET : Colours.FONT_RED + "No server connection!" + Colours.RESET);

            cosOuts.clearConsole();
        } while (!connected);

        try {
            FileWriter fw = new FileWriter(getJarDirectory(App.class) + "settings.config");
            fw.write("server-url=" + url);
            fw.close();
            System.out.println(Colours.FONT_GREEN + "Server URL saved!" + Colours.RESET);
            cosOuts.pause(2000);
        } catch (IOException e) {
            System.out.println(Colours.FONT_RED + "Failed to save server URL!" + Colours.RESET);
        }
    }
}
