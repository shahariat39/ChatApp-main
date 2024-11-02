package src.client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

import src.database.UserAuthentication;

import java.io.*;
import java.net.Socket;
import java.util.Set;

public class ChatClientLogic {
    private ChatClientGUI gui;
    private PrintWriter out;
    private BufferedReader in;
    private Socket clientSocket;

    public ChatClientLogic(ChatClientGUI gui) {
        this.gui = gui;
    }

    public void startConnection(String serverAddress, int port) {
        try {
            clientSocket = new Socket(serverAddress, port);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Thread to listen for messages from the server
            new Thread(() -> {
                String message;
                try {
                    while ((message = in.readLine()) != null) {
                        processIncomingMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processIncomingMessage(String message) {
        String[] parts = message.split(": ", 2);
        String recipient = parts[0];
        String content = parts.length > 1 ? parts[1] : "";

        String currentRecipient = gui.getCurrentRecipient();
        
        if (recipient.equals(currentRecipient) || recipient.equals("ALL") || currentRecipient.contains(recipient)) {
            final String formattedMessage = message;
            // Append incoming messages to the message area
            SwingUtilities.invokeLater(() -> gui.getMessageArea().append(formattedMessage + "\n"));
        }
    }

    public void sendMessage() {
        if (out != null) {
            String message = gui.getInputField().getText();
            if (!message.isEmpty()) {
                // Append the message to the message area
                gui.getMessageArea().append("Me: " + message + "\n");

                // Send the message to the server
                out.println(gui.getCurrentRecipient() + ": " + message);
                gui.getInputField().setText("");
            }
        }
    }

    public void sendFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", ImageIO.getReaderFileSuffixes()));
        int returnValue = fileChooser.showOpenDialog(gui.getFrame());
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                Socket fileSocket = new Socket("127.0.0.1", 50001); // Assuming the server is listening on port 50001 for file transfers
                DataOutputStream dos = new DataOutputStream(fileSocket.getOutputStream());
                FileInputStream fis = new FileInputStream(selectedFile);

                // Send file name, file size, and recipient information
                dos.writeUTF(selectedFile.getName());
                dos.writeLong(selectedFile.length());
                dos.writeUTF(gui.getCurrentRecipient());

                // Send file content
                byte[] buffer = new byte[4096];
                int read;
                while ((read = fis.read(buffer)) > 0) {
                    dos.write(buffer, 0, read);
                }

                dos.close();
                fis.close();
                fileSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void fetchAllUsers() {
        Set<String> allUsers = UserAuthentication.getAllUsers();
        SwingUtilities.invokeLater(() -> {
            gui.getUserListModel().clear();
            for (String user : allUsers) {
                gui.getUserListModel().addElement(user);
            }
        });
    }
}
