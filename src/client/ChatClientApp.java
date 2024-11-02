package src.client;

import javax.swing.SwingUtilities;

public class ChatClientApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChatClientGUI::new);
    }
}
