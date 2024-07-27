import java.io.*;
import java.net.*;
import javax.swing.*;

public class TcpChatClient {
    private static PrintWriter toServer;
    private static BufferedReader fromServer;
    private static JTextArea chatArea;
    private static JTextField inputField;

    public static void main(String[] args) throws Exception {
        JFrame frame = new JFrame("Client Chat");
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        inputField = new JTextField();

        frame.setLayout(new java.awt.BorderLayout());
        frame.add(new JScrollPane(chatArea), java.awt.BorderLayout.CENTER);
        frame.add(inputField, java.awt.BorderLayout.SOUTH);

        inputField.addActionListener(e -> {
            String message = inputField.getText();
            toServer.println(message);
            chatArea.append("Client: " + message + "\n");
            inputField.setText("");
            if (message.equals("end")) {
                try {
                    fromServer.close();
                    toServer.close();
                } catch (IOException ex) {
                    chatArea.append("Error: " + ex.getMessage() + "\n");
                }
            }
        });

        frame.setSize(400, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);

        try {
            Socket Clt;
            if (args.length > 1) {
                chatArea.append("Usage: java TcpChatClient [hostipaddr]\n");
                System.exit(-1);
            }
            if (args.length == 0)
                Clt = new Socket(InetAddress.getLocalHost(), 5555);
            else
                Clt = new Socket(InetAddress.getByName(args[0]), 5555);

            toServer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Clt.getOutputStream())), true);
            fromServer = new BufferedReader(new InputStreamReader(Clt.getInputStream()));

            String SrvMsg;
            chatArea.append("Type \"end\" to Quit\n");
            while (true) {
                SrvMsg = fromServer.readLine();
                if (SrvMsg == null || SrvMsg.equals("end")) {
                    break;
                }
                chatArea.append("Server: " + SrvMsg + "\n");
            }
            chatArea.append("Server Disconnected\n");
            fromServer.close();
            toServer.close();
            Clt.close();
        } catch (Exception E) {
            chatArea.append("Error: " + E.getMessage() + "\n");
        }
    }
}
