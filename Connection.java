/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Verwaltet die Verbindung zwischen sich selbst und dem Chat-Partner
 */
public class Connection extends Thread {

    /**
     * Listener zum löschen der Verbinung im ContactHandler
     */
    public interface DeleteUserListener {

        public void deleteUser(User user, Connection connection);
    }

    private Socket peer;
    private BufferedReader in;
    private PrintWriter out;
    private User user;
    private boolean terminate;
    private boolean isNameSet;
    private DeleteUserListener deleteUserListener;

    public Connection(Socket socket, User user) {
        try {
            this.peer = socket;
            this.in = new BufferedReader(new InputStreamReader(peer.getInputStream(), StandardCharsets.UTF_8.name()));
            this.out = new PrintWriter(peer.getOutputStream(), true);
            this.user = user;
            this.terminate = false;
            this.isNameSet = true;
        } catch (IOException ex) {
        }
    }

    public Connection(Socket socket) {
        try {
            this.peer = socket;
            this.in = new BufferedReader(new InputStreamReader(peer.getInputStream(), StandardCharsets.UTF_8.name()));
            this.out = new PrintWriter(peer.getOutputStream(), true);
            this.user = new User();
            this.terminate = false;
            this.isNameSet = false;
        } catch (IOException ex) {
            Logger.getLogger(Connection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void registerListener(DeleteUserListener listener) {
        this.deleteUserListener = listener;
    }

    public User getUser() {
        return user;
    }

    private void terminate() {
        this.terminate = true;
        this.deleteUserListener.deleteUser(user, this);
    }

    /**
     * meldet sich bei einem Benutzer an
     *
     * @param user
     */
    public void makeFirstContact(User user) {
        this.out.write(String.format("%s %s%n", Commands.Client.login, user.getName()));
        this.out.flush();
    }

    /**
     * sendet eine Nachricht an einen Benutzer
     *
     * @param message
     */
    public void sendMessage(String message) {
        this.out.write(String.format("%s %s%n", Commands.Client.message, message));
        this.out.flush();
    }

    /**
     * meldet sich beim anderen Benutzer ab und schließt die Verbindung
     */
    public void closeConnection() {
        try {
            this.out.write(Commands.Server.logout + "%n");
            this.out.flush();
            this.terminate();
            this.peer.close();
        } catch (IOException ex) {
        }
    }

    @Override
    public void run() {

        while (!this.terminate) {

            try {
                String input = this.in.readLine();
                if (!isNameSet && input.startsWith(Commands.Client.login)) {
                    user.setName(input.substring(1));
                    this.isNameSet = true;
                } else if (input.startsWith(Commands.Client.message)) {
                    String clientMessage = input.substring(2);
                    System.out.println(user.getName() + ": " + clientMessage);
                } else if (input.startsWith(Commands.Server.logout)) {
                    if (input.length() > 1) {
                        String clientMessage = input.substring(2);
                        System.out.println(user.getName() + ": " + clientMessage);
                        System.out.println(user.getName() + " has left the chat and is offline.");
                    } else {
                        System.out.println(user.getName() + " has left the chat and is offline.");
                    }
                    peer.close();
                    this.terminate();
                }
                //wenn der Benutzer ohne logout den Chat verlässt
            } catch (SocketException e) {
                if (!peer.isClosed()) {
                    try {
                        peer.close();
                        this.terminate();
                        System.out.println(user.getName() + " has unexpected left the chat and is offline.");
                    } catch (IOException ex) {
                    }
                }
            } catch (IOException ex) {
            }
        }

    }

    public void setIsNameSet(boolean isNameSet) {
        this.isNameSet = isNameSet;
    }

}
