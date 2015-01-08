/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verwaltet die Eingaben des Benutzers
 */
public class UserInput extends Thread {

    private Scanner scanner;
    private ContactHandler contactHandler;
    private boolean terminate;
    private BufferedReader in;

    public UserInput(ContactHandler ch) {
        scanner = new Scanner(System.in);
        this.contactHandler = ch;
        this.terminate = false;
    }

    /**
     * zeigt dem Benutzer die Hilfe-Tabelle
     */
    public void showHelpTable() {
        System.out.println("-----------------Help---------------");
        System.out.println("#help             :    shows HelpTable");
        System.out.println("#whoisonline      :    shows who is online on the server");
        System.out.println("#quit             :    closes all connections and goes offline");
        System.out.println("@NAME             :    sends message to NAME");
    }

    @Override
    public void run() {

        try {
            in = new BufferedReader(new InputStreamReader(System.in,
                    StandardCharsets.UTF_8.name()));
            while (!terminate) {
                try {
                    String input = in.readLine();
                    //trennt die Befehle am Leerzeichen, aber maximal an 2
                    String[] commandSplit = input.split(" ", 2);

                    //Befehl ohne Leerzeichen
                    if (commandSplit.length == 1) {
                        if (commandSplit[0].startsWith("#")) {
                            commandSplit[0] = commandSplit[0].substring(1);
                            switch (commandSplit[0]) {
                                case Commands.UserInput.whoIsOnline:
                                    contactHandler.printOnline();
                                    break;
                                case Commands.UserInput.help:
                                    showHelpTable();
                                    break;
                                case Commands.UserInput.quit:
                                    contactHandler.closeAll();
                                    this.terminate = true;
                                    break;
                                default:
                                    System.out.println("Wrong input. Type in #help");
                                    break;
                            }
                        } else {
                            System.out.println("# is the command character!");
                            showHelpTable();
                        }
                        //Befehel mit Leerzeichen (Nachricht)
                    } else if (commandSplit.length == 2) {
                        if (commandSplit[0]
                                .startsWith(Commands.UserInput.message)) {
                            if (!contactHandler.getUser().getName()
                                    .equals(commandSplit[0].substring(1))) {
                                contactHandler.sendMessage(new User(
                                        commandSplit[0].substring(1)),
                                        commandSplit[1]);
                            } else {
                                System.out.println("You cannot chat with yourself!");
                            }
                        } else {
                            showHelpTable();
                        }
                    }
                } catch (IOException ex) {
                }
            }
        } catch (UnsupportedEncodingException ex) {
        }
    }

}
