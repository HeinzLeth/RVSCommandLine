/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heinz
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

    public void showHelpTable() {
        System.out.println("---------------Help---------------");
        System.out.println("#help             :    shows HelpTable");
        System.out.println("@NAME             :    sends message to NAME");
        System.out.println("#whoisonline      :    shows who is online on the server");
    }

    @Override
    public void run() {

        in = new BufferedReader(new InputStreamReader(System.in));
        while (!terminate) {
            try {
                String input = in.readLine();
                String[] commandSplit = input.split(" ", 2);

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
                                System.out.println("Gib was richtiges ein!");
                                break;
                        }
                    } else {
                        System.out.println("# ist das Zeichen für ein Kommando");
                        showHelpTable();
                    }
                } else if (commandSplit.length == 2) {
                    if (commandSplit[0].startsWith(Commands.UserInput.message)) {
                        contactHandler.sendMessage(new User(commandSplit[0].substring(1)), commandSplit[1]);
                    } else {
                        System.out.println("Gib was richtiges ein!");
                        showHelpTable();
                    }
                } else {

                }
            } catch (IOException ex) {
                Logger.getLogger(UserInput.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
