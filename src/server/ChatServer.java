package src.server;

import java.io.*;
import java.net.*;
import java.util.*;
import src.database.UserAuthentication;

public class ChatServer {
    private ServerSocket serverSocket;
    private Set<ClientHandler> clientHandlers = Collections.synchronizedSet(new HashSet<>());
    Map<String, ClientHandler> userMap = new HashMap<>();

    public ChatServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("Server started on port " + serverSocket.getLocalPort());
        while (true) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                clientHandler.start();
            } catch (IOException e) {
                System.err.println("Accept failed: " + e.getMessage());
            }
        }
    }

    public synchronized void broadcastMessage(String message, ClientHandler excludeUser) {
        System.out.println("Broadcasting message: " + message);
        for (ClientHandler clientHandler : clientHandlers) {
            if (clientHandler != excludeUser) {
                clientHandler.sendMessage(message);
            }
        }
    }

    public synchronized void privateMessage(String message, String recipient) {
        ClientHandler clientHandler = userMap.get(recipient);
        if (clientHandler != null) {
            clientHandler.sendMessage(message);
        }
    }

    public synchronized void addUser(String username, ClientHandler clientHandler) {
        userMap.put(username, clientHandler);
    }

    public synchronized void removeUser(String username) {
        ClientHandler clientHandler = userMap.remove(username);
        if (clientHandler != null) {
            clientHandlers.remove(clientHandler);
            System.out.println("Client disconnected: " + clientHandler.clientSocket.getInetAddress());
        }
    }

    public synchronized void updateClientUserList() {
        StringBuilder userList = new StringBuilder("USERLIST:");
        for (String user : userMap.keySet()) {
            userList.append(user).append(",");
        }
        if (userList.length() > 9) {
            userList.setLength(userList.length() - 1); // Remove trailing comma
        }
        String userListMessage = userList.toString();
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(userListMessage);
        }
    }

    public static void main(String[] args) {
        int port = 50000;
        try {
            ChatServer server = new ChatServer(port);
            server.start();
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
        }
    }
}
