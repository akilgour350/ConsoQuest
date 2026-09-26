package consoclient;

import consoclient.data.Colours;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;

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

            GameInstanceHandler.setServerUrl(url);

            cosOuts.loadingStart("Checking server status");
            connected = GameInstanceHandler.checkStatus();
            cosOuts.stop(connected ? cosOuts.buildColouredString("Server connected!", Colours.GREEN) : cosOuts.buildColouredString("No server connection!", Colours.RED));

        } while (!connected);

        try {
            FileWriter fw = new FileWriter(Utils.getJarDirectory(App.class) + "/game/settings.config");
            fw.write("server-url=" + url);
            fw.close();
            System.out.println(Colours.GREEN + "Server URL saved!" + Colours.RESET);
            cosOuts.pause(2000);
            return true;

        } catch (IOException e) {
            System.out.println(Colours.RED + "Failed to save server URL!" + Colours.RESET);
            System.out.println("Error: " + e.getMessage());
            return false;
        }
    }
}
