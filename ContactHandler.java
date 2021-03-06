/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class ContactHandler extends Thread implements Connection.DeleteUserListener {

    private final ServerSocket serverSocket;
    private final Socket clientSocket;
    private Socket peer;

    private final BufferedReader fromServer;
    private final PrintWriter toServer;

    private boolean terminate;
    private final User user;
    private ArrayList<User> onlineUsers;
    private LinkedList<Connection> connections;

    public ContactHandler(ServerSocket serverSocket, Socket clientSocket, BufferedReader fromServer, PrintWriter toServer, User user) {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;
        this.fromServer = fromServer;
        this.toServer = toServer;
        this.terminate = false;
        this.user = user;
        this.onlineUsers = new ArrayList<>();
        this.connections = new LinkedList<>();
        this.printOnline();
    }

    /**
     * Sendet neue Nachricht an uebergebene Nachricht
     *
     * @param u
     * @param message
     */
    public void sendMessage(User u, String message) {

        boolean sent = false;

        //Geht die Liste der Verbindungen durch und schaut, ob bereits eine Verbindung besteht.
        Iterator<Connection> iterator = connections.iterator();
        while (iterator.hasNext() && !sent) {
            Connection connection = iterator.next();
            if (connection.getUser().equals(u)) {
                connection.sendMessage(message);
                sent = true;
            }
        }
        //Wenn nicht wird beim Server die Liste der aktuell eingeloggten Benutzer abgefragt und durchgegangen.
        if (!sent) {
            try {
                this.showOnline();
                for (User onlineUser : onlineUsers) {
                    if (onlineUser.equals(u)) {
                        try {
                            //Es wird ein neuer Socket erzeugt mit den vom Server uebergebenen Benutzerdaten.
                            peer = new Socket();
                            peer.connect(new InetSocketAddress(onlineUser.getIp(), onlineUser.getPort()));
                            Connection connection = new Connection(peer, onlineUser);
                            connections.add(connection);
                            connection.registerListener(this);
                            connection.start();
                            connection.makeFirstContact(user);
                            connection.sendMessage(message);
                            sent = true;
                        } catch (ConnectException e) {
                            System.out.println("The connection to " + u.getName() + " is lost.");
                        } catch (IOException ex) {
                        }
                    }
                }
                //Falls der Sever nicht erreichbar ist, wirft showOnline eine IOException.
            } catch (IOException ex) {
                System.out.println("Server not reachable...");
            }
        }
        //Falls kein Benutzer mit diesem Namen existiert, wird dies angezeigt.
        if (!sent) {
            System.out.println("No such User online");
        }
    }

    /**
     * Holt sich die Online-Tabelle vom Server
     *
     * @throws java.io.IOException
     */
    public void showOnline() throws IOException {
        toServer.write(Commands.Server.userTable + "\n");
        toServer.flush();
        String string = fromServer.readLine();

        String userTable = "";
        for (int i = 0; i < Integer.valueOf(String.valueOf(string.charAt(2))); i++) {
            userTable += fromServer.readLine() + "\n";
        }
        onlineUsers = User.getUsersFromUserTable(userTable);
    }

    public void printOnline() {
        try {
            this.showOnline();
            System.out.println("------Online Users------");
            for (User u : onlineUsers) {
                System.out.println(u.getName());
            }

        } catch (SocketException e) {
            System.out.println("Server not reachable...");
        } catch (IOException ex) {
        }
    }

    public void closeAll() {
        try {
            Iterator<Connection> iter = connections.iterator();
            while (iter.hasNext()) {
                iter.next().closeConnection();
            }
            this.terminate();
            serverSocket.close();
            clientSocket.close();
        } catch (IOException ex) {
        }
    }

    public void terminate() {
        this.terminate = true;
    }

    @Override
    public void run() {
        while (!terminate) {
            try {
                peer = serverSocket.accept();
                Connection connection = new Connection(peer);
                connection.start();
                connection.registerListener(this);
                connections.add(connection);
            } catch (SocketException exception) {
                System.out.println("ServerSocket closed");
            } catch (IOException ex) {
            }
        }
    }

    public User getUser() {
        return user;
    }

    @Override
    public void deleteUser(User user, Connection connection) {
        this.onlineUsers.remove(user);
        this.connections.remove(connection);
    }

}
