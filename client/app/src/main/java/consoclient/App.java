package consoclient;

import consoclient.data.Colours;
import org.jspecify.annotations.NonNull;

import javax.swing.plaf.ColorUIResource;
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

    public static void main(String[] args) {
        // GAME INITIALISATION =============================================================================================
        cosOuts.clearConsole();
        System.out.println(Colours.FONT_BLUE + "Initialising ConsoQuest:" + Colours.FONT_RESET);

        // GET SERVER URL
        cosOuts.loadingStart("· Getting server URL");
        File scf = new File(getJarDirectory(App.class) + "settings.config");
        boolean foundUrl = false;
        try (Scanner fileReader = new Scanner(scf)) {
            while (fileReader.hasNextLine()) {
                String data = fileReader.nextLine();
                if (data.contains("server-url")) {
                    APIHandler.setServerUrl(data.split("=")[1]);
                    foundUrl = true;
                }
            }

            if (foundUrl) {
                cosOuts.stop(Colours.FONT_GREEN + "· Server URL loaded!" + Colours.FONT_RESET);
            } else {
                cosOuts.stop(Colours.FONT_RED + "· No server URL found!" + Colours.FONT_RESET);
            }
        } catch (FileNotFoundException e) {
            cosOuts.stop(Colours.FONT_RED + "· No server URL found!" + Colours.FONT_RESET);
        }

        // RUN SERVER CONNECTION TEST IF URL FOUND
        boolean connected = false;
        if (foundUrl) {
            cosOuts.loadingStart("· Checking server status");
            connected = APIHandler.checkStatus();
            cosOuts.stop(connected ? Colours.FONT_GREEN + "· Server connected!" + Colours.FONT_RESET : Colours.FONT_RED + "· No server connection!" + Colours.FONT_RESET);
        } else {
            System.out.println(Colours.FONT_YELLOW + "· Skipping server connection test!" + Colours.FONT_RESET);
        }

        // PAUSE SO USER CAN SEE LOADING RESULTS
        cosOuts.pause(2000);

        cosOuts.clearConsole();

        // MAIN MENU INIT ==============================================================================================
        boolean validInput = false; // for determining when to quit the main menu and do something
        int input = -1; // the user's actual keyboard input

        // create an arraylist with all the menu options with a corresponding function to execute
        ArrayList<Map.Entry<String, Runnable>> menuOptions = buildMenuOptions(connected);

        // MAIN MENU DISPLAY ===========================================================================================
        // loop til valid input
        do {
            System.out.println(Colours.FONT_BLUE + "<> " + Colours.FONT_RESET + " ConsoQuest! " + Colours.FONT_BLUE + "<>" + Colours.FONT_RESET + "\n");

            // print the options
            for (int i = 0; i < menuOptions.size(); i++) {
                System.out.println((i + 1) + ": " + menuOptions.get(i).getKey());
            }

            try {
                System.out.print(Colours.FONT_GREEN + "Pick an option: " + Colours.FONT_RESET);
                input = scanner.nextInt();

                if (input >= 1 && input <= 9) {
                    validInput = true;
                }
            } catch (Exception ex) {
                System.out.println(Colours.FONT_RED + "INVALID INPUT!" + Colours.FONT_RESET);
                scanner.next();
            }
        } while (!validInput);

        scanner.nextLine();

        menuOptions.get(input - 1).getValue().run();
    }

    /// Generated the ArrayList of main menu options and corresponding method calls
    /// @param connected if a server connection was successfully established
    /// @return the ArrayList of menu options
    private static @NonNull ArrayList<Map.Entry<String, Runnable>> buildMenuOptions(boolean connected) {
        LinkedHashMap<String, Runnable> menuOptions = new LinkedHashMap<>(); // use a LinkedHashMap as it's to start like this and insert values then convert later

        if (connected) { // only happens if server connection was established
            menuOptions.put("New Game", () -> System.out.println("New game picked")); // start a new game
            menuOptions.put("Load Game", () -> System.out.println("Load game picked")); // load an existing game (prompts user to log in)

        } else { // only happens if server connection failed
            menuOptions.put("Configure Server", App::setServerURL); // quick option for users to enter a server address
        }

        // these options are always available
        menuOptions.put("Settings", () -> System.out.println("Settings picked"));

        return new ArrayList<>(menuOptions.entrySet()); // convert the LinkedHashMap to an ArrayList and return
    }

    public static String getJarDirectory(Class<?> callingClass) {
        try {
            String path = callingClass.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decodedPath = URLDecoder.decode(path, StandardCharsets.UTF_8);
            File jarFile = new File(decodedPath);
            return jarFile.getParentFile().getPath();
        } catch (Exception e) {
            System.err.println("Error decoding JAR path: " + e.getMessage());
            return null;
        }
    }

    private static void setServerURL() {
        boolean connected = false;
        String url = "";

        do {
            System.out.print("Enter the server URL or 'exit' " + Colours.FONT_GREY + "[conso.akilgour.com]" + Colours.FONT_RESET + ": ");
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
            cosOuts.stop(connected ? Colours.FONT_GREEN + "Server connected!" + Colours.FONT_RESET : Colours.FONT_RED + "No server connection!" + Colours.FONT_RESET);

            // PAUSE SO USER CAN SEE LOADING RESULTS
            cosOuts.pause(3000);

            cosOuts.clearConsole();
        } while (!connected);

        try {
            FileWriter fw = new FileWriter(getJarDirectory(App.class) + "settings.config");
            fw.write("server-url=" + url);
            fw.close();
            System.out.println(Colours.FONT_GREEN + "Server URL saved!" + Colours.FONT_RESET);
        } catch (IOException e) {
            System.out.println(Colours.FONT_RED + "Failed to save server URL!" + Colours.FONT_RESET);
        }
    }
}
