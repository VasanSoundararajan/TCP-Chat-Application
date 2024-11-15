import javax.swing.*;
import java.io.IOException;

/**
 * MainApp serves as the entry point for the chat application.
 * It starts the server and launches the client login interface.
 */
public class MainApp {
    public static void main(String[] args) {
        // Start the server in a separate thread
        Thread serverThread = new Thread(() -> {
            try {
                Server.main(null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        // Delay to ensure the server starts before clients attempt to connect
        try {
            Thread.sleep(2000); // Wait for 2 seconds
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Launch the client login interface
        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
