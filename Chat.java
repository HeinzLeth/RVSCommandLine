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
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat {

    /**
     * Wartet auf IP-Adressen und Nutzernameneingabe - Bei erfolgreicher
     * Anmeldung: Verbindung zum Server
     *
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
            System.out.println("Port eingeben:");
            boolean isPortInt = false;
            int clientPort = 0;
            while (!isPortInt) {
                try {
                    clientPort = scanner.nextInt();
                    isPortInt = true;
                } catch (InputMismatchException e) {
                    System.out.println("Gib eine Zahl ein");
                }
            }
            Socket mySocket = new Socket();
            try {
                mySocket.connect(new InetSocketAddress(serverIp, serverPort));
            } catch (Exception e) {
                System.out.println("Die IP-Adresse ist falsch");
            }
            ServerSocket ssocket = new ServerSocket();
            ssocket.bind(new InetSocketAddress(clientPort));

            PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            String userInput = "";

            //String userName = "";
            System.out.println("Please choose a username. If your input contains spaces the first will be your name!");
            userInput = scanner.next();
            User user = new User();

//            if (userInput.contains(" ")) {
//                userInput = userInput.replaceAll(" ", "");
//                System.out.println("Your spaces got removed automatically");
//            }
            user.setName(userInput);
            user.setPort(clientPort);

            out.write(String.format("%s %s %s\n", Commands.Client.login, userInput, clientPort));
            out.flush();
            String answer = in.readLine();
            if (answer.equals(Commands.Server.loginSucessfull)) {
                System.out.println("Hello " + userInput + "! You may now chat.");
            } else if (answer.startsWith(Commands.Server.error)) {
                System.out.println(answer.substring(2, answer.length()));
                //schauen welche Fehlermeldung kommt
            } else {
                throw new RuntimeException("unknown answer from server");
            }

            ContactHandler contactHandler = new ContactHandler(ssocket, mySocket, in, out, user);
            contactHandler.start();

            UserInput input = new UserInput(contactHandler);
            input.start();
        } catch (IOException ex) {
            Logger.getLogger(Chat.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
