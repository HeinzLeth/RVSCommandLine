/*
 safdf * To change this license header, choose License Headers in Project Properties.
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

/*
 * Echo crasht nachdem wir ausloggen. Testclient nicht??
 * */
public class Chat {

    /**
     * Wartet auf Server-IP-Adressen-,Port- und Nutzernameneingabe. Bei
     * erfolgreicher Eingabe: Verbindung zum Server
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {

            int serverPort = 2534;

            BufferedReader userIn = new BufferedReader(new InputStreamReader(System.in));

            Socket mySocket = null;
            int clientPort = 0;
            boolean isCorrectIp = false;
            while (!isCorrectIp) {
                System.out.println("Type in server-IP-address");
                String serverIp = userIn.readLine();

                //Abfrage bis ein valider Port eingegeben wurde
                boolean isPortInt = false;
                while (!isPortInt) {
                    try {
                        System.out.println("Choose a port between 50000 & 65000:");
                        clientPort = Integer.parseInt(userIn.readLine());
                        if (clientPort > 49999 && clientPort < 65001) {
                            isPortInt = true;
                        }

                    } catch (NumberFormatException e) {
                        System.out.println("A port consists of integers!");
                    }
                }
                //Abfrage ob der Server unter der IP-Adresse zu erreichen ist, falls nicht neuer Versuch.
                try {
                    mySocket = new Socket();
                    mySocket.connect(new InetSocketAddress(serverIp, serverPort));
                    isCorrectIp = true;
                } catch (Exception e) {
                    System.out.println("Wrong server-IP-address!");
                }
            }
            ServerSocket ssocket = new ServerSocket();
            ssocket.bind(new InetSocketAddress(clientPort));

            //Reader für Socket-Eingang und Writer für Socket-Ausgang
            PrintWriter out = new PrintWriter(mySocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(mySocket.getInputStream()));
            String userInput = "";

            //Der eigene Benutzername wird als User abgespeichert
            User user = new User();
            boolean isConnected = false;
            while (!isConnected) {
                System.out.println("Please choose a username. NO spaces");
                userInput = userIn.readLine();

                if (userInput.contains(" ")) {
                    userInput = userInput.replaceAll(" ", "");
                    System.out.println("Your spaces got removed automatically");
                }

                user.setName(userInput);
                user.setPort(clientPort);

                //Formatiert die Eingabe gemäß Protokoll zum korrekten Senden des Logins an den Server 
                out.write(String.format("%s %s %s\n", Commands.Client.login, userInput, clientPort));
                out.flush();
                String answer = in.readLine();
                if (answer.equals(Commands.Server.loginSucessfull)) {
                    System.out.println("Hello " + userInput + "! You may now chat.");
                    isConnected = true;
                } else if (answer.startsWith(Commands.Server.error)) {
                    //Ausgabe der Fehlermeldung, die der Server zurückschickt
                    String error = answer.substring(2, answer.length());
                    System.out.println(error);
                } else {
                    throw new RuntimeException("unknown answer from server");
                }
            }
            //Erstellen des Thread zum Warten auf neue Verbindungen
            ContactHandler contactHandler = new ContactHandler(ssocket, mySocket, in, out, user);
            contactHandler.start();

            //Erstllen des Thread zum Warten auf die Eingaben des Benutzers
            UserInput input = new UserInput(contactHandler);
            input.start();

        } catch (IOException ex) {
            System.err.println("Fehler im Program");
        }
    }

}
