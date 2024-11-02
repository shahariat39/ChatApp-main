package src.client;

import java.io.*;
import java.net.*;

public class ChatClient {
    
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public void startConnection(String ip, int port) throws IOException {
        clientSocket = new Socket(ip, port);
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        System.out.println("Connected to the server");
    }

    public void sendMessage(String msg) {
        out.println(msg);
    }

    public String readMessage() throws IOException {
        return in.readLine();
    }

    public void stopConnection() throws IOException {
        in.close();
        out.close();
        clientSocket.close();
    }

    public static void main(String[] args) {
        ChatClient client = new ChatClient();
        try {
            client.startConnection("127.0.0.1", 50000); // Update the IP and port as necessary
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in));
            String userInput;
            while ((userInput = stdIn.readLine()) != null) {
                client.sendMessage(userInput);
                System.out.println("Server response: " + client.readMessage());
            }
            client.stopConnection();
        } catch (IOException e) {
            System.err.println("Client exception: " + e.getMessage());
        }
    }
}
