package consoclient;

import consoclient.data.Colours;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Utils {
    /// Gets the directory of the currently running JAR file
    /// @param callingClass calling class (usually `App.class`)
    /// @return String containing the JAR directory
    public static String getJarDirectory(Class<?> callingClass) {
        try {
            return new File(URLDecoder.decode(callingClass.getProtectionDomain().getCodeSource().getLocation().getPath(), StandardCharsets.UTF_8)).getParentFile().getPath();
        } catch (Exception e) {
            System.out.println(Colours.RED + "Error decoding JAR path: " + e.getMessage() + Colours.RESET);
            return null;
        }
    }

    /// Lets the user configure the server URL
    public static boolean setServerURL(Scanner scanner, CosmeticOutputs cosOuts) {
        boolean connected = false;
        String url = "";

        do {
            System.out.print("Enter a server URL or 'exit' " + cosOuts.buildColouredString("[https://conso.akilgour.com/api]", Colours.GREY) + ": ");
            url = scanner.nextLine();
            if (url.isEmpty()) {
                url = "https://conso.akilgour.com/api";
            }

            if (Objects.equals(url, "exit")) {
                return false;
            }

            GIH.setServerUrl(url);
            System.out.print("\n");

            try {
                cosOuts.loadingStart("Checking server status");
                connected = GIH.checkStatus();
                cosOuts.stop(connected ? cosOuts.buildColouredString("Server connected!", Colours.GREEN) : cosOuts.buildColouredString("No server connection!", Colours.RED));
            } catch (Exception e) {
                cosOuts.displayError(e.getMessage());
            }

        } while (!connected);

        try {
            FileWriter fw = new FileWriter(Utils.getJarDirectory(App.class) + "/game/settings.config");
            fw.write("server-url=" + url);
            fw.close();
            System.out.println(Colours.GREEN + "Server URL saved!" + Colours.RESET);
            cosOuts.pressToContinue(true);
            return true;

        } catch (IOException e) {
            System.out.println(Colours.RED + "Failed to save server URL!" + Colours.RESET);
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }

    /// Builds and shows a menu to the user
    /// @param options Map of options where Key is the text displayed and Value is the code to be executed on completion
    /// @param prompts Array of prompts for use. The positions correspond to:
    ///                0. Initial (e.g. "Pick an option")
    ///                1. Invalid input (e.g. "enter a number between 1 and 5")
    ///                2. Error (e.g. "Something went wrong")
    /// @param scanner scanner used by main to avoid conflicts
    /// @param cosOuts instance of CosmeticOutputs to avoid instantiating a new one
    /// @return code to be executed for the selected option in the form of a Runnable
    public static Runnable showMenu(LinkedHashMap<String, Runnable> options, String[] prompts, Scanner scanner, CosmeticOutputs cosOuts) {
        ArrayList<String> optionStrings = new ArrayList<>(options.keySet());
        String inputStr = "";
        int input = 0;

        for (int i = 0; i < optionStrings.size(); i++) {
            System.out.println((i + 1) + ". " + optionStrings.get(i));
        }

        do {
            System.out.print(cosOuts.buildColouredString("\n" + prompts[0], Colours.PURPLE));

            try {
                inputStr = scanner.nextLine();
                input = Integer.parseInt(inputStr);

                if (input < 1 || input > optionStrings.size()) {
                    System.out.println(cosOuts.buildColouredString(prompts[1], Colours.YELLOW));
                    continue;
                }

                return options.get(optionStrings.get(input - 1));
            } catch (Exception e) {
                cosOuts.typeText(cosOuts.buildColouredString("That wasn't a number!", Colours.YELLOW));
            }
        } while (input < 1 || input > optionStrings.size()); // no idea why IntelliJ thinks this is always true but whatever lol

        return null;
    }
}
