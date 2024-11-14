import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ChatClientWithLogin {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            Scanner scanner = new Scanner(System.in);

            // Login
            System.out.println(in.readLine()); // "Enter username:"
            String username = scanner.nextLine();
            out.println(username);

            System.out.println(in.readLine()); // "Enter password:"
            String password = scanner.nextLine();
            out.println(password);

            String response = in.readLine();
            if (response.startsWith("Login failed")) {
                System.out.println(response);
                return; // Exit if login fails
            } else {
                System.out.println(response); // "Login successful. Welcome ..."
            }

            // Thread to receive messages from the server
            Thread receiveMessages = new Thread(() -> {
                String serverMessage;
                try {
                    while ((serverMessage = in.readLine()) != null) {
                        System.out.println(serverMessage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receiveMessages.start();

            // Main thread to send messages to the server
            String message;
            while (scanner.hasNextLine()) {
                message = scanner.nextLine();
                out.println(message);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
