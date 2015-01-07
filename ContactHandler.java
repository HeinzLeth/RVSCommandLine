/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
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
public class ContactHandler extends Thread {

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
    }

    public void sendMessage(User u, String message) {

        boolean sent = false;

        Iterator<Connection> iterator = connections.iterator();
        while (iterator.hasNext() && !sent) {
            Connection connection = iterator.next();
            if (connection.getUser().equals(u)) {
                connection.sendMessage(message);
                sent = true;
            }
        }
        if (!sent) {
            this.showOnline();
            for (User onlineUser : onlineUsers) {
                if (onlineUser.equals(u)) {

                    try {
                        peer = new Socket();
                        peer.connect(new InetSocketAddress(onlineUser.getIp(), onlineUser.getPort()));
                        Connection connection = new Connection(peer, onlineUser);
                        connections.add(connection);
                        connection.start();
                        connection.makeFirstContact(user);
                        connection.sendMessage(message);
                        sent = true;
                    } catch (IOException ex) {
                        Logger.getLogger(ContactHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        if (!sent) {
            System.out.println("No such User online");
        }
    }

    public void showOnline() {
        try {
            toServer.write(Commands.Server.userTable + "\n");
            toServer.flush();
            String string = fromServer.readLine();

            String userTable = "";
            for (int i = 0; i < Integer.valueOf(String.valueOf(string.charAt(2))); i++) {
                userTable += fromServer.readLine() + "\n";
            }
            onlineUsers = User.getUsersFromUserTable(userTable);
        } catch (IOException ex) {
            Logger.getLogger(ContactHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void printOnline() {
        this.showOnline();
        for (User u : onlineUsers) {
            System.out.println(u.getName());
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
            Logger.getLogger(ContactHandler.class.getName()).log(Level.SEVERE, null, ex);
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
                connections.add(connection);
            } catch (SocketException exception) {
                System.out.println("ServerSocket closed");
            } catch (IOException ex) {
                Logger.getLogger(ContactHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

	public User getUser() {
		return user;
	}
    
    
}
