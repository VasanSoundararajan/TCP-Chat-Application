import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServerWithLogin {
    private static final int PORT = 12345;
    private static Set<PrintWriter> clientWriters = new HashSet<>();
    private static Map<String, String> userCredentials = new HashMap<>();

    public static void main(String[] args) {
        loadUserCredentials();
        System.out.println("Chat server started with login...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadUserCredentials() {
        try (BufferedReader reader = new BufferedReader(new FileReader("users.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                if (parts.length == 2) {
                    userCredentials.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        public void run() {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                if (authenticate()) {
                    synchronized (clientWriters) {
                        clientWriters.add(out);
                    }

                    // Broadcast messages from this client to all others
                    String message;
                    while ((message = in.readLine()) != null) {
                        System.out.println("[" + username + "]: " + message);
                        broadcastMessage("[" + username + "]: " + message);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                synchronized (clientWriters) {
                    clientWriters.remove(out);
                }
                System.out.println(username + " has disconnected.");
            }
        }

        private boolean authenticate() throws IOException {
            out.println("Enter username:");
            String username = in.readLine();
            out.println("Enter password:");
            String password = in.readLine();

            if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
                this.username = username;
                out.println("Login successful. Welcome " + username + "!");
                System.out.println(username + " has connected.");
                return true;
            } else {
                out.println("Login failed. Disconnecting.");
                socket.close();
                return false;
            }
        }

        private void broadcastMessage(String message) {
            synchronized (clientWriters) {
                for (PrintWriter writer : clientWriters) {
                    writer.println(message);
                }
            }
        }
    }
}
