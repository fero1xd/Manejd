package net.fero.manejd.io;

import net.fero.manejd.Manejd;

import java.util.Scanner;

public class IO {

    public static String takeInput(String prompt) {
        System.out.print(prompt);

        Scanner scanner = Manejd.scanner;

        return scanner.nextLine();
    }
}
