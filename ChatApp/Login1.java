package ChatApp;

import java.awt.*;
import java.awt.event.*;

public class Login1 extends Frame implements ActionListener {
    TextField userField, passField;
    Button loginBtn, resetBtn;
    Label msg;

    public Login1() {
        // ✅ Same size as chat page
        setTitle("Friend1 Login");
        setSize(450, 600);
        setLayout(null);
        setBackground(new Color(240, 255, 240)); // light green shade

        // Header banner
        Label title = new Label("🔒 Secret Chat - Friend1", Label.CENTER);
        title.setBounds(50, 80, 350, 40);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.white);
        title.setBackground(new Color(37, 211, 102)); // WhatsApp-like green
        add(title);

        // Username label
        Label userLabel = new Label("Username:");
        userLabel.setBounds(80, 200, 100, 25);
        userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(userLabel);

        // Username input
        userField = new TextField();
        userField.setBounds(190, 200, 170, 25);
        add(userField);

        // Password label
        Label passLabel = new Label("Password:");
        passLabel.setBounds(80, 250, 100, 25);
        passLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(passLabel);

        // Password input
        passField = new TextField();
        passField.setEchoChar('*');
        passField.setBounds(190, 250, 170, 25);
        add(passField);

        // Login button
        loginBtn = new Button("Login");
        loginBtn.setBounds(140, 320, 80, 35);
        loginBtn.setBackground(new Color(37, 211, 102));
        loginBtn.setForeground(Color.white);
        loginBtn.addActionListener(this);
        add(loginBtn);

        // Reset button
        resetBtn = new Button("Reset");
        resetBtn.setBounds(240, 320, 80, 35);
        resetBtn.setBackground(Color.red);
        resetBtn.setForeground(Color.white);
        resetBtn.addActionListener(this);
        add(resetBtn);

        // Message label
        msg = new Label("", Label.CENTER);
        msg.setBounds(100, 380, 250, 25);
        msg.setFont(new Font("Arial", Font.BOLD, 12));
        add(msg);

        // Close window properly
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }
        });

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginBtn) {
            String user = userField.getText();
            String pass = passField.getText();

            if (user.equals("chelsi") && pass.equals("1234")) {
                msg.setText("✅ Login successful!");
                msg.setForeground(Color.green);
                dispose(); // close login window
                new Friend1(); // open chat window
            } else {
                msg.setText("❌ Invalid credentials. Try again.");
                msg.setForeground(Color.red);
            }
        } else if (e.getSource() == resetBtn) {
            userField.setText("");
            passField.setText("");
            msg.setText("");
        }
    }

    public static void main(String[] args) {
        new Login1();
    }
}
