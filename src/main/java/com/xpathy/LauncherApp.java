package com.xpathy;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.datatransfer.*;
import java.net.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import com.xpathy.application.demo.DemoApplication;

public class LauncherApp {

    private static Thread springThread;
    private static ConfigurableApplicationContext springContext;

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(79, 70, 229); // Indigo
    private static final Color PRIMARY_HOVER = new Color(67, 56, 202);
    private static final Color DANGER_COLOR = new Color(239, 68, 68); // Red
    private static final Color DANGER_HOVER = new Color(220, 38, 38);
    private static final Color BACKGROUND = new Color(248, 250, 252); // Light gray-blue
    private static final Color PANEL_BG = Color.WHITE;
    private static final Color TEXT_PRIMARY = new Color(30, 41, 59);
    private static final Color TEXT_SECONDARY = new Color(100, 116, 139);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color INFO_BG = new Color(239, 246, 255); // Light blue
    private static final Color CODE_BG = new Color(241, 245, 249); // Light gray

    public static void main(String[] args) {
        // Set system look and feel for better OS integration
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(LauncherApp::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("XPathy Server Launcher");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 550);
        frame.setResizable(true);

        // Main panel with custom background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                // Gradient background
                GradientPaint gp = new GradientPaint(0, 0, BACKGROUND, 0, getHeight(), new Color(226, 232, 240));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new GridBagLayout());

        // Scrollable container
        JPanel containerPanel = new JPanel();
        containerPanel.setLayout(new BoxLayout(containerPanel, BoxLayout.Y_AXIS));
        containerPanel.setBackground(new Color(0, 0, 0, 0));

