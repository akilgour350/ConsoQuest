package consoclient;

import consoclient.data.Colours;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class App {
    static CosmeticOutputs cosOuts = new CosmeticOutputs();
    static Scanner scanner = new Scanner(System.in);
    static boolean connected = false;

    public static void main(String[] args) {
        //<editor-fold desc="GAME INIT">

        // GAME INITIALISATION =============================================================================================
        cosOuts.clearConsole();
        System.out.println(Colours.BLUE + "Initialising ConsoQuest..." + Colours.RESET);

        // GET SERVER URL
        String workingDir = Utils.getJarDirectory(App.class);
        boolean foundUrl = false;

        if (workingDir != null) {
            System.out.println(Colours.GREEN + "· Working directory: " + workingDir + Colours.RESET);

            cosOuts.loadingStart("· Getting server URL");

            File scf = new File(workingDir + "/game/settings.config");
            if (!scf.exists()) {
                cosOuts.stop("");
                firstSetup(workingDir);
            } else {
                try (Scanner fileReader = new Scanner(scf)) {
                    while (fileReader.hasNextLine()) {
                        String data = fileReader.nextLine();
                        if (data.contains("server-url=")) {
                            GameInstanceHandler.setServerUrl(data.split("=")[1]);
                            foundUrl = true;
                        }
                    }
                    if (foundUrl) {
                        cosOuts.stop(Colours.GREEN + "· Server URL loaded!" + Colours.RESET);
                    } else {
                        cosOuts.stop(Colours.RED + "· No server URL found!" + Colours.RESET);
                    }
                } catch (FileNotFoundException e) {
                    cosOuts.stop(Colours.RED + "· No server URL found!" + Colours.RESET);
                }
            }
        } else {
            System.out.println(Colours.RED + "· Failed to get working directory!" + Colours.RESET);
        }

        // RUN SERVER CONNECTION TEST IF URL FOUND
        if (foundUrl) {
            cosOuts.loadingStart("· Checking server status");
            connected = GameInstanceHandler.checkStatus();
            cosOuts.stop(connected ? Colours.GREEN + "· Server connected!" + Colours.RESET : Colours.RED + "· No server connection!" + Colours.RESET);
        } else {
            System.out.println(Colours.YELLOW + "· Skipping server connection test!\n" + Colours.RESET);
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
                    System.out.print(Colours.CYAN + "\nPick an option: " + Colours.RESET);
                    input = scanner.nextInt();

                    if (input >= 1 && input <= menuOptions.size()) {
                        validInput = true;
                    }
                } catch (Exception ex) {
                    System.out.println(Colours.RED + "Invalid input!" + Colours.RESET);
                    cosOuts.pause(1000);
                    cosOuts.clearConsole();
                    scanner.next();
                }
            } while (!validInput);

            menuOptions.get(input - 1).getValue().run();
        } while (true); // if we're leaving this loop the user closed the game
        //</editor-fold>
    }

    /// Generated the ArrayList of main menu options and corresponding method calls
    /// @return the ArrayList of menu options
    private static @NonNull ArrayList<Map.Entry<String, Runnable>> buildMenuOptions() {
        LinkedHashMap<String, Runnable> menuOptions = new LinkedHashMap<>(); // use a LinkedHashMap as it's to start like this and insert values then convert later

        if (connected) { // only happens if server connection was established
            menuOptions.put("New Game", App::newGame); // start a new game
            menuOptions.put("Load Game", App::loadGame); // load an existing game (prompts user to log in)

        } else { // only happens if server connection failed
            menuOptions.put("Configure Server", () -> Utils.setServerURL(scanner, cosOuts)); // quick option for users to enter a server address
        }

        // these options are always available
        menuOptions.put("Settings", () -> System.out.println("Settings picked"));
        menuOptions.put("Quit", () -> {
            cosOuts.typeText("Thanks for playing!", 40);
            System.exit(0);
        });

        return new ArrayList<>(menuOptions.entrySet()); // convert the LinkedHashMap to an ArrayList and return
    }

    /// Does a one-time setup to create all the game files and find a server
    /// @param workingDir the JAR working directory
    private static void firstSetup(String workingDir) {
        try {
            cosOuts.clearConsole();
            cosOuts.typeText(Colours.CYAN + "Welcome to ConsoQuest!" + Colours.RESET);
            cosOuts.pause(500);

            // create the "game" folder
            cosOuts.typeText("\nJust a moment...");
            boolean success = false;
            File f = new File(workingDir + "/game");
            if (!f.exists()) {
                if (!f.mkdirs()) {
                    throw new IOException("Could not create directory: " + workingDir + "/game");
                }
            }

            cosOuts.typeText("Alright, let's get some things set up!");

            cosOuts.typeText("You'll need a server running the ConsoQuest server application!");
            if (!Utils.setServerURL(scanner, cosOuts)) {
                throw new IOException("Could not save server URL");
            }

            cosOuts.typeText("\nAnd that's you all set up! Let's play!");


        } catch (Exception e) {
            cosOuts.typeText("Hmmm ... looks like something isn't quite right ...");
            cosOuts.pause(500);
            cosOuts.typeText("Got to go!");
            cosOuts.pause(500);
            System.exit(1);
        }

    }

    private static void newGame() {
        cosOuts.clearConsole();
        cosOuts.typeText("Ready for a new adventure?");

        String username = "";

        do {
            System.out.print("\nPick a name for your character: ");
            username = scanner.nextLine();

            if (username.isEmpty()) {
                cosOuts.typeText(cosOuts.buildColouredString("You need to enter a name!", Colours.YELLOW));
            } else if (username.contains("=")) {
                cosOuts.typeText(cosOuts.buildColouredString("Character names can't contain '='!", Colours.YELLOW));
            }
        } while (username.isEmpty());

        String password = UUID.randomUUID().toString();

        try {
            cosOuts.loadingStart(Colours.CYAN + "Creating your character");
            GameInstanceHandler.register(username, password);
            cosOuts.stop(cosOuts.buildColouredString("Character created!", Colours.GREEN));

            cosOuts.pressToContinue(true);

            playGame();
        } catch (Exception e) {
            cosOuts.stop(cosOuts.buildColouredString("Something went wrong creating your character!", Colours.RED));
            System.out.println(cosOuts.buildColouredString(e.getMessage(), Colours.GREY)); // writes the error message to the console in grey colour
        }
    }

    /// Lets the user pick a save to load and loads it
    private static void loadGame() {
        try {
            HashMap<String, String> saves = GameInstanceHandler.loadGames();
            if (saves.isEmpty()) {
                cosOuts.typeText(cosOuts.buildColouredString("\nLooks like you don't have any saves!", Colours.YELLOW));

                cosOuts.pressToContinue(true);

                return;
            }

            ArrayList<String> usernames = new ArrayList<>(saves.keySet());

            cosOuts.typeText("Pick a save game:");

            for (int i = 0; i < usernames.size(); i++) {
                System.out.println((i + 1) + ": " + usernames.get(i));
            }

            int input = 0;

            do {
                input = scanner.nextInt();

                if (input < 1 || input > saves.size()) {
                    System.out.println(cosOuts.buildColouredString("You need to pick a save number!", Colours.YELLOW));
                }
            } while (input < 1 || input > saves.size());

            cosOuts.loadingStart("Loading selected save");
            String password = saves.get(usernames.get(input - 1));
            GameInstanceHandler.login(usernames.get(input - 1), password);

            if (GameInstanceHandler.PLAYER == null) {
                throw new Exception("The provided username and password did not retrieve a valid save game from the server!");
            }

            cosOuts.stop(cosOuts.buildColouredString("Save game loaded!", Colours.GREEN));
            playGame();

        } catch (Exception e) {
            cosOuts.stop(cosOuts.buildColouredString("\nSomething went wrong loading your saves!", Colours.RED));
            System.out.println(cosOuts.buildColouredString(e.getMessage(), Colours.GREY)); // writes the error message to the console in grey colour

            cosOuts.pressToContinue(true);
        }


    }

    /// Here we are, the big bad ...
    private static void playGame() {

    }
}
