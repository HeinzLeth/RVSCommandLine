/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

/**
 *
 * @author Heinz
 */
public class Commands {

    public class Server {

        public static final String loginSucessfull = "s";
        public static final String userTable = "t";
        public static final String logout = "x byebye";
        public static final String error = "e";

    }

    public class Client {

        public static final String clientLogin = "n";
        public static final String clientMessage = "m";
    }

    public class UserInput {

        public static final String whoIsOnline = "Whoisonline";
        public static final String help = "help";
        public static final String quit = "quit";
        public static final String message = "@";

    }

}
