package consoclient;

import java.util.Scanner;

public class App {
    public static void main(String[] args) {
        // OPENING STUFF
        for (int i = 0; i < 19; i++) {
            System.out.print("\uD834\uDF06");
        }
        System.out.println("\n\uD834\uDF06 ConsoQuest! (Java Edition) \uD834\uDF06");
        for (int i = 0; i < 19; i++) {
            System.out.print("\uD834\uDF06");
        }

        // MAIN MENU
        boolean validInput = false; // for determining when to quit the main menu and do something
        Scanner scanner = new Scanner(System.in); // yummy keyboard inputs
        int input = -1; // the user's actual keyboard input

        // loop til valid input
        do {
            // print the options
            System.out.println("\n\n1: New Game");
            System.out.println("2: Load Game");
            System.out.println("0: Exit");

            try {
                System.out.print(Colours.FONT_GREEN + "Pick an option: " + Colours.FONT_RESET);
                input = scanner.nextInt();

                if (input >= 0 && input <= 2) {
                    validInput = true;
                }
            } catch (Exception ex) {
                System.out.println(Colours.FONT_RED + "INVALID INPUT!" + Colours.FONT_RESET);
                scanner.next();
            }
        } while (!validInput);

        scanner.close();
        System.out.println("You picked: " + input);
    }
}
