package com.example.Client;

public class ClientMain {
    public static void main(String[] args) {
        String server = "localhost";
        int port = Integer.parseInt(args[0]);
        new ClientGUI(server, port);
    }
}
