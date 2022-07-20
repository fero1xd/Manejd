package net.fero.manejd.utils;

import net.fero.manejd.database.SqlStore;
import net.fero.manejd.io.IO;
import net.fero.manejd.structure.Search;
import org.apache.commons.codec.digest.DigestUtils;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;


public class Actions {
    private static final String[] questions = { "What is the site name ?", "What is the site url ?", "What is the email you used for this site ?", "What is the username you used for this site?" };

    public static void setup() {
        String pass = IO.takeInput("Enter a master password " + ConsoleColors.RED + " ( This will be used to retrieve all your passwords )" + ConsoleColors.RESET + " >> ");


        if(pass.trim().equals("")) {
            System.out.println(ConsoleColors.RED + "Enter a valid master password" + ConsoleColors.RESET);
            System.exit(1);
        }


        String retype = IO.takeInput("Please retype your master password >> ");

        if(retype.trim().equals("") || !retype.equals(pass)) {
            System.out.println(ConsoleColors.RED + "Password mismatch" + ConsoleColors.RESET);
            System.exit(1);
        }

        String hashed = DigestUtils.sha256Hex(pass);

        SqlStore.addSecret(hashed);

        System.out.println(ConsoleColors.GREEN + "Setup Completed" + ConsoleColors.RESET);
    }


    public static void get() {
        String mp = authenticate();
        if(mp == null) return;

        Search search = askQuestions();
        List<Search> searches = SqlStore.searchEntries(search);

        if(searches == null) return;

        System.out.println("\n" + ConsoleColors.RED_BACKGROUND + "Found " + searches.size() + (searches.size() > 1 ? " Results" : " Result") + ConsoleColors.RESET);

        StringBuilder output = new StringBuilder();

        for(Search s : searches) {
            output.append("Site Name: ").
                    append(s.siteName).
                    append("\n").
                    append("Site Link: ").
                    append(s.siteUrl).
                    append("\n").
                    append("Email: ").
                    append(s.email).
                    append("\n").
                    append("Username: ").
                    append(s.username);
            output.append("\n\n");
        }

        output = new StringBuilder(output.substring(0, output.length() - 2));
        System.out.println(output);

        if(searches.size() == 1) {
            String encPassword = searches.get(0).password;

            try {
                String masterKey = Hashing.computeMasterKey(mp);

                String clearPassword = Hashing.aesDecrypt(encPassword, masterKey);

                copyToClipboard(clearPassword);
                System.out.println("\n" + ConsoleColors.GREEN + "1 Result found, Password copied to your clipboard" + ConsoleColors.RESET);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void add() {
        String mp = authenticate();
        if(mp == null) return;

        Search answers = askQuestions();

        if(answers.siteName == null || answers.siteUrl == null || answers.username == null) {
            if(answers.siteName == null) {
                System.out.println(ConsoleColors.RED + "Site Name is required" + ConsoleColors.RESET);
            }
            if(answers.siteUrl == null) {
                System.out.println(ConsoleColors.RED + "Site Url is required" + ConsoleColors.RESET);
            }
            if(answers.username == null) {
                System.out.println(ConsoleColors.RED + "Username is required" + ConsoleColors.RESET);
            }

            return;
        }

        if(answers.email == null) {
            answers.email = "";
        }

        String password = IO.takeInput("Password >> ");
        if(password.trim().equals("")) {
            System.out.println(ConsoleColors.RED + "Input a valid password" + ConsoleColors.RESET);
            return;
        }

        SqlStore.addEntry(answers, password, mp);
    }

    private static Search askQuestions() {
        List<String> answers = new ArrayList<>();

        for (String question : questions) {
            String ans = IO.takeInput(question + ConsoleColors.PURPLE + " [ Default: None ] " + ConsoleColors.RESET + " >> ");

            answers.add(ans.trim().equals("") ? null : ans);
        }

        return new Search(answers.get(0), answers.get(1), answers.get(2), answers.get(3));
    }

    private static String authenticate() {
        String pass = IO.takeInput("Enter your master password >> ");

        String hashed = DigestUtils.sha256Hex(pass);

        if(!SqlStore.validatePassword(hashed)) {
            System.out.println("\n" + ConsoleColors.RED + "Invalid Master password" + ConsoleColors.RESET);
            return null;
        }

        System.out.println(ConsoleColors.BLUE + "\n\nWelcome Back !\n\n" + ConsoleColors.RESET);
        return hashed;
    }

    private static void copyToClipboard(String msg) {
        StringSelection selection = new StringSelection(msg);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(selection, selection);
    }

}
