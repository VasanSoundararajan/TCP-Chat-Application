import java.io.*;
import java.net.*;
import javax.swing.*;

public class TcpChatServer {
    private static PrintWriter toClient;
    private static BufferedReader fromClient;
    private static JTextArea chatArea;
    private static JTextField inputField;

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Server Chat");
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(new JScrollPane(chatArea), java.awt.BorderLayout.CENTER);
        frame.add(inputField, java.awt.BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String message = inputField.getText();
            toClient.println(message);
            chatArea.append("Server: " + message + "\n");
            inputField.setText("");
        });

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            ServerSocket Srv = new ServerSocket(5555);
            chatArea.append("Server started\n");
            Socket Clt = Srv.accept();
            chatArea.append("Client connected\n");

            toClient = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Clt.getOutputStream())), true);
            fromClient = new BufferedReader(new InputStreamReader(Clt.getInputStream()));

            String CltMsg;
            while (true) {
                CltMsg = fromClient.readLine();
                if (CltMsg.equals("end")) {
                    break;
                } else {
                    chatArea.append("Client: " + CltMsg + "\n");
                }
            }
            chatArea.append("Client Disconnected\n");
            fromClient.close();
            toClient.close();
            Clt.close();
            Srv.close();
        } catch (Exception E) {
            chatArea.append("Error: " + E.getMessage() + "\n");
        }
    }
}
