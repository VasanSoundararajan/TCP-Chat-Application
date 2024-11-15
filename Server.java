import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    private static final int PORT = 12345;
    public static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());
    public static Map<String, String> userCredentials = new HashMap<>();
    private static final String CHAT_LOG_FILE = "chat_logs/chat_history.txt";

    public static void main(String[] args) throws IOException {
        System.out.println("Server is running...");
        loadUserCredentials();
        ServerSocket serverSocket = new ServerSocket(PORT);
        try {
            while (true) {
                new ClientHandler(serverSocket.accept()).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    private static void loadUserCredentials() {
        userCredentials.put("Vasan", "pass");
        userCredentials.put("user2", "pass");
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

                // Authentication
                while (true) {
                    // out.println("SUBMIT_USERNAME");
                    username = in.readLine();
                    // out.println("SUBMIT_PASSWORD");
                    String password = in.readLine();
                    if (username == null || password == null) {
                        return;
                    }
                    synchronized (userCredentials) {
                        if (userCredentials.containsKey(username) && userCredentials.get(username).equals(password)) {
                            out.println("LOGIN_SUCCESS");
                            sendChatHistoryToClient();
                            break;
                        } else {
                            out.println("LOGIN_FAILED");
                        }
                    }
                }

                clientWriters.add(out);

                // Messaging
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/quit")) {
                        return;
                    }
                    String formattedMessage = username + ": " + message;
                    for (PrintWriter writer : clientWriters) {
                        writer.println(formattedMessage);
                    }
                    storeMessage(formattedMessage);
                }
            } catch (IOException e) {
                System.out.println(e);
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientWriters.remove(out);
            }
        }

        private void sendChatHistoryToClient() {
            File chatLog = new File(CHAT_LOG_FILE);
            if (chatLog.exists()) {
                try (BufferedReader logReader = new BufferedReader(new FileReader(chatLog))) {
                    String line;
                    while ((line = logReader.readLine()) != null) {
                        out.println(line); // Send each line of chat history to the client
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void storeMessage(String message) {
            try (PrintWriter logWriter = new PrintWriter(new FileWriter(CHAT_LOG_FILE, true))) {
                logWriter.println(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