        // Content panel with rounded borders
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(PANEL_BG);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(30, 40, 30, 40)
        ));
        contentPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Title label
        JLabel titleLabel = new JLabel("XPathy Server");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(TEXT_PRIMARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Subtitle
        JLabel subtitleLabel = new JLabel("Configure and launch your server");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Status indicator
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        statusPanel.setBackground(PANEL_BG);
        statusPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel statusDot = new JLabel("●");
        statusDot.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        statusDot.setForeground(TEXT_SECONDARY);

        JLabel statusLabel = new JLabel("Server Offline");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(TEXT_SECONDARY);

        statusPanel.add(statusDot);
        statusPanel.add(statusLabel);

        // Port input section
        JPanel portPanel = new JPanel();
        portPanel.setLayout(new BoxLayout(portPanel, BoxLayout.Y_AXIS));
        portPanel.setBackground(PANEL_BG);
        portPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel portLabel = new JLabel("Port Number");
        portLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        portLabel.setForeground(TEXT_PRIMARY);
        portLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextField portField = new JTextField("5055");
        portField.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        portField.setHorizontalAlignment(JTextField.CENTER);
        portField.setMaximumSize(new Dimension(200, 40));
        portField.setPreferredSize(new Dimension(200, 40));
        portField.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));

        portPanel.add(portLabel);
        portPanel.add(Box.createVerticalStrut(8));
        portPanel.add(portField);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setBackground(PANEL_BG);
        buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = createStyledButton("Start Server", PRIMARY_COLOR, PRIMARY_HOVER);
        JButton stopButton = createStyledButton("Stop Server", DANGER_COLOR, DANGER_HOVER);
        stopButton.setEnabled(false);

        buttonPanel.add(startButton);
        buttonPanel.add(stopButton);

        // Server info panel (initially hidden)
        JPanel serverInfoPanel = createServerInfoPanel("8080");
        serverInfoPanel.setVisible(false);
        serverInfoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Add all components to content panel
        contentPanel.add(titleLabel);
        contentPanel.add(Box.createVerticalStrut(5));
        contentPanel.add(subtitleLabel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(statusPanel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(portPanel);
        contentPanel.add(Box.createVerticalStrut(25));
        contentPanel.add(buttonPanel);

        // Add content panel to container
        containerPanel.add(Box.createVerticalStrut(20));
        containerPanel.add(contentPanel);
        containerPanel.add(Box.createVerticalStrut(20));
        containerPanel.add(serverInfoPanel);
        containerPanel.add(Box.createVerticalStrut(20));

        // Add container to scroll pane
        JScrollPane scrollPane = new JScrollPane(containerPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Add scroll pane to main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        mainPanel.add(scrollPane, gbc);

        frame.setContentPane(mainPanel);

        // Start button action
        startButton.addActionListener(e -> {
            String port = portField.getText().trim();
            if (!port.matches("\\d{2,5}")) {
                showStyledDialog(frame, "Invalid port number! Please enter a valid port (2-5 digits).", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            portField.setEnabled(false);

            // Update status
            statusDot.setForeground(SUCCESS_COLOR);
            statusLabel.setText("Server Running on port " + port);
            statusLabel.setForeground(SUCCESS_COLOR);

            springThread = new Thread(() -> {
                SpringApplication app = new SpringApplication(DemoApplication.class);
                springContext = app.run("--server.port=" + port);
            });

            springThread.setDaemon(false);
            springThread.start();

            // Update and show server info panel
            updateServerInfoPanel(serverInfoPanel, port);
            serverInfoPanel.setVisible(true);

            // Resize window if needed
            frame.setSize(600, 700);

            // Scroll to show the info panel
            SwingUtilities.invokeLater(() -> {
                serverInfoPanel.scrollRectToVisible(serverInfoPanel.getBounds());
            });

            showStyledDialog(frame, "Server started successfully on port " + port, "Success", JOptionPane.INFORMATION_MESSAGE);
        });

        // Stop button action
        stopButton.addActionListener(e -> {
            int confirm = showStyledConfirmDialog(frame, "Are you sure you want to stop the server?", "Confirm Stop");
            if (confirm == JOptionPane.YES_OPTION) {
                // Stop the Spring Boot server gracefully
                if (springContext != null) {
                    springContext.close();
                    springContext = null;
                }

                if (springThread != null && springThread.isAlive()) {
                    springThread.interrupt();
                }

                // Reset UI state
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                portField.setEnabled(true);

                // Update status
                statusDot.setForeground(TEXT_SECONDARY);
                statusLabel.setText("Server Offline");
                statusLabel.setForeground(TEXT_SECONDARY);

                // Hide server info panel
                serverInfoPanel.setVisible(false);

                // Resize window back to original size
                frame.setSize(600, 550);

                showStyledDialog(frame, "Server stopped successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    // Create server info panel with access URLs
    private static JPanel createServerInfoPanel(String port) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_BG);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(20, new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(25, 40, 25, 40)
        ));
        panel.setMaximumSize(new Dimension(520, 2000));

        // Title
        JLabel infoTitle = new JLabel("🌐 Server Access Information");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        infoTitle.setForeground(TEXT_PRIMARY);
        infoTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(infoTitle);
        panel.add(Box.createVerticalStrut(20));

        // This will be populated when server starts
        return panel;
    }

    // Update server info panel with actual port and IP
    private static void updateServerInfoPanel(JPanel panel, String port) {
        // Clear existing content except title
        Component[] components = panel.getComponents();
        for (int i = 2; i < components.length; i++) {
            panel.remove(components[i]);
        }

        String localIP = getLocalIPAddress();

        // Local access section
        panel.add(createSectionLabel("Local Access"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoText("Access from this computer:"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createURLPanel("http://localhost:" + port + "/execute?expression={xpathy snippet}"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createInfoText("Example:"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createURLPanel("http://localhost:" + port + "/execute?expression=id.contains(\"id-1\");", true));

        panel.add(Box.createVerticalStrut(25));

        // Network access section
        panel.add(createSectionLabel("Network Access"));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createInfoText("Access from other devices on the same network:"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createURLPanel("http://" + localIP + ":" + port + "/execute?expression={xpathy snippet}"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createInfoText("Example:"));
        panel.add(Box.createVerticalStrut(8));
        panel.add(createURLPanel("http://" + localIP + ":" + port + "/execute?expression=id.contains(\"id-1\");", true));

        panel.add(Box.createVerticalStrut(20));

        // Help text
        JTextArea helpText = new JTextArea(
            "💡 Tip: Replace {xpathy snippet} with your XPath expression. " +
            "Make sure devices are on the same network and firewall allows connections on port " + port + "."
        );
        helpText.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        helpText.setForeground(TEXT_SECONDARY);
        helpText.setBackground(INFO_BG);
        helpText.setLineWrap(true);
        helpText.setWrapStyleWord(true);
        helpText.setEditable(false);
        helpText.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(191, 219, 254)),
            BorderFactory.createEmptyBorder(12, 15, 12, 15)
        ));
        helpText.setMaximumSize(new Dimension(440, 200));
        helpText.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(helpText);

        panel.revalidate();
        panel.repaint();
    }

    // Create section label
    private static JLabel createSectionLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 16));
        label.setForeground(PRIMARY_COLOR);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Create info text
    private static JLabel createInfoText(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(TEXT_SECONDARY);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    // Create URL panel with copy button
    private static JPanel createURLPanel(String url, boolean isExample) {
        JPanel urlPanel = new JPanel(new BorderLayout(10, 0));
        urlPanel.setBackground(CODE_BG);
        urlPanel.setBorder(BorderFactory.createCompoundBorder(
            new RoundedBorder(8, new Color(203, 213, 225)),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        urlPanel.setMaximumSize(new Dimension(440, 60));

        JTextArea urlText = new JTextArea(url);
        urlText.setFont(new Font("Consolas", isExample ? Font.PLAIN : Font.BOLD, 11));
        urlText.setForeground(isExample ? TEXT_SECONDARY : PRIMARY_COLOR);
        urlText.setBackground(CODE_BG);
        urlText.setLineWrap(true);
        urlText.setWrapStyleWord(false);
        urlText.setEditable(false);

        JButton copyButton = new JButton("📋 Copy");
        copyButton.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        copyButton.setForeground(PRIMARY_COLOR);
        copyButton.setBackground(PANEL_BG);
        copyButton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(PRIMARY_COLOR, 1),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)
        ));
        copyButton.setFocusPainted(false);
        copyButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        copyButton.addActionListener(e -> {
            StringSelection selection = new StringSelection(url);
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
            copyButton.setText("✓ Copied!");
            Timer timer = new Timer(2000, evt -> copyButton.setText("📋 Copy"));
            timer.setRepeats(false);
            timer.start();
        });

        urlPanel.add(urlText, BorderLayout.CENTER);
        urlPanel.add(copyButton, BorderLayout.EAST);

        return urlPanel;
    }

    private static JPanel createURLPanel(String url) {
        return createURLPanel(url, false);
    }

    // Get local IP address
    private static String getLocalIPAddress() {
        try {
            // Try to get the local network IP address
            for (NetworkInterface networkInterface : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                    for (InetAddress address : java.util.Collections.list(networkInterface.getInetAddresses())) {
                        if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                            String ip = address.getHostAddress();
                            // Prefer 192.168.x.x or 10.x.x.x addresses
                            if (ip.startsWith("192.168.") || ip.startsWith("10.")) {
                                return ip;
                            }
                        }
                    }
                }
            }
            // Fallback to any non-loopback IPv4 address
            for (NetworkInterface networkInterface : java.util.Collections.list(NetworkInterface.getNetworkInterfaces())) {
                for (InetAddress address : java.util.Collections.list(networkInterface.getInetAddresses())) {
                    if (address instanceof Inet4Address && !address.isLoopbackAddress()) {
                        return address.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "localhost";
    }

    // Create styled button with hover effect
    private static JButton createStyledButton(String text, Color bgColor, Color hoverColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (getModel().isPressed()) {
                    g2d.setColor(hoverColor.darker());
                } else if (getModel().isRollover() && isEnabled()) {
                    g2d.setColor(hoverColor);
                } else {
                    g2d.setColor(isEnabled() ? bgColor : new Color(203, 213, 225));
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                g2d.setColor(isEnabled() ? Color.WHITE : new Color(148, 163, 184));
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);

                g2d.dispose();
            }
        };

        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(140, 40));
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return button;
    }

    // Custom rounded border
    static class RoundedBorder extends AbstractBorder {
        private int radius;
        private Color color;

        RoundedBorder(int radius, Color color) {
            this.radius = radius;
            this.color = color;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(color);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRoundRect(x + 1, y + 1, width - 2, height - 2, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(2, 2, 2, 2);
        }
    }

    // Styled dialog methods
    private static void showStyledDialog(JFrame parent, String message, String title, int messageType) {
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("Panel.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        JOptionPane.showMessageDialog(parent, message, title, messageType);
    }

    private static int showStyledConfirmDialog(JFrame parent, String message, String title) {
        UIManager.put("OptionPane.background", PANEL_BG);
        UIManager.put("Panel.background", PANEL_BG);
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
        return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_OPTION);
    }
}

