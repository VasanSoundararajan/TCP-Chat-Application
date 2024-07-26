import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;


class ChatClient extends JFrame implements ActionListener {
    private JTextField messageField;
    private JTextArea chatArea;
    private JButton sendButton;
    private PrintWriter output;
    private BufferedReader input;
    private Socket socket;

    public ChatClient() {
        setTitle("Chat Client");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);

        messageField = new JTextField(20);
        sendButton = new JButton("Send");
        sendButton.addActionListener(this);

        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        panel.add(messageField);
        panel.add(sendButton);

        add(scrollPane, BorderLayout.CENTER);
        add(panel, BorderLayout.SOUTH);

        setVisible(true);

        try {
            socket = new Socket("localhost", 12345);
            output = new PrintWriter(socket.getOutputStream(), true);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Start a thread to handle receiving messages from the server
            Thread receivingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        String message;
                        while ((message = input.readLine()) != null) {
                            chatArea.append("Server: " + message + "\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
            receivingThread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            // Send message to the server
            output.println(message);
            chatArea.append("Client: " + message + "\n");
            messageField.setText("");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ChatClient();
            }
        });
    }

    // Close the socket when the client window is closed
    @Override
    public void dispose() {
        super.dispose();
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
