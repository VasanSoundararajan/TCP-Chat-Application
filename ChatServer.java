import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.*;
import java.net.*;
import java.util.*;

class ChatServer extends JFrame {
    private DefaultTableModel tableModel;
    private File file;
    private ServerSocket serverSocket;
    private ArrayList<ClientHandler> clientHandlers;

    public ChatServer() {
        super("Chat Server");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 300);
        JTable table = new JTable();
        tableModel = new DefaultTableModel(new Object[] { "Client", "Message" }, 0);
        table.setModel(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        getContentPane().add(scrollPane);

        setVisible(true);
        file = new File("messages.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        clientHandlers = new ArrayList<>();
    }

    public void addMessage(String client, String message) {
        tableModel.addRow(new Object[] { client, message });
        try {
            FileWriter fileWriter = new FileWriter(file, true);
            fileWriter.write(client + ": " + message + "\n");
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (ClientHandler clientHandler : clientHandlers) {
            clientHandler.sendMessage(message);
        }
    }

    public void startServer() {
        final int PORT = 12345;

        try {
            // Create a server socket
            serverSocket = new ServerSocket(PORT);
            System.out.println("Chat server started. Waiting for clients to connect...");

            // Keep accepting client connections
            while (true) {
                // Accept client connection
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket);

                // Create a new thread to handle the client
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clientHandlers.add(clientHandler);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            System.err.println("Error in chat server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        ChatServer chatServer = new ChatServer();
        chatServer.startServer();
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ChatServer chatServer;

    public ClientHandler(Socket clientSocket, ChatServer chatServer) {
        this.clientSocket = clientSocket;
        this.chatServer = chatServer;
    }

    @Override
    public void run() {
        try {
            // Input stream to read data from client
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            // Output stream to send data to client
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Read input from client and send it to server for display
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                chatServer.addMessage(clientSocket.getInetAddress().getHostAddress(), inputLine);
            }

            // Close streams and socket
            in.close();
            out.close();
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }

    public void sendMessage(String message) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
        }
    }
}
