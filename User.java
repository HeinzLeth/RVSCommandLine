/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package chat;

import java.util.ArrayList;

/**
 *
 * @author Heinz
 */
public class User {

    private String name;
    private String ip;
    private int port;

    public User() {
        this.name = "";
        this.ip = "";
        this.port = 0;
    }

    public User(String name) {
        this.name = name;
        this.ip = "";
        this.port = 0;
    }   

    public static ArrayList<User> getUsersFromUserTable(String userTable) {
        String[] rows = userTable.split("\n");
        ArrayList<User> users = new ArrayList<>();
        for (String row : rows) {
            String[] userData = row.split(" ");
            User u = new User();
            u.setName(userData[0]);
            u.setIp(userData[1]);
            u.setPort(Integer.parseInt(userData[2]));
            users.add(u);
        }
        return users;
    }

    @Override
    public boolean equals(Object obj) {
        User u = (User) obj;
        return u.getName().equals(this.name);
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public void setName(String name) {
        this.name = name.trim();
    }

    public void setIp(String ip) {
        this.ip = ip.trim();
    }

    public void setPort(int port) {
        this.port = port;
    }
}
