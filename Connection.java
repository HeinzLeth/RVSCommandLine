/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heinz
 */
public class Connection extends Thread {

    private Socket peer;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private boolean terminate;

    public Connection(Socket socket) {
        try {
            this.peer = socket;
            this.in = new BufferedReader(new InputStreamReader(peer.getInputStream()));
            this.out = new PrintWriter(peer.getOutputStream(), true);
            this.user = new User();
            this.terminate = false;
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public User getUser() {
        return user;
    }

    private void terminate() {
        this.terminate = true;
    }

    public void makeFirstContact(User user) {
        this.out.write("n " + user.getName() + "\n");
        this.out.flush();
    }

    public void sendMessage(String message) {
        this.out.write("m " + message + "\n");
        this.out.flush();
    }

    public void closeConnection() {
        try {
            this.out.write(Commands.Server.logout + "\n");
            this.out.flush();
            this.terminate();
            peer.close();
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {

        while (!this.terminate) {

            try {
                String input = this.in.readLine();
                if (input.startsWith("n")) {
                    user.setName(input.substring(2));
                } else if (input.startsWith("m")) {
                    String clientMessage = input.substring(2);
                    System.out.println(user.getName() + ": " + clientMessage);
                } else if (input.startsWith("x")) {
                    String clientMessage = input.substring(2);
                    System.out.println(user.getName() + ": " + clientMessage);
                    System.out.println(user.getName() + " has left the chat and is offline.");
                    peer.close();
                    this.terminate();
                }
            } catch (IOException ex) {
                Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
}