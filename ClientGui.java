import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.html.*;

import java.util.ArrayList;
import java.util.Arrays;

public class ClientGui extends Thread {

  final JTextPane chatTextPane = new JTextPane();
  final JTextPane userListTextPane = new JTextPane();
  final JTextField chatInputField = new JTextField();
  private String oldMsg = "";
  private Thread read;
  private String serverName;
  private int PORT;
  private String name;
  BufferedReader input;
  PrintWriter output;
  Socket server;

  public ClientGui() {
    this.serverName = "localhost";
    this.PORT = 12345;
    this.name = "nickname";

    String fontfamily = "Arial, sans-serif";
    Font font = new Font(fontfamily, Font.PLAIN, 15);

    final JFrame frame = new JFrame("Chat");
    frame.getContentPane().setLayout(null);
    frame.setSize(700, 500);
    frame.setResizable(true);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    // Chat text module
    chatTextPane.setBounds(25, 25, 490, 320);
    chatTextPane.setFont(font);
    chatTextPane.setMargin(new Insets(6, 6, 6, 6));
    chatTextPane.setEditable(false);
    JScrollPane chatTextScrollPane = new JScrollPane(chatTextPane);
    chatTextScrollPane.setBounds(25, 25, 490, 320);

    chatTextPane.setContentType("text/html");
    chatTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // User list module
    userListTextPane.setBounds(520, 25, 156, 320);
    userListTextPane.setEditable(true);
    userListTextPane.setFont(font);
    userListTextPane.setMargin(new Insets(6, 6, 6, 6));
    userListTextPane.setEditable(false);
    JScrollPane userListScrollPane = new JScrollPane(userListTextPane);
    userListScrollPane.setBounds(520, 25, 156, 320);

    userListTextPane.setContentType("text/html");
    userListTextPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true);

    // Chat input field
    chatInputField.setBounds(0, 350, 400, 50);
    chatInputField.setFont(font);
    chatInputField.setMargin(new Insets(6, 6, 6, 6));
    final JScrollPane chatInputScrollPane = new JScrollPane(chatInputField);
    chatInputScrollPane.setBounds(25, 350, 650, 50);

    // Send button
    final JButton sendButton = new JButton("Send");
    sendButton.setFont(font);
    sendButton.setBounds(575, 410, 100, 35);

    // Disconnect button
    final JButton disconnectButton = new JButton("Disconnect");
    disconnectButton.setFont(font);
    disconnectButton.setBounds(25, 410, 130, 35);

