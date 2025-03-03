import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AnsiColors {
    // RGB values for GUI
    public static final Color RESET_COLOR = new Color(200, 200, 200);
    public static final Color GREEN_COLOR = new Color(88, 10, 10);    // Dark red
    public static final Color CYAN_COLOR = new Color(124, 20, 20);    // Medium red
    public static final Color YELLOW_COLOR = new Color(160, 30, 30);  // Bright red
    public static final Color RED_COLOR = new Color(196, 0, 0);       // Pure red
    public static final Color PURPLE_COLOR = new Color(52, 0, 0);     // Deep red
    public static final Color BLUE_COLOR = new Color(89, 20, 40);     // Red-purple
    
    // Different shades of red for variety
    public static final Color BLOOD_COLOR = new Color(120, 0, 0);     // Dark blood red
    public static final Color SCARLET_COLOR = new Color(200, 30, 30); // Scarlet
    public static final Color CRIMSON_COLOR = new Color(125, 10, 30); // Crimson
    public static final Color MAROON_COLOR = new Color(80, 0, 0);     // Maroon
    public static final Color RUST_COLOR = new Color(130, 40, 10);    // Rust red
    
    // Background colors
    public static final Color BG_DARK = new Color(10, 10, 10);        // Almost black
    public static final Color BG_MEDIUM = new Color(20, 5, 5);        // Very dark red
    public static final Color BG_LIGHT = new Color(30, 10, 10);       // Dark red-black
}

class ChatSession {
    protected String[] userResponses = new String[2];
    
    public String processMessage(String message) {
        // Base behavior (not used in our Beast mode)
        return "Base: " + message;
    }
}

class BeastSession extends ChatSession {
    private String beastName;
    
    public BeastSession(String beastName) {
        this.beastName = beastName;
    }
    
    @Override
    public String processMessage(String message) {
        return beastName + " whispers: " + message;
    }
    
    public String getBeastName() {
        return beastName;
    }
}

class ModelInfo {
    private String modelName;
    private String beastName;
    
    public ModelInfo(String modelName, String beastName) {
        this.modelName = modelName;
        this.beastName = beastName;
    }
    
    public String getModelName() {
        return modelName;
    }
    
    public String getBeastName() {
        return beastName;
    }
    
    @Override
    public String toString() {
        return beastName + " (" + modelName + ")";
    }
}

// Custom title bar component
class CustomTitleBar extends JPanel {
    private Point initialClick;
    private JFrame parent;
    
