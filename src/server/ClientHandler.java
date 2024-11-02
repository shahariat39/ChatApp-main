package src.server;

import java.io.*;
import java.net.*;
import src.database.UserAuthentication;
import src.database.UserRegistration;

public class ClientHandler extends Thread {
    Socket clientSocket;
    private ChatServer server;
    private PrintWriter out;
    private BufferedReader in;
    private String username;

    public ClientHandler(Socket socket, ChatServer server) {
        this.clientSocket = socket;
        this.server = server;
    }

    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            boolean isAuthenticated = true;
            while (!isAuthenticated) {
                out.println("Do you have an account? (yes/no)");
                String response = in.readLine();
                if ("no".equalsIgnoreCase(response)) {
                    out.println("Enter a username to register:");
                    String username = in.readLine();
                    out.println("Enter a password:");
                    String password = in.readLine();
                    if (UserRegistration.registerUser(username, password)) {
                        out.println("Registration successful. You can now log in.");
                    } else {
                        out.println("Registration failed. Please try again.");
                    }
                }

                out.println("Enter your username:");
                String username = in.readLine();
                out.println("Enter your password:");
                String password = in.readLine();
                isAuthenticated = UserAuthentication.authenticateUser(username, password);
                if (isAuthenticated) {
                    this.username = username;
                    server.addUser(username, this);
                    out.println("Authenticated successfully!");
                    server.updateClientUserList();
                } else {
                    out.println("Authentication failed. Please try again.");
                }
            }

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.startsWith("/pm")) {
                    // Private message: /pm recipient message
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String message = parts[2];
                        server.privateMessage(this.username + " (private): " + message, recipient);
                    }
                } else if (inputLine.startsWith("/group")) {
                    // Group message: /group user1,user2 message
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String[] recipients = parts[1].split(",");
                        String message = parts[2];
                        for (String recipient : recipients) {
                            server.privateMessage(this.username + " (group): " + message, recipient);
                        }
                    }
                } else if (inputLine.startsWith("/file")) {
                    // File transfer: /file recipient filename
                    String[] parts = inputLine.split(" ", 3);
                    if (parts.length == 3) {
                        String recipient = parts[1];
                        String filename = parts[2];
                        sendFile(recipient, filename);
                    }
                } else {
                    server.broadcastMessage(this.username + ": " + inputLine, this);
                }
            }
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } finally {
            try {
                in.close();
                out.close();
                clientSocket.close();
            } catch (IOException e) {
                System.err.println("Failed to close resources: " + e.getMessage());
            }
            server.removeUser(this.username);
            server.updateClientUserList();
        }
    }

    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    public void sendFile(String recipient, String filename) {
        ClientHandler recipientHandler = server.userMap.get(recipient);
        if (recipientHandler != null) {
            try {
                File file = new File(filename);
                if (file.exists()) {
                    DataOutputStream dos = new DataOutputStream(recipientHandler.clientSocket.getOutputStream());
                    FileInputStream fis = new FileInputStream(file);
                    byte[] buffer = new byte[4096];
                    dos.writeUTF("FILE:" + file.getName());
                    int read;
                    while ((read = fis.read(buffer)) > 0) {
                        dos.write(buffer, 0, read);
                    }
                    dos.flush();
                    fis.close();
                } else {
                    sendMessage("File not found: " + filename);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            sendMessage("User not found: " + recipient);
        }
    }

    public String getUsername() {
        return username;
    }
}
