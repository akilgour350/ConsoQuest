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
        System.out.println(Colours.BLUE + "Initialising ConsoQuest...\n" + Colours.RESET);

        // GET SERVER URL
        String workingDir = Utils.getJarDirectory(App.class);
        boolean foundUrl = false;
        boolean configExists = false;

        if (workingDir != null) {
            System.out.println(Colours.GREEN + "· Working directory: " + workingDir + Colours.RESET);

            cosOuts.loadingStart("· Getting server URL");

            File scf = new File(workingDir + "/game/settings.config");
            configExists = scf.exists();

            if (!configExists) { // if the config file doesn't exist, run a first time setup
                cosOuts.stop("");
                firstSetup(workingDir);

            } else {
                try (Scanner fileReader = new Scanner(scf)) {
                    while (fileReader.hasNextLine()) {
                        String data = fileReader.nextLine();
                        if (data.contains("server-url=")) {
                            GIH.setServerUrl(data.split("=")[1]);
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
            try {
                cosOuts.loadingStart("· Checking server status");
                connected = GIH.checkStatus();
                cosOuts.stop(connected ? Colours.GREEN + "· Server connected!" + Colours.RESET : Colours.RED + "· No server connection!" + Colours.RESET);
            } catch (Exception e) {
                cosOuts.displayError(e.getMessage());
            }
        } else if (configExists) {
            System.out.println(Colours.YELLOW + "· Skipping server connection test!\n" + Colours.RESET);
        }

        System.out.println("\nInitialisation complete!");
        // PAUSE SO USER CAN SEE LOADING RESULTS
        cosOuts.pause(1000);

        //</editor-fold>

        //<editor-fold desc="MAIN MENU">
        // MAIN MENU DISPLAY ===========================================================================================
        do {
            cosOuts.clearConsole();
            cosOuts.showGameName();

            // create an arraylist with all the menu options with a corresponding function to execute
            Runnable r = Utils.showMenu(buildMenuOptions(),
                    new String[] {
                            "Pick an option: ",
                            "Not a valid option!",
                            cosOuts.buildColouredString("Uh oh... something's gone wrong!", Colours.RED)
                    },
                    scanner, cosOuts
            );

            if (r != null) {
                r.run();
            } else {
                System.out.println(cosOuts.buildColouredString("Failed to run method associated with this action!", Colours.GREY));
                cosOuts.pressToContinue(true);
            }
        } while (true); // if we're leaving this loop the user closed the game
        //</editor-fold>
    }

    /// Generated the ArrayList of main menu options and corresponding method calls
    /// @return the ArrayList of menu options
    private static @NonNull LinkedHashMap<String, Runnable> buildMenuOptions() {
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
            cosOuts.typeText("Thanks for playing!");
            System.exit(0);
        });

        return menuOptions; // convert the LinkedHashMap to an ArrayList and return
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
            cosOuts.displayError(e.getMessage());
            System.exit(1);
        }

    }

    private static void newGame() {
        cosOuts.clearConsole();
        cosOuts.showGameName();

        cosOuts.typeText(cosOuts.buildColouredString("Ready for a new adventure?", Colours.BLUE));

        String username = "";

        do {
            System.out.print("Pick a name for your character: ");
            username = scanner.nextLine();

            if (username.isEmpty()) {
                return;
            } else if (username.contains("=")) {
                cosOuts.typeText(cosOuts.buildColouredString("Character names can't contain '='!\n", Colours.YELLOW));
                username = "";
            }
        } while (username.isEmpty());

        String password = UUID.randomUUID().toString();

        try {
            cosOuts.loadingStart(Colours.CYAN + "Creating your character");
            GIH.register(username, password);
            cosOuts.stop(cosOuts.buildColouredString("Character created!", Colours.GREEN));

            cosOuts.pressToContinue(true);

            playGame();
        } catch (Exception e) {
            cosOuts.displayError(e.getMessage());
        }
    }

    /// Lets the user pick a save to load and loads it
    private static void loadGame() {
        cosOuts.clearConsole();
        cosOuts.showGameName();

        try {
            HashMap<String, String> saves = GIH.loadGames();

            if (saves.isEmpty()) {
                cosOuts.typeText(cosOuts.buildColouredString("\nLooks like you don't have any saves!", Colours.YELLOW));
                cosOuts.pressToContinue(true);

                return;
            }

            ArrayList<String> usernames = new ArrayList<>(saves.keySet());

            for (int i = 0; i < usernames.size(); i++) {
                System.out.println((i + 1) + ": " + usernames.get(i));
            }

            int input = 0;

            do {
                try {
                    System.out.print(cosOuts.buildColouredString("\nPick a save: ", Colours.PURPLE));
                    String inputStr = scanner.nextLine();
                    input = Integer.parseInt(inputStr);

                    if (input < 1 || input > saves.size())
                        System.out.println(cosOuts.buildColouredString("You need to pick a save number!", Colours.YELLOW));

                } catch (Exception e) {
                    cosOuts.typeText(cosOuts.buildColouredString("That wasn't a number!", Colours.YELLOW));
                }
            } while (input < 1 || input > saves.size());

            cosOuts.loadingStart("Loading selected save");
            String password = saves.get(usernames.get(input - 1));
            GIH.login(usernames.get(input - 1), password);

            if (GIH.PLAYER == null) {
                throw new Exception("The provided username and password did not retrieve a valid save game from the server!");
            }

            cosOuts.stop(cosOuts.buildColouredString("Save game loaded!", Colours.GREEN));
            cosOuts.pause(1000);

            playGame();

        } catch (Exception e) {
            cosOuts.displayError(e.getMessage());
        }
    }

    /// Here we are, the big bad ...
    private static void playGame() {
        try {
            cosOuts.clearConsole();
            cosOuts.typeText("The light is blinding as you open your eyes...");
            cosOuts.loadingStart("");
            GIH.move("");
            cosOuts.stop("");

            cosOuts.pause(1000);

            boolean[] quit = { false };

            do {
                cosOuts.clearConsole();
                if (GIH.CURRENT_TILE != null) {
                    cosOuts.typeText("You are in a " + GIH.CURRENT_TILE.biome.toLowerCase() + " at co-ordinates: X" + GIH.PLAYER.x + " Y" + GIH.PLAYER.y + " ...");

                    if (!GIH.CURRENT_TILE.structure.equals("NONE")) {
                        cosOuts.typeText("There is a " + GIH.CURRENT_TILE.structure.toLowerCase() + " nearby!\n");
                    } else {
                        cosOuts.typeText("There doesn't seem to be much here!\n");
                    }

                    LinkedHashMap<String, Runnable> options = new LinkedHashMap<>(); // use a LinkedHashMap as it's to start like this and insert values then convert later
                    options.put("Head north", App::moveNorth);
                    options.put("Head east", App::moveEast);
                    options.put("Head south", App::moveSouth);
                    options.put("Head west", App::moveWest);
                    options.put("Quit", () -> quit[0] = true);

                    Runnable r = Utils.showMenu(
                            options,
                            new String[] {
                                    "What will you do?\n",
                                    "You can't do that now!",
                                    cosOuts.buildColouredString("Uh oh... something's gone wrong!", Colours.RED)
                            },
                            scanner, cosOuts
                            );

                    if (r != null) {
                        r.run();
                    } else {
                        throw new Exception("Failed to run method associated with option!");
                    }
                }
            } while (!quit[0]);

        } catch (Exception e) {
            cosOuts.displayError(e.getMessage());
        }
    }

    // methods for moving the player character around
    private static void moveNorth() {
        try {
            GIH.move("north");
        } catch (Exception ignored) {}
    }
    private static void moveSouth() {
        try {
            GIH.move("south");
        } catch (Exception ignored) {}
    }
    private static void moveEast() {
        try {
            GIH.move("east");
        } catch (Exception ignored) {}
    }
    private static void moveWest() {
        try {
            GIH.move("west");
        } catch (Exception ignored) {}
    }
}
