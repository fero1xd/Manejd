package net.fero.manejd;


import net.fero.manejd.database.SqlStore;
import net.fero.manejd.exceptions.NotValidInput;
import net.fero.manejd.utils.Actions;
import net.fero.manejd.utils.ConsoleColors;

import java.sql.Connection;
import java.util.Scanner;

public class Manejd {

    private static Connection conn = null;
    private static String[] options = { Options.SETUP.label };
    public final static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) throws NotValidInput {
        conn = SqlStore.getConn();

        if(SqlStore.hasConfigured()) {
            options = new String[] { Options.ADD.label, Options.Get.label };
        }

        // Show welcome screen
        System.out.println("\n\n" + ConsoleColors.BLUE + "Welcome to Manejd. A local password manager that I made for time pass" + ConsoleColors.RESET);
        System.out.print("Choose an option - \n" + getOptionsString());


        // Get input for option
        String option = getOption(">> ");


        if (option.equals(Options.SETUP.label)) {
            Actions.setup();
        }
        else if (option.equals(Options.Get.label)) {
            Actions.get();
        }
        else if (option.equals(Options.ADD.label)) {
            Actions.add();
        }
    }


    private static String getOption(String prompt) throws NotValidInput {
        System.out.print(prompt);

        boolean s = scanner.hasNextInt();
        if(!s) throw new NotValidInput("No correct option selected");

        int i = scanner.nextInt();

        if(i <= 0 || i > options.length) throw new NotValidInput("No correct option selected");

        scanner.nextLine();

        return options[i - 1];
    }


    private static String getOptionsString() {
        StringBuilder builder = new StringBuilder();

        for(int i = 0; i < options.length; i++) {
            builder.append(i+1).append(". ").append(options[i]).append("\n");
        }


        return builder.toString();
    }
}