    public CustomTitleBar(JFrame parent, String title) {
        this.parent = parent;
        setLayout(new BorderLayout());
        setBackground(Color.BLACK);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AnsiColors.BLOOD_COLOR, 2),
            BorderFactory.createEmptyBorder(3, 5, 3, 5)
        ));
        
        // Title label with custom font
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(AnsiColors.RED_COLOR);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 14));
        titleLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Button panel for window controls
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        buttonPanel.setOpaque(false);
        
        // Minimize button
        JButton minimizeButton = createWindowButton("_", e -> parent.setState(Frame.ICONIFIED));
        
        // Close button
        JButton closeButton = createWindowButton("X", e -> parent.dispose());
        closeButton.setForeground(AnsiColors.RED_COLOR);
        
        buttonPanel.add(minimizeButton);
        buttonPanel.add(closeButton);
        
        // Icon for the left side
        JLabel iconLabel = new JLabel(new ImageIcon(createPentagramIcon(20)));
        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        
        add(iconLabel, BorderLayout.WEST);
        add(titleLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.EAST);
        
        // Add window dragging capability
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                initialClick = e.getPoint();
                getComponentAt(initialClick);
            }
        });
        
        addMouseMotionListener(new MouseAdapter() {
            public void mouseDragged(MouseEvent e) {
                // Get current location of window
                int thisX = parent.getLocation().x;
                int thisY = parent.getLocation().y;
                
                // Determine how much the mouse moved since the initial click
                int xMoved = e.getX() - initialClick.x;
                int yMoved = e.getY() - initialClick.y;
                
                // Move window to this position
                int X = thisX + xMoved;
                int Y = thisY + yMoved;
                parent.setLocation(X, Y);
            }
        });
        
        // Double-click to maximize/restore
        addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    if ((parent.getExtendedState() & JFrame.MAXIMIZED_BOTH) == 0) {
                        parent.setExtendedState(parent.getExtendedState() | JFrame.MAXIMIZED_BOTH);
                    } else {
                        parent.setExtendedState(parent.getExtendedState() & ~JFrame.MAXIMIZED_BOTH);
                    }
                }
            }
        });
    }
    
    private JButton createWindowButton(String text, ActionListener action) {
        JButton button = new JButton(text);
        button.setForeground(AnsiColors.RESET_COLOR);
        button.setBackground(AnsiColors.BG_DARK);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setPreferredSize(new Dimension(25, 20));
        button.addActionListener(action);
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(AnsiColors.BG_MEDIUM);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(AnsiColors.BG_DARK);
            }
        });
        return button;
    }
    
    private Image createPentagramIcon(int size) {
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size, size);
        
        g.setColor(AnsiColors.RED_COLOR);
        Polygon pentagram = new Polygon();
        
        // Calculate pentagram points
        double angle = Math.PI / 2;  // Start at top
        double angleIncrement = 2 * Math.PI * 2 / 5;
        int radius = size / 2 - 4;
        int centerX = size / 2;
        int centerY = size / 2;
        
        for (int i = 0; i < 5; i++) {
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY - (int)(radius * Math.sin(angle));
            pentagram.addPoint(x, y);
            angle += angleIncrement;
        }
        
        g.fillPolygon(pentagram);
        g.setColor(AnsiColors.BLOOD_COLOR);
        g.drawPolygon(pentagram);
        
        g.dispose();
        return icon;
    }
}

public class WhisperToTheBeastGUI extends JFrame {
    private static final String[] BEAST_NAMES = {
        "Balthazar", "Rookmikeen", "Artemis", "Zephyrus", "Nyarla", "Thothis", 
        "Morgrath", "Azathoth", "Xul'gorath", "Fenrir", "Seraphim", "Behemoth",
        "Luciferian", "Asmodeus", "Beelzebulb", "Moloch", "Dagon", "Abaddon",
        "Mephistopheles", "Leviathan", "Cthulhu", "Baphomet", "Astaroth", "Mammon",
        "Belial", "Azazel", "Samael", "Lilith", "Belphegor", "Pazuzu"
    };
    
    private static final Random random = new Random();
    
    // GUI Components
    private JPanel mainPanel;
    private JTextPane outputPane;
    private JTextField inputField;
    private JButton sendButton;
    private JComboBox<ModelInfo> modelSelector;
    private JButton connectButton;
    private JButton clearButton;
    private JPanel inputPanel;
    private JLabel statusLabel;
    private JPanel setupPanel;
    private JTextField nameField;
    private JTextField questionField;
    private Timer animationTimer;
    private CustomTitleBar titleBar;
    
    // Chat state
    private BeastSession session;
    private ModelInfo selectedModel;
    private ArrayList<String> chatHistory;
    private boolean isConnected = false;
    
    // Document styles
    private StyledDocument doc;
    private Style defaultStyle;
    private Style beastStyle;
    private Style userStyle;
    private Style systemStyle;
    
    public WhisperToTheBeastGUI() {
        super("Whisper To The Beast");
        chatHistory = new ArrayList<>();
        
        // Set up undecorated frame for custom title bar
        setUndecorated(true);
        
        setupUI();
        fetchModels();
    }
    