    chatInputField.addKeyListener(new KeyAdapter() {
      // Send message on Enter
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
          sendMessage();
        }

        // Get the last message typed
        if (e.getKeyCode() == KeyEvent.VK_UP) {
          String currentMessage = chatInputField.getText().trim();
          chatInputField.setText(oldMsg);
          oldMsg = currentMessage;
        }

        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
          String currentMessage = chatInputField.getText().trim();
          chatInputField.setText(oldMsg);
          oldMsg = currentMessage;
        }
      }
    });

    // Click on send button
    sendButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        sendMessage();
      }
    });

    // Connection view
    final JTextField nameField = new JTextField(this.name);
    final JTextField portField = new JTextField(Integer.toString(this.PORT));
    final JTextField addrField = new JTextField(this.serverName);
    final JButton connectButton = new JButton("Connect");

    // Check if those fields are not empty
    nameField.getDocument().addDocumentListener(new TextListener(nameField, portField, addrField, connectButton));
    portField.getDocument().addDocumentListener(new TextListener(nameField, portField, addrField, connectButton));
    addrField.getDocument().addDocumentListener(new TextListener(nameField, portField, addrField, connectButton));

    // Position of modules
    connectButton.setFont(font);
    addrField.setBounds(25, 380, 135, 40);
    nameField.setBounds(375, 380, 135, 40);
    portField.setBounds(200, 380, 135, 40);
    connectButton.setBounds(575, 380, 100, 40);

    // Default color of chat and user list modules
    chatTextPane.setBackground(Color.LIGHT_GRAY);
    userListTextPane.setBackground(Color.LIGHT_GRAY);

    // Adding elements to the frame
    frame.add(connectButton);
    frame.add(chatTextScrollPane);
    frame.add(userListScrollPane);
    frame.add(nameField);
    frame.add(portField);
    frame.add(addrField);
    frame.setVisible(true);

    // Chat information
    appendToPane(chatTextPane, "<h4>Possible commands in the chat:</h4>"
        + "<ul>"
        + "<li><b>@nickname</b> to send a private message to the user 'nickname'</li>"
        + "<li><b>#d3961b</b> to change the color of your username to the specified hexadecimal code</li>"
        + "<li><b>;)</b>some smileys are implemented</li>"
        + "<li><b>Up arrow key</b> to retrieve the last typed message</li>"
        + "</ul><br/>");

    // On connect
    connectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        try {
          name = nameField.getText();
          String port = portField.getText();
          serverName = addrField.getText();
          PORT = Integer.parseInt(port);

          appendToPane(chatTextPane, "<span>Connecting to " + serverName + " on port " + PORT + "...</span>");
          server = new Socket(serverName, PORT);

          appendToPane(chatTextPane, "<span>Connected to " +
              server.getRemoteSocketAddress() + "</span>");

          input = new BufferedReader(new InputStreamReader(server.getInputStream()));
          output = new PrintWriter(server.getOutputStream(), true);

          // Send nickname to server
          output.println(name);

          // Create a new Read Thread
          read = new Read();
          read.start();
          frame.remove(nameField);
          frame.remove(portField);
          frame.remove(addrField);
          frame.remove(connectButton);
          frame.add(sendButton);
          frame.add(chatInputScrollPane);
          frame.add(disconnectButton);
          frame.revalidate();
          frame.repaint();
          chatTextPane.setBackground(Color.WHITE);
          userListTextPane.setBackground(Color.WHITE);
        } catch (Exception ex) {
          appendToPane(chatTextPane, "<span>Could not connect to Server</span>");
          JOptionPane.showMessageDialog(frame, ex.getMessage());
        }
      }
    });

    // On disconnect
    disconnectButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        frame.add(nameField);
        frame.add(portField);
        frame.add(addrField);
        frame.add(connectButton);
        frame.remove(sendButton);
        frame.remove(chatInputScrollPane);
        frame.remove(disconnectButton);
        frame.revalidate();
        frame.repaint();
        read.interrupt();
        userListTextPane.setText(null);
        chatTextPane.setBackground(Color.LIGHT_GRAY);
        userListTextPane.setBackground(Color.LIGHT_GRAY);
        appendToPane(chatTextPane, "<span>Connection closed.</span>");
        output.close();
      }
    });
  }

  // Check if all fields are not empty
  public class TextListener implements DocumentListener {
    JTextField field1;
    JTextField field2;
    JTextField field3;
    JButton connectButton;

    public TextListener(JTextField field1, JTextField field2, JTextField field3, JButton connectButton) {
      this.field1 = field1;
      this.field2 = field2;
      this.field3 = field3;
      this.connectButton = connectButton;
    }

    public void changedUpdate(DocumentEvent e) {}

    public void removeUpdate(DocumentEvent e) {
      if (field1.getText().trim().equals("") ||
          field2.getText().trim().equals("") ||
          field3.getText().trim().equals("")) {
        connectButton.setEnabled(false);
      } else {
        connectButton.setEnabled(true);
      }
    }

    public void insertUpdate(DocumentEvent e) {
      if (field1.getText().trim().equals("") ||
          field2.getText().trim().equals("") ||
          field3.getText().trim().equals("")) {
        connectButton.setEnabled(false);
      } else {
        connectButton.setEnabled(true);
      }
    }
  }

  // Send messages
  public void sendMessage() {
    try {
      String message = chatInputField.getText().trim();
      if (message.equals("")) {
        return;
      }
      this.oldMsg = message;
      output.println(message);
      chatInputField.requestFocus();
      chatInputField.setText(null);
    } catch (Exception ex) {
      JOptionPane.showMessageDialog(null, ex.getMessage());
      System.exit(0);
    }
  }

  public static void main(String[] args) throws Exception {
    SwingUtilities.invokeLater(() -> {
      new ClientGui();
  });
  }

  // Read new incoming messages
  class Read extends Thread {
    public void run() {
      String message;
      while (!Thread.currentThread().isInterrupted()) {
        try {
          message = input.readLine();
          if (message != null) {
            if (message.charAt(0) == '[') {
              message = message.substring(1, message.length() - 1);
              ArrayList<String> userList = new ArrayList<String>(
                  Arrays.asList(message.split(", "))
              );
              userListTextPane.setText(null);
              for (String user : userList) {
                appendToPane(userListTextPane, "@" + user);
              }
            } else {
              appendToPane(chatTextPane, message);
            }
          }
        } catch (IOException ex) {
          System.err.println("Failed to parse incoming message");
        }
      }
    }
  }

  // Send HTML to pane
  private void appendToPane(JTextPane tp, String msg) {
    HTMLDocument doc = (HTMLDocument) tp.getDocument();
    HTMLEditorKit editorKit = (HTMLEditorKit) tp.getEditorKit();
    try {
      editorKit.insertHTML(doc, doc.getLength(), msg, 0, 0, null);
      tp.setCaretPosition(doc.getLength());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
