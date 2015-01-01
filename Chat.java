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
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heinz
 */
public class Chat {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            // TODO code application logic here            

            int serverPort = 2534;

            // BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));
            Scanner scanner = new Scanner(System.in);
            System.out.println("Server ip eingeben:");
            String serverIp = scanner.next();

            //String ipServer = "";
            //String ipClient = ipServer;
            int clientPort = 2540;

            Socket mySocket = new Socket();
            mySocket.connect(new InetSocketAddress(serverIp, serverPort));

            ServerSocket ssocket = new ServerSocket();
            ssocket.bind(new InetSocketAddress(clientPort));

            PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));

            String userInput = "";

            //String userName = "";
            System.out.println("Please choose a username. NO spaces");
            userInput = scanner.next();
            User user = new User();
            user.setName(userInput);
            user.setPort(clientPort);

            if (userInput.contains(" ")) {
                userInput = userInput.replaceAll(" ", "");
            }

            out.write(String.format("%s %s %s\n", "n", userInput, clientPort));
            out.flush();
            String answer = in.readLine();
            if (answer.equals("s")) {
                System.out.println("Hello " + userInput + "! You may now chat.");
            } else if (answer.startsWith("e")) {
                System.out.println(answer.substring(2, answer.length()));
            } else {
                throw new RuntimeException("unknown answer from server");
            }

            ContactHandler contactHandler = new ContactHandler(ssocket, mySocket, in, out, user);
            contactHandler.start();

            UserInput input = new UserInput(contactHandler);
            input.start();
//            makeContactThread myContacts = new makeContactThread(ssocket, mySocket, in, out, userInput);
//            myContacts.start();

//            UserInput userInputThread = new UserInput(myContacts);
//            userInputThread.start();
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
