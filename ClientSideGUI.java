import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientSideGUI extends JFrame {

    private JPanel chatPanel;
    private JTextField inputField;
    private JButton sendButton;
    private PrintWriter out;
    private Socket socket;
    private String username;

    public ClientSideGUI(String serverAddress, int serverPort) {
        setTitle("Java Chat Client");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //Ask for username
        username=(String)JOptionPane.showInputDialog(this,"Enter your username: ","Username",JOptionPane.PLAIN_MESSAGE,null,null,"");
        if(username==null ||username.trim().isEmpty()){
            username="Anonymous";
        }

        // Scrollable chat panel
        chatPanel = new JPanel();
        chatPanel.setLayout(new BoxLayout(chatPanel, BoxLayout.Y_AXIS));
        JScrollPane scrollPane = new JScrollPane(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Bottom panel for input + button
        JPanel bottomPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        sendButton = new JButton("Send");
        bottomPanel.add(inputField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        // Connecting to server
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);

            // Thread to listen for messages
            new Thread(() -> {
                try (BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()))) {

                          String message;
                         while ((message = in.readLine()) != null) {
                             if(!message.startsWith(username+":")){
                                addMessageBubble( message, Color.LIGHT_GRAY, FlowLayout.LEFT);
                             }
                    }

                } catch (IOException e) {
                    addMessageBubble("Disconnected from server", Color.RED, FlowLayout.CENTER);
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Unable to connect to server!");
            System.exit(1);
        }

        // Common send message logic
        Action sendMessage = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = inputField.getText().trim();
                if (!text.isEmpty()) {
                    out.println(username+": "+text);//send with username to the server
                    addMessageBubble("Me: " + text, new Color(173, 216, 230), FlowLayout.RIGHT);
                    inputField.setText("");
                }
            }
        };

        inputField.addActionListener(sendMessage);
        sendButton.addActionListener(sendMessage);
    }

    // Add message bubble
    private void addMessageBubble(String text, Color color, int align) {

        JPanel bubblePanel = new JPanel(new FlowLayout(align));

        JLabel bubble = new JLabel("<html><p style=\"width:200px;\">" + text + "</p></html>");
        bubble.setOpaque(true);
        bubble.setBackground(color);
        bubble.setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));

        bubblePanel.add(bubble);
        chatPanel.add(bubblePanel);

        chatPanel.revalidate();
        chatPanel.repaint();

        // Auto scroll to bottom
        SwingUtilities.invokeLater(() -> {
            JScrollBar bar = ((JScrollPane) chatPanel.getParent().getParent()).getVerticalScrollBar();
            bar.setValue(bar.getMaximum());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientSideGUI client = new ClientSideGUI("localhost", 1234);
            client.setVisible(true);
        });
    }
}