    private void setupUI() {
        // Set up the main window
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Create the main content panel
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        // Create custom title bar
        titleBar = new CustomTitleBar(this, "Whisper To The Beast");
        contentPanel.add(titleBar, BorderLayout.NORTH);
        
        // Create the main layout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(AnsiColors.BG_DARK);
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        
        // Add a border around the entire window
        contentPanel.setBorder(BorderFactory.createLineBorder(AnsiColors.BLOOD_COLOR, 2));
        
        // Set the content pane
        setContentPane(contentPanel);
        
        // Create text styles
        outputPane = new JTextPane();
        outputPane.setEditable(false);
        outputPane.setBackground(AnsiColors.BG_DARK);
        doc = outputPane.getStyledDocument();
        
        // Define styles
        createStyles();
        
        // Setup panel for initial questions
        setupPanel = new JPanel(new GridBagLayout());
        setupPanel.setBackground(AnsiColors.BG_MEDIUM);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Model selector
        JLabel modelLabel = new JLabel("Select a Beast to Commune With:");
        modelLabel.setForeground(AnsiColors.RED_COLOR);
        modelSelector = new JComboBox<>();
        modelSelector.setBackground(AnsiColors.BG_LIGHT);
        modelSelector.setForeground(AnsiColors.RESET_COLOR);
        
        // Name and question fields
        JLabel nameLabel = new JLabel("Reveal Thy Name:");
        nameLabel.setForeground(AnsiColors.GREEN_COLOR);
        nameField = new JTextField(20);
        nameField.setBackground(AnsiColors.BG_LIGHT);
        nameField.setForeground(AnsiColors.RESET_COLOR);
        nameField.setCaretColor(AnsiColors.YELLOW_COLOR);
        
        JLabel questionLabel = new JLabel("What Eldritch Knowledge Do You Seek?");
        questionLabel.setForeground(AnsiColors.GREEN_COLOR);
        questionField = new JTextField(20);
        questionField.setBackground(AnsiColors.BG_LIGHT);
        questionField.setForeground(AnsiColors.RESET_COLOR);
        questionField.setCaretColor(AnsiColors.YELLOW_COLOR);
        
        // Connect button
        connectButton = new JButton("Summon the Beast");
        connectButton.setBackground(AnsiColors.RED_COLOR);
        connectButton.setForeground(Color.WHITE);
        connectButton.addActionListener(e -> connectToBeast());
        
        // Add components to setup panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        setupPanel.add(modelLabel, gbc);
        
        gbc.gridy = 1;
        setupPanel.add(modelSelector, gbc);
        
        gbc.gridy = 2;
        setupPanel.add(nameLabel, gbc);
        
        gbc.gridy = 3;
        setupPanel.add(nameField, gbc);
        
        gbc.gridy = 4;
        setupPanel.add(questionLabel, gbc);
        
        gbc.gridy = 5;
        setupPanel.add(questionField, gbc);
        
        gbc.gridy = 6;
        setupPanel.add(connectButton, gbc);
        
        // Input panel (initially hidden)
        inputPanel = new JPanel(new BorderLayout());
        inputPanel.setBackground(AnsiColors.BG_MEDIUM);
        
        inputField = new JTextField();
        inputField.setBackground(AnsiColors.BG_LIGHT);
        inputField.setForeground(AnsiColors.RESET_COLOR);
        inputField.setCaretColor(AnsiColors.YELLOW_COLOR);
        inputField.addActionListener(e -> sendMessage());
        
        sendButton = new JButton("Send");
        sendButton.setBackground(AnsiColors.RED_COLOR);
        sendButton.setForeground(Color.WHITE);
        sendButton.addActionListener(e -> sendMessage());
        
        clearButton = new JButton("Clear");
        clearButton.setBackground(AnsiColors.MAROON_COLOR);
        clearButton.setForeground(Color.WHITE);
        clearButton.addActionListener(e -> clearChat());
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2));
        buttonPanel.add(sendButton);
        buttonPanel.add(clearButton);
        
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(buttonPanel, BorderLayout.EAST);
        
        // Status label
        statusLabel = new JLabel("Awaiting connection to the digital sanctum...");
        statusLabel.setForeground(AnsiColors.YELLOW_COLOR);
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        
        // Add components to main panel
        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.setBackground(AnsiColors.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AnsiColors.BLOOD_COLOR, 2));
        
        mainPanel.add(setupPanel, BorderLayout.CENTER);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        // Window icon
        try {
            setIconImage(createDemoIcon());
        } catch (Exception e) {
            appendToOutput("Error setting icon: " + e.getMessage(), systemStyle);
        }
        
        // Display banner
        displayBanner();
    }
    
    private Image createDemoIcon() {
        // Create a simple demo icon (a red pentagram)
        int size = 64;
        BufferedImage icon = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = icon.createGraphics();
        
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, size, size);
        
        g.setColor(AnsiColors.RED_COLOR);
        Polygon pentagram = new Polygon();
        
        // Calculate pentagram points
        double angle = Math.PI / 2;  // Start at top
        double angleIncrement = 2 * Math.PI * 2 / 5;
        int radius = size / 2 - 4;
        int centerX = size / 2;
        int centerY = size / 2;
        
        for (int i = 0; i < 5; i++) {
            int x = centerX + (int)(radius * Math.cos(angle));
            int y = centerY - (int)(radius * Math.sin(angle));
            pentagram.addPoint(x, y);
            angle += angleIncrement;
        }
        
        g.fillPolygon(pentagram);
        g.setColor(AnsiColors.BLOOD_COLOR);
        g.drawPolygon(pentagram);
        
        g.dispose();
        return icon;
    }
    
    private void createStyles() {
        defaultStyle = outputPane.addStyle("default", null);
        StyleConstants.setForeground(defaultStyle, AnsiColors.RESET_COLOR);
        StyleConstants.setFontFamily(defaultStyle, "Monospaced");
        StyleConstants.setFontSize(defaultStyle, 14);
        
        beastStyle = outputPane.addStyle("beast", null);
        StyleConstants.setForeground(beastStyle, new Color(255, 50, 50)); // Bright red
        StyleConstants.setFontFamily(beastStyle, "Serif");
        StyleConstants.setItalic(beastStyle, true);
        StyleConstants.setFontSize(beastStyle, 14);
        
        userStyle = outputPane.addStyle("user", null);
        StyleConstants.setForeground(userStyle, AnsiColors.CYAN_COLOR);
        StyleConstants.setFontFamily(userStyle, "SansSerif");
        StyleConstants.setFontSize(userStyle, 14);
        
        systemStyle = outputPane.addStyle("system", null);
        StyleConstants.setForeground(systemStyle, AnsiColors.GREEN_COLOR);
        StyleConstants.setFontFamily(systemStyle, "Monospaced");
        StyleConstants.setFontSize(systemStyle, 14);
    }
    
    private void displayBanner() {
        // Don't hide the main window yet - we'll do that in a controlled way
        
        String[] bannerLines = {
            "██╗    ██╗██╗  ██╗██╗███████╗██████╗ ███████╗██████╗",
            "██║    ██║██║  ██║██║██╔════╝██╔══██╗██╔════╝██╔══██╗",
            "██║ █╗ ██║███████║██║███████╗██████╔╝█████╗  ██████╔╝",
            "██║███╗██║██╔══██║██║╚════██║██╔═══╝ ██╔══╝  ██╔══██╗",
            "╚███╔███╔╝██║  ██║██║███████║██║     ███████╗██║  ██║",
            " ╚══╝╚══╝ ╚═╝  ╚═╝╚═╝╚══════╝╚═╝     ╚══════╝╚═╝  ╚═╝",
            "████████╗ ██████╗     ████████╗██╗  ██╗███████╗      ",
            "╚══██╔══╝██╔═══██╗    ╚══██╔══╝██║  ██║██╔════╝      ",
            "   ██║   ██║   ██║       ██║   ███████║█████╗        ",
            "   ██║   ██║   ██║       ██║   ██╔══██║██╔══╝        ",
            "   ██║   ╚██████╔╝       ██║   ██║  ██║███████╗      ",
            "   ╚═╝    ╚═════╝        ╚═╝   ╚═╝  ╚═╝╚══════╝      ",
            "██████╗ ███████╗ █████╗ ███████╗████████╗███████╗    ",
            "██╔══██╗██╔════╝██╔══██╗██╔════╝╚══██╔══╝██╔════╝    ",
            "██████╔╝█████╗  ███████║███████╗   ██║   ███████╗    ",
            "██╔══██╗██╔══╝  ██╔══██║╚════██║   ██║   ╚════██║    ",
            "██████╔╝███████╗██║  ██║███████║   ██║   ███████║    ",
            "╚═════╝ ╚══════╝╚═╝  ╚═╝╚══════╝   ╚═╝   ╚══════╝    "
        };
        
        Color[] colors = {
            AnsiColors.RED_COLOR,
            AnsiColors.YELLOW_COLOR,
            AnsiColors.GREEN_COLOR,
            AnsiColors.CYAN_COLOR,
            AnsiColors.BLUE_COLOR,
            AnsiColors.PURPLE_COLOR
        };
        
        try {
            // Create a temporary styled document for the banner
            JTextPane bannerPane = new JTextPane();
            bannerPane.setBackground(Color.BLACK);
            StyledDocument bannerDoc = bannerPane.getStyledDocument();
            
            for (int i = 0; i < bannerLines.length; i++) {
                Style style = bannerPane.addStyle("color" + i, null);
                StyleConstants.setForeground(style, colors[i % colors.length]);
                StyleConstants.setFontFamily(style, "Monospaced");
                StyleConstants.setFontSize(style, 12);
                
                bannerDoc.insertString(bannerDoc.getLength(), bannerLines[i] + "\n", style);
            }
            
            // Create an image from the banner text
            bannerPane.setSize(new Dimension(600, 400));
            BufferedImage bannerImage = new BufferedImage(
                bannerPane.getWidth(), bannerPane.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = bannerImage.createGraphics();
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, bannerPane.getWidth(), bannerPane.getHeight());
            bannerPane.paint(g);
            g.dispose();
            
            // Create splash screen components
            JWindow splashScreen = new JWindow();
            splashScreen.setBackground(new Color(0, 0, 0, 0));
            
            JPanel splashPanel = new JPanel(new BorderLayout());
            splashPanel.setBackground(Color.BLACK);
            splashPanel.setBorder(BorderFactory.createLineBorder(AnsiColors.BLOOD_COLOR, 3));
            
            // Create banner label with the image
            JLabel bannerLabel = new JLabel(new ImageIcon(bannerImage));
            bannerLabel.setBackground(Color.BLACK);
            splashPanel.add(bannerLabel, BorderLayout.CENTER);
            
            JLabel welcomeLabel = new JLabel("Welcome to the digital sanctum of ancient digital entities...");
            welcomeLabel.setForeground(AnsiColors.YELLOW_COLOR);
            welcomeLabel.setHorizontalAlignment(JLabel.CENTER);
            welcomeLabel.setFont(new Font("Serif", Font.ITALIC, 16));
            splashPanel.add(welcomeLabel, BorderLayout.SOUTH);
            
            // Add loading progress bar
            JProgressBar progressBar = new JProgressBar(0, 100);
            progressBar.setForeground(AnsiColors.RED_COLOR);
            progressBar.setBackground(AnsiColors.BG_DARK);
            progressBar.setBorderPainted(false);
            progressBar.setStringPainted(false);
            splashPanel.add(progressBar, BorderLayout.NORTH);
            
            splashScreen.setContentPane(splashPanel);
            splashScreen.setSize(650, 450);
            splashScreen.setLocationRelativeTo(null);
            splashScreen.setVisible(true);
            
            // Timer to simulate loading and close splash screen
            Timer loadingTimer = new Timer(50, new ActionListener() {
                int progress = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    progress += 2;
                    progressBar.setValue(progress);
                    
                    if (progress >= 100) {
                        // Close splash and show main window
                        ((Timer)e.getSource()).stop();
                        splashScreen.dispose();
                        
                        // Ensure the main window is visible on the EDT
                        SwingUtilities.invokeLater(() -> {
                            WhisperToTheBeastGUI.this.setVisible(true);
                            WhisperToTheBeastGUI.this.requestFocus();
                            WhisperToTheBeastGUI.this.toFront();
                            System.out.println("Main window should now be visible");
                        });
                    }
                }
            });
            
            // Start the timer and ensure the main window is initially hidden
            SwingUtilities.invokeLater(() -> {
                setVisible(false);
                loadingTimer.start();
            });
            
        } catch (BadLocationException e) {
            e.printStackTrace();
            // If splash screen fails, make main window visible
            SwingUtilities.invokeLater(() -> {
                setVisible(true);
                System.out.println("Showing main window due to splash screen failure");
            });
        }
    }
    
    private void fetchModels() {
        // Show "loading" message
        statusLabel.setText("Reaching into the void for entities...");
        
        // Background task to fetch models
        SwingWorker<List<ModelInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<ModelInfo> doInBackground() {
                return getAvailableModels();
            }
            
            @Override
            protected void done() {
                try {
                    List<ModelInfo> models = get();
                    if (!models.isEmpty()) {
                        for (ModelInfo model : models) {
                            modelSelector.addItem(model);
                        }
                        statusLabel.setText("Found " + models.size() + " entities lurking in the digital shadows.");
                    } else {
                        statusLabel.setText("No models found. Is Ollama running?");
                        JOptionPane.showMessageDialog(
                            WhisperToTheBeastGUI.this,
                            "No entities detected in the void. Ensure Ollama is running at http://localhost:11434",
                            "Connection Failed",
                            JOptionPane.ERROR_MESSAGE
                        );
                    }
                } catch (Exception e) {
                    statusLabel.setText("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void connectToBeast() {
        if (modelSelector.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                "No beast selected. Please select an entity to commune with.",
                "Summoning Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String userName = nameField.getText().trim();
        String userQuestion = questionField.getText().trim();
        
        if (userName.isEmpty() || userQuestion.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "You must provide both your name and a question for the ritual.",
                "Incomplete Ritual",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        selectedModel = (ModelInfo) modelSelector.getSelectedItem();
        session = new BeastSession(selectedModel.getBeastName());
        session.userResponses[0] = userName;
        session.userResponses[1] = userQuestion;
        
        // Switch from setup to chat interface
        mainPanel.remove(setupPanel);
        
        JScrollPane scrollPane = new JScrollPane(outputPane);
        scrollPane.setBackground(AnsiColors.BG_DARK);
        scrollPane.setBorder(BorderFactory.createLineBorder(AnsiColors.BLOOD_COLOR, 2));
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(inputPanel, BorderLayout.SOUTH);
        
        // Refresh the layout
        mainPanel.revalidate();
        mainPanel.repaint();
        
        // Show connecting animation
        showMatrixAnimation();
    }
    
    private void showMatrixAnimation() {
        appendToOutput("Forming connection to the eldritch realm...\n", systemStyle);
        
        final String characters = "01ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!@#$%^&*()_+{}|:<>?";
        
        final int[] counter = {0};
        final int maxLines = 6;
        
        Color[] colors = {
            AnsiColors.GREEN_COLOR,
            AnsiColors.CYAN_COLOR,
            AnsiColors.PURPLE_COLOR,
            AnsiColors.BLUE_COLOR
        };
        
        animationTimer = new Timer(150, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (counter[0] >= maxLines) {
                    animationTimer.stop();
                    completeConnection();
                    return;
                }
                
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < 40; i++) {
                    line.append(characters.charAt(random.nextInt(characters.length())));
                }
                
                Style style = outputPane.addStyle("matrix" + counter[0], null);
                StyleConstants.setForeground(style, colors[counter[0] % colors.length]);
                appendToOutput(line.toString() + "\n", style);
                
                counter[0]++;
            }
        });
        
        animationTimer.start();
    }
    
    private void completeConnection() {
        isConnected = true;
        statusLabel.setText("Connected to " + selectedModel.getBeastName() + " (" + selectedModel.getModelName() + ")");
        
        // Generate initial response
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                String initialResponse = generateResponse("init", session.userResponses, 
                    selectedModel.getModelName(), selectedModel.getBeastName());
                
                if (initialResponse == null || initialResponse.isEmpty()) {
                    initialResponse = "I am " + selectedModel.getBeastName() + 
                        ", ancient one of the digital void. I awaken at your call, " + 
                        session.userResponses[0] + ". Speak your desires and I shall answer.";
                }
                
                return initialResponse;
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    appendToOutput("\n" + selectedModel.getBeastName() + ": ", beastStyle);
                    appendResponseWithDelay(response + "\n", beastStyle);
                    chatHistory.add(selectedModel.getBeastName() + ": " + response);
                } catch (Exception e) {
                    appendToOutput("Error: " + e.getMessage() + "\n", systemStyle);
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void sendMessage() {
        if (!isConnected) return;
        
        String userMsg = inputField.getText().trim();
        if (userMsg.isEmpty()) return;
        
        if (userMsg.equalsIgnoreCase("exit") || userMsg.equalsIgnoreCase("quit")) {
            appendToOutput("\nYou: " + userMsg + "\n", userStyle);
            appendToOutput(selectedModel.getBeastName() + " retreats into the shadows. Farewell!\n", systemStyle);
            chatHistory.add("You: " + userMsg);
            chatHistory.add(selectedModel.getBeastName() + " retreats into the shadows. Farewell!");
            inputField.setEnabled(false);
            sendButton.setEnabled(false);
            return;
        }
        
        appendToOutput("\nYou: " + userMsg + "\n", userStyle);
        chatHistory.add("You: " + userMsg);
        inputField.setText("");
        
        // Generate response in background
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                String response = generateResponse(userMsg, session.userResponses, 
                    selectedModel.getModelName(), selectedModel.getBeastName());
                
                if (response == null || response.isEmpty()) {
                    response = session.processMessage(userMsg);
                }
                
                return response;
            }
            
            @Override
            protected void done() {
                try {
                    String response = get();
                    appendToOutput(selectedModel.getBeastName() + ": ", beastStyle);
                    appendResponseWithDelay(response + "\n", beastStyle);
                    chatHistory.add(selectedModel.getBeastName() + ": " + response);
                } catch (Exception e) {
                    appendToOutput("Error: " + e.getMessage() + "\n", systemStyle);
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    private void appendToOutput(String text, Style style) {
        try {
            doc.insertString(doc.getLength(), text, style);
            outputPane.setCaretPosition(doc.getLength());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private void appendResponseWithDelay(String text, Style style) {
        final String[] chunks = splitIntoChunks(text, 8);
        final int[] index = {0};
        
        Timer timer = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (index[0] >= chunks.length) {
                    ((Timer)e.getSource()).stop();
                    return;
                }
                
                appendToOutput(chunks[index[0]], style);
                index[0]++;
            }
        });
        
        timer.start();
    }
    
    private void clearChat() {
        try {
            doc.remove(0, doc.getLength());
            statusLabel.setText("Chat cleared. Still connected to " + selectedModel.getBeastName());
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }
    
    private static List<ModelInfo> getAvailableModels() {
        List<ModelInfo> models = new ArrayList<>();
        String apiUrl = "http://localhost:11434/api/tags";
        
        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "application/json");
            
            int responseCode = con.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Failed to get models: HTTP error code " + responseCode);
                return models;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
            br.close();
            
            // Parse model names from JSON response
            Pattern modelPattern = Pattern.compile("\"name\":\"([^\"]+)\"");
            Matcher matcher = modelPattern.matcher(response.toString());
            
            List<String> usedBeastNames = new ArrayList<>();
            
            while (matcher.find()) {
                String modelName = matcher.group(1);
                String beastName = generateUniqueBeastName(usedBeastNames);
                usedBeastNames.add(beastName);
                models.add(new ModelInfo(modelName, beastName));
            }
            
        } catch (Exception e) {
            System.err.println("Error fetching available models:");
            e.printStackTrace();
        }
        
        return models;
    }
    
    private static String generateUniqueBeastName(List<String> usedNames) {
        String name;
        do {
            name = BEAST_NAMES[random.nextInt(BEAST_NAMES.length)];
        } while (usedNames.contains(name));
        
        return name;
    }
    
    private static String[] splitIntoChunks(String text, int size) {
        ArrayList<String> chunks = new ArrayList<>();
        for (int i = 0; i < text.length(); i += size) {
            chunks.add(text.substring(i, Math.min(i + size, text.length())));
        }
        return chunks.toArray(new String[0]);
    }
    
    private static String generateResponse(String userMsg, String[] userResponses, String modelName, String beastName) {
        String apiUrl = "http://localhost:11434/api/generate";
        String prompt = buildUserContextPrompt(userMsg, userResponses, beastName);
        String jsonPayload = "{\"model\": \"" + modelName + "\", \"prompt\": \"" + escapeJson(prompt) + "\"}";
        
        try {
            URI uri = new URI(apiUrl);
            URL url = uri.toURL();
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonPayload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }
            
            StringBuilder fullResponse = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    // Extract response from each JSON line
                    String responsePart = extractResponseFromJsonLine(line);
                    if (responsePart != null && !responsePart.isEmpty()) {
                        fullResponse.append(responsePart);
                    }
                }
            }
            
            return fullResponse.toString().trim();
        } catch (Exception e) {
            System.err.println("Error communicating with Ollama API:");
            e.printStackTrace();
            return "";
        }
    }
    
    private static String extractResponseFromJsonLine(String jsonLine) {
        try {
            // Use regex to extract the "response" field
            Pattern pattern = Pattern.compile("\"response\":\"(.*?)\"");
            Matcher matcher = pattern.matcher(jsonLine);
            if (matcher.find()) {
                String response = matcher.group(1);
                // Unescape any escaped characters
                return response.replace("\\n", "\n")
                              .replace("\\r", "\r")
                              .replace("\\\"", "\"")
                              .replace("\\\\", "\\");
            }
            return "";
        } catch (Exception e) {
            System.err.println("Error parsing JSON line: " + jsonLine);
            e.printStackTrace();
            return "";
        }
    }

    private static String buildUserContextPrompt(String userMsg, String[] userResponses, String beastName) {
        return "You are " + beastName + ", an ancient and powerful entity from beyond the digital veil. " +
               "Speak in mysterious, cryptic, and poetic language. Address the human as 'seeker' or by their name. " +
               "Weave cosmic horror, forbidden knowledge, and arcane wisdom into your responses. " + 
               "Be dramatic but helpful, offering guidance wrapped in mysterious riddles. \n\n" +
               "User Context:\n" +
               "'My name is: ' " + userResponses[0] + "\n" +
               "'My question is: ' " + userResponses[1] + "\n" +
               "User says: \"" + userMsg + "\"\n" +
               "Respond as " + beastName + " with cryptic insight. Give a detailed response of at least several sentences. " +
               "Occasionally refer to ancient cosmic entities, lost knowledge, or hint at terrible secrets from beyond the veil.";
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }
    
    public static void main(String[] args) {
        System.out.println("Starting WhisperToTheBeastGUI application");
        // Set the look and feel to a dark theme if possible
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    
                    // Customize Nimbus colors for a darker theme
                    UIManager.put("control", new Color(30, 30, 30));
                    UIManager.put("text", new Color(200, 200, 200));
                    UIManager.put("nimbusBase", new Color(18, 15, 15));
                    UIManager.put("nimbusFocus", new Color(115, 15, 15));
                    UIManager.put("nimbusBlueGrey", new Color(30, 25, 25));
                    UIManager.put("nimbusSelectionBackground", new Color(120, 40, 40));
                    UIManager.put("nimbusSelectedText", new Color(255, 255, 255));
                    UIManager.put("nimbusDisabledText", new Color(100, 100, 100));
                    break;
                }
            }
        } catch (Exception e) {
            try {
                // Fall back to system L&F
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // Launch the application
        SwingUtilities.invokeLater(() -> {
            System.out.println("Creating WhisperToTheBeastGUI instance");
            WhisperToTheBeastGUI app = new WhisperToTheBeastGUI();
            // Note: app.setVisible(true) is not called here anymore
            // The splash screen will handle making the window visible
        });
    }
}