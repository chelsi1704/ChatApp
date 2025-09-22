package ChatApp;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

class WhatsAppBubble extends Canvas {
    private String text;
    private Color bubbleColor;
    private boolean isMyMessage;
    private String time;

    public WhatsAppBubble(String text, Color bubbleColor, boolean isMyMessage, String time) {
        this.text = text;
        this.bubbleColor = bubbleColor;
        this.isMyMessage = isMyMessage;
        this.time = time;
        setSize(300, 50);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padding = 12;
        int bubbleWidth = getWidth() - 20;

        // Prepare font
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        FontMetrics fm = g2.getFontMetrics();

        // Split text into wrapped lines
        List<String> lines = new ArrayList<>();
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            if (fm.stringWidth(line + word) < bubbleWidth - 2 * padding) {
                line.append(word).append(" ");
            } else {
                lines.add(line.toString());
                line = new StringBuilder(word + " ");
            }
        }
        lines.add(line.toString());

        int lineHeight = fm.getHeight();
        int bubbleHeight = (lines.size() * lineHeight) + 2 * padding + 15; // Extra space for timestamp

        // Draw bubble background with shadow effect
        if (isMyMessage) {
            g2.setColor(new Color(220, 248, 198)); // Light green for my messages
        } else {
            g2.setColor(Color.WHITE); // White for friend's messages
        }
        g2.fillRoundRect(10, 10, bubbleWidth, bubbleHeight, 20, 20);

        // Draw subtle shadow
        g2.setColor(new Color(0, 0, 0, 30));
        g2.drawRoundRect(10, 10, bubbleWidth - 1, bubbleHeight - 1, 20, 20);

        // Draw text inside bubble
        g2.setColor(Color.BLACK);
        int y = 10 + fm.getAscent() + padding;
        for (String l : lines) {
            g2.drawString(l, 20, y);
            y += lineHeight;
        }

        // Draw timestamp
        g2.setColor(Color.GRAY);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        g2.drawString(time, bubbleWidth - fm.stringWidth(time) + 5, bubbleHeight - 5);

        // Resize bubble dynamically
        setSize(bubbleWidth + 20, bubbleHeight + 20);
    }
}

public class Friend1 extends Frame {

    ServerSocket server;
    Socket socket;
    BufferedReader br;
    PrintWriter out;

    Panel chatPanel;
    ScrollPane scrollPane;
    TextField inputField;
    Button sendBtn, exitBtn;
    String myName = "Chelsi";
    String friendName = "Ruchi";

    public Friend1() {
        createWhatsAppUI();
        connectToFriend();
    }

    private void selectAndSendFile() {
        FileDialog fileDialog = new FileDialog(this, "Select File to Send", FileDialog.LOAD);
        fileDialog.setVisible(true);

        String fileName = fileDialog.getFile();
        String filePath = fileDialog.getDirectory();

        if (fileName != null && filePath != null) {
            File file = new File(filePath + fileName);
            sendFile(file);
        }
    }

    private void sendFile(File file) {
        try {
            // Send file metadata first
            String fileInfo = "FILE:" + file.getName() + ":" + file.length();
            sendMessage(fileInfo);

            // Send file content
            FileInputStream fis = new FileInputStream(file);
            OutputStream os = socket.getOutputStream();

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }

            fis.close();

            // Show file sent message in chat
            String fileMsg = "Sent file: " + file.getName(); // No timestamp
            addMessage(fileMsg, new Color(220, 248, 198), true, getTimeStamp());
            saveMessageToFile("Me: " + fileMsg); // Save without timestamp

        } catch (Exception e) {
            e.printStackTrace();
            addMessage("Failed to send file", new Color(255, 200, 200), true, getTimeStamp());
        }
    }

    private void receiveFile(String fileName, long fileSize) {
        try {
            // Create downloads folder if it doesn't exist
            File downloadsDir = new File("downloads");
            if (!downloadsDir.exists()) {
                downloadsDir.mkdir();
            }

            File file = new File("downloads/" + fileName);
            FileOutputStream fos = new FileOutputStream(file);
            InputStream is = socket.getInputStream();

            byte[] buffer = new byte[4096];
            long totalRead = 0;
            int bytesRead;

            while (totalRead < fileSize && (bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
            }

            fos.close();

            // Show file received message
            String fileMsg = "Received file: " + fileName; // No timestamp
            addMessage(fileMsg, Color.WHITE, false, getTimeStamp());
            saveMessageToFile(friendName + ": " + fileMsg); // Save without timestamp

        } catch (Exception e) {
            e.printStackTrace();
            addMessage("Failed to receive file", new Color(255, 200, 200), false, getTimeStamp());
        }
    }

    private void createWhatsAppUI() {
        setTitle("WhatsApp - " + myName);
        setSize(400, 700);
        setLayout(new BorderLayout());
        setBackground(new Color(240, 240, 240));

        // WhatsApp-style Header
        Panel header = new Panel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(new Color(0, 128, 105)); // WhatsApp green
        header.setPreferredSize(new Dimension(400, 60));

        Panel namePanel = new Panel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        namePanel.setBackground(new Color(0, 128, 105));

        Label nameLabel = new Label(friendName);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(Color.WHITE);

        Label statusLabel = new Label("online");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 200, 200));

        namePanel.add(nameLabel);
        namePanel.add(statusLabel);

        header.add(namePanel);
        add(header, BorderLayout.NORTH);

        // Chat area with WhatsApp background
        chatPanel = new Panel();
        chatPanel.setLayout(new GridBagLayout());
        chatPanel.setBackground(new Color(234, 234, 234)); // WhatsApp chat background

        scrollPane = new ScrollPane(ScrollPane.SCROLLBARS_AS_NEEDED);
        scrollPane.add(chatPanel);
        add(scrollPane, BorderLayout.CENTER);

        // Input area (WhatsApp style)
        Panel inputPanel = new Panel(new BorderLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setPreferredSize(new Dimension(400, 60));

        Panel inputContainer = new Panel(new BorderLayout());
        inputContainer.setBackground(Color.WHITE);

        inputField = new TextField();
        inputField.setBackground(new Color(240, 240, 240));
        inputField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        Panel buttonPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        buttonPanel.setBackground(Color.WHITE);

        // Create buttons
        sendBtn = new Button("➤");
        sendBtn.setBackground(new Color(0, 180, 90));
        sendBtn.setForeground(Color.WHITE);
        sendBtn.setFont(new Font("Arial", Font.BOLD, 14));
        sendBtn.setPreferredSize(new Dimension(50, 30));

        Button fileBtn = new Button("📎");
        fileBtn.setBackground(new Color(200, 200, 200));
        fileBtn.setForeground(Color.BLACK);
        fileBtn.setFont(new Font("Arial", Font.PLAIN, 14));
        fileBtn.setPreferredSize(new Dimension(40, 30));
        fileBtn.addActionListener(e -> selectAndSendFile());

        exitBtn = new Button("X");
        exitBtn.setBackground(Color.RED);
        exitBtn.setForeground(Color.WHITE);
        exitBtn.setPreferredSize(new Dimension(40, 30));

        buttonPanel.add(fileBtn);
        buttonPanel.add(sendBtn);
        buttonPanel.add(exitBtn);

        inputContainer.add(inputField, BorderLayout.CENTER);
        inputContainer.add(buttonPanel, BorderLayout.EAST);
        inputPanel.add(inputContainer, BorderLayout.CENTER);

        add(inputPanel, BorderLayout.SOUTH);
        setVisible(true);

        // Load chat history
        loadChatHistory();

        // Event listeners
        sendBtn.addActionListener(e -> sendMessageFromField());
        inputField.addActionListener(e -> sendMessageFromField());
        exitBtn.addActionListener(e -> {
            sendMessage("Exit");
            closeConnection();
        });

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sendMessage("Exit");
                closeConnection();
            }
        });
    }

    private String getTimeStamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return LocalTime.now().format(formatter);
    }

    private void saveMessageToFile(String msg) {
        try (FileWriter fw = new FileWriter("chat_history_friend2.txt", true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw)) {

            // Always append timestamp before saving
            String timeMsg = msg + " (" + getTimeStamp() + ")";
            pw.println(timeMsg);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadChatHistory() {
        File file = new File("chat_history_friend1.txt");
        if (!file.exists())
            return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Parse the saved message format
                if (line.startsWith("Me: ")) {
                    addMessage(line.substring(4), new Color(220, 248, 198), true, "");
                } else if (line.startsWith(friendName + ": ")) {
                    addMessage(line.substring(friendName.length() + 2), Color.WHITE, false, "");
                } else {
                    addMessage(line, Color.LIGHT_GRAY, false, "");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectToFriend() {
        new Thread(() -> {
            try {
                server = new ServerSocket(7777);
                server.setReuseAddress(true);
                socket = server.accept();

                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

                startReading();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }

    private void sendMessageFromField() {
        String msg = inputField.getText().trim();
        if (!msg.isEmpty()) {
            sendMessage(msg);
            String displayMsg = msg; // No timestamp in the message text
            addMessage(displayMsg, new Color(220, 248, 198), true, getTimeStamp());
            saveMessageToFile("Me: " + msg); // Save without timestamp
            inputField.setText("");
        }
    }

    private void startReading() {
        new Thread(() -> {
            try {
                while (true) {
                    String msg = br.readLine();
                    if (msg == null || msg.equalsIgnoreCase("Exit")) {
                        String timeMsg = friendName + " left the chat. (" + getTimeStamp() + ")";
                        addMessage(timeMsg, Color.LIGHT_GRAY, false, getTimeStamp());
                        saveMessageToFile(timeMsg);
                        closeConnection();
                        break;
                    }

                    // Check if it's a file transfer message
                    if (msg.startsWith("FILE:")) {
                        String[] fileInfo = msg.split(":");
                        String fileName = fileInfo[1];
                        long fileSize = Long.parseLong(fileInfo[2]);

                        // Show "receiving file" message
                        addMessage("Receiving file: " + fileName + "...", Color.LIGHT_GRAY, false, getTimeStamp());

                        // Receive the actual file
                        receiveFile(fileName, fileSize);
                    } else {
                        // Regular text message
                        // Regular text message
                        addMessage(msg, Color.WHITE, false, getTimeStamp());
                        saveMessageToFile(friendName + ": " + msg); // Save without timestamp
                    }
                }
            } catch (Exception e) {
                closeConnection();
            }
        }).start();
    }

    private int messageRow = 0;

    private void addMessage(String msg, Color color, boolean isMyMessage, String time) {
        WhatsAppBubble bubble = new WhatsAppBubble(msg, color, isMyMessage, time);

        Panel wrapper = new Panel(new FlowLayout(isMyMessage ? FlowLayout.RIGHT : FlowLayout.LEFT));
        wrapper.setBackground(new Color(234, 234, 234)); // WhatsApp background

        // Add some margin
        wrapper.setPreferredSize(new Dimension(380, bubble.getHeight() + 10));
        wrapper.add(bubble);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = messageRow++;
        gbc.anchor = isMyMessage ? GridBagConstraints.EAST : GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        chatPanel.add(wrapper, gbc);
        chatPanel.validate();
        scrollPane.validate();

        // Auto-scroll to bottom
        scrollPane.setScrollPosition(0, 10000);
    }

    private void closeConnection() {
        try {
            if (br != null)
                br.close();
            if (out != null)
                out.close();
            if (socket != null)
                socket.close();
            if (server != null)
                server.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        dispose();
    }

    public static void main(String[] args) {
        new Friend1();
    }
}