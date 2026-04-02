import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

public class MainFrame extends JFrame {

    // ── Panels ──
    private LoginPanel          loginPanel;
    private RegisterPanel       registerPanel;
    private DashboardPanel      dashboardPanel;
    private SchemeFinderPanel   finderPanel;
    private ApplicationPanel    appPanel;
    private DocumentUploadPanel uploadPanel;
    //private ChatbotPanel        chatbotPanel;
    private AlertsPanel         alertsPanel;

    // ── Layout ──
    private CardLayout cardLayout;
    private JPanel     cardContainer;
    private JPanel     sideNav;
    private JPanel     statusBar;
    private JLabel     lblStatusMsg;
    private JLabel     lblUser;
    private JLabel     lblDb;

    // ── State ──
    private User          currentUser;
    private SchemeService service;

    public MainFrame() {
        service = new SchemeService();
        initFrame();
        buildUI();
    }

    private void initFrame() {
        setTitle("YojanaConnect — Government Scheme Awareness System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        setIconTitle();
    }

    private void setIconTitle() {
        // Set taskbar title
        setTitle("YojanaConnect");
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── MENU BAR ──
        setJMenuBar(buildMenuBar());

        // ── TOOLBAR ──
        add(buildToolbar(), BorderLayout.NORTH);

        // ── MAIN AREA: sidebar + card container ──
        JPanel mainArea = new JPanel(new BorderLayout());
        sideNav = buildSideNav();
        sideNav.setVisible(false); // Hidden until logged in

        cardLayout   = new CardLayout();
        cardContainer = new JPanel(cardLayout);
        cardContainer.setBackground(new Color(212, 208, 200));

        // Create all panels
        loginPanel    = new LoginPanel(this, service);
        registerPanel = new RegisterPanel(this, service);
        dashboardPanel  = new DashboardPanel(this, service);
        finderPanel     = new SchemeFinderPanel(this, service);
        appPanel        = new ApplicationPanel(this, service);
        uploadPanel     = new DocumentUploadPanel(this, service);
        //chatbotPanel    = new ChatbotPanel(this, service);
        alertsPanel     = new AlertsPanel(this, service);

        cardContainer.add(loginPanel,    "login");
        cardContainer.add(registerPanel, "register");
        cardContainer.add(dashboardPanel,"dashboard");
        cardContainer.add(finderPanel,   "finder");
        cardContainer.add(appPanel,      "applications");
        cardContainer.add(uploadPanel,   "upload");
        //cardContainer.add(chatbotPanel,  "chatbot");
        cardContainer.add(alertsPanel,   "alerts");

        mainArea.add(sideNav,       BorderLayout.WEST);
        mainArea.add(cardContainer, BorderLayout.CENTER);

        add(mainArea, BorderLayout.CENTER);

        // ── STATUS BAR ──
        add(buildStatusBar(), BorderLayout.SOUTH);

        // Start on login screen
        showPanel("login");
    }

    // ─────────────────────────────────────────
    // MENU BAR
    // ─────────────────────────────────────────
    private JMenuBar buildMenuBar() {
        JMenuBar bar = new JMenuBar();

        JMenu mFile = new JMenu("File");
        mFile.setMnemonic('F');
        JMenuItem miLogin  = new JMenuItem("Login");
        JMenuItem miLogout = new JMenuItem("Logout");
        JMenuItem miExit   = new JMenuItem("Exit");
        miLogin.addActionListener(e -> showPanel("login"));
        miLogout.addActionListener(e -> doLogout());
        miExit.addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(this, "Exit YojanaConnect?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) System.exit(0);
        });
        mFile.add(miLogin); mFile.add(miLogout); mFile.addSeparator(); mFile.add(miExit);

        JMenu mSchemes = new JMenu("Schemes");
        mSchemes.setMnemonic('S');
        JMenuItem miFinder = new JMenuItem("Scheme Finder");
        JMenuItem miApply  = new JMenuItem("My Applications");
        miFinder.addActionListener(e -> showPanel("finder"));
        miApply.addActionListener(e  -> showPanel("applications"));
        mSchemes.add(miFinder); mSchemes.add(miApply);

        JMenu mTools = new JMenu("Tools");
        mTools.setMnemonic('T');
        JMenuItem miChatbot = new JMenuItem("AI Chatbot");
        JMenuItem miUpload  = new JMenuItem("Upload Documents");
        JMenuItem miAlerts  = new JMenuItem("Alerts");
        miChatbot.addActionListener(e -> showPanel("chatbot"));
        miUpload.addActionListener(e  -> showPanel("upload"));
        miAlerts.addActionListener(e  -> showPanel("alerts"));
        mTools.add(miChatbot); mTools.add(miUpload); mTools.add(miAlerts);

        JMenu mHelp = new JMenu("Help");
        mHelp.setMnemonic('H');
        JMenuItem miAbout = new JMenuItem("About YojanaConnect");
        miAbout.addActionListener(e -> JOptionPane.showMessageDialog(this,
            "YojanaConnect v1.0\nGovernment Scheme Awareness System\n\nDeveloped using Java Swing + JDBC\nData source: Open Government Data Platform India\n\nFor support: india.gov.in",
            "About", JOptionPane.INFORMATION_MESSAGE));
        mHelp.add(miAbout);

        bar.add(mFile); bar.add(mSchemes); bar.add(mTools); bar.add(mHelp);
        return bar;
    }

    // ─────────────────────────────────────────
    // TOOLBAR
    // ─────────────────────────────────────────
    private JToolBar buildToolbar() {
        JToolBar tb = new JToolBar();
        tb.setFloatable(false);
        tb.setBackground(new Color(212, 208, 200));
        tb.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(128,128,128)));

        String[][] btns = {
            {"🏠 Home",         "dashboard"},
            {"🔍 Scheme Finder", "finder"},
            {"📋 My Applications","applications"},
            {"📤 Upload Docs",   "upload"},
          //  {"🤖 Chatbot",       "chatbot"},
            {"🔔 Alerts",        "alerts"},
        };

        for (String[] b : btns) {
            JButton btn = new JButton(b[0]);
            btn.setFont(new Font("SansSerif", Font.BOLD, 11));
            btn.setFocusPainted(false);
            btn.setToolTipText(b[0].replaceAll("[^a-zA-Z ]","").trim());
            final String target = b[1];
            btn.addActionListener(e -> showPanel(target));
            tb.add(btn);
            tb.addSeparator(new Dimension(2, 0));
        }

        tb.add(Box.createHorizontalGlue());
        lblUser = new JLabel("  Not logged in  ");
        lblUser.setFont(new Font("SansSerif", Font.BOLD, 11));
        lblUser.setForeground(new Color(0,0,128));
        tb.add(lblUser);

        JButton btnLogout = new JButton("Logout");
        btnLogout.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnLogout.addActionListener(e -> doLogout());
        tb.add(btnLogout);
        tb.addSeparator(new Dimension(6,0));

        return tb;
    }

    // ─────────────────────────────────────────
    // SIDE NAVIGATION
    // ─────────────────────────────────────────
    private JPanel buildSideNav() {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(new Color(30, 40, 80));
        nav.setPreferredSize(new Dimension(190, 0));
        nav.setBorder(BorderFactory.createMatteBorder(0,0,0,1, new Color(80,80,120)));

        // Logo
        JPanel logoArea = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        logoArea.setBackground(new Color(20,28,60));
        logoArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 58));
        JLabel logo = new JLabel("🇮🇳 YojanaConnect");
        logo.setFont(new Font("SansSerif",Font.BOLD,14));
        logo.setForeground(Color.WHITE);
        logoArea.add(logo);
        nav.add(logoArea);

        // Profile area (filled on login)
        JPanel profileArea = new JPanel(new BorderLayout(0,2));
        profileArea.setBackground(new Color(25,35,70));
        profileArea.setBorder(BorderFactory.createEmptyBorder(8,12,8,8));
        profileArea.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        JLabel pName = new JLabel("Guest User");
        pName.setFont(new Font("SansSerif",Font.BOLD,12));
        pName.setForeground(Color.WHITE);
        profileArea.add(pName, BorderLayout.NORTH);
        nav.add(profileArea);

        // Separator
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(80,90,140));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        nav.add(sep);
        nav.add(Box.createVerticalStrut(6));

        // Nav items
        String[][] navItems = {
            {"🏠", "Dashboard",       "dashboard"},
            {"🔍", "Scheme Finder",   "finder"},
            {"📋", "My Applications", "applications"},
            {"📤", "Upload Documents","upload"},
         //   {"🤖", "AI Chatbot",      "chatbot"},
            {"🔔", "Alerts",          "alerts"},
        };

        for (String[] ni : navItems) {
            JButton btn = createNavBtn(ni[0] + "  " + ni[1]);
            final String target = ni[2];
            btn.addActionListener(e -> showPanel(target));
            nav.add(btn);
        }

        nav.add(Box.createVerticalGlue());

        JSeparator sep2 = new JSeparator();
        sep2.setForeground(new Color(80,90,140));
        sep2.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2));
        nav.add(sep2);

        JButton btnLogout = createNavBtn("🚪  Logout");
        btnLogout.addActionListener(e -> doLogout());
        nav.add(btnLogout);
        nav.add(Box.createVerticalStrut(6));

        return nav;
    }

    private JButton createNavBtn(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btn.setForeground(new Color(200,210,240));
        btn.setBackground(new Color(30,40,80));
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 8));
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(new Color(50,70,130)); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(new Color(30,40,80)); }
        });
        return btn;
    }

    // ─────────────────────────────────────────
    // STATUS BAR
    // ─────────────────────────────────────────
    private JPanel buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(212,208,200));
        bar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(128,128,128)));
        bar.setPreferredSize(new Dimension(0,22));

        lblStatusMsg = new JLabel("  Ready");
        lblStatusMsg.setFont(new Font("SansSerif",Font.PLAIN,11));

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT,0,2));
        right.setBackground(new Color(212,208,200));
        lblDb = new JLabel("DB: Offline  ");
        lblDb.setFont(new Font("SansSerif",Font.PLAIN,11));
        lblDb.setForeground(Color.GRAY);
        right.add(lblDb);
        right.add(makeSbSeg("YojanaConnect v1.0"));

        bar.add(lblStatusMsg, BorderLayout.WEST);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JLabel makeSbSeg(String text) {
        JLabel lbl = new JLabel("  " + text + "  ");
        lbl.setFont(new Font("SansSerif",Font.PLAIN,11));
        lbl.setBorder(BorderFactory.createMatteBorder(0,1,0,0,new Color(128,128,128)));
        return lbl;
    }

    // ─────────────────────────────────────────
    // PUBLIC: show a panel by name
    // ─────────────────────────────────────────
    public void showPanel(String name) {
        // Block navigation if not logged in (except login/register)
        if (!name.equals("login") && !name.equals("register") && currentUser == null) {
            showPanel("login");
            return;
        }

        cardLayout.show(cardContainer, name);

        // Show/hide sidebar
        boolean loggedIn = (currentUser != null);
        sideNav.setVisible(loggedIn && !name.equals("login") && !name.equals("register"));

        // Refresh panels that need user data
        if (name.equals("dashboard") && currentUser != null) dashboardPanel.refresh(currentUser);
        if (name.equals("applications") && currentUser != null) appPanel.setUser(currentUser);
        if (name.equals("alerts") && currentUser != null) alertsPanel.setUser(currentUser);
        if (name.equals("finder") && currentUser != null) finderPanel.setUser(currentUser);

        // Update status bar
        String screenName = name.substring(0,1).toUpperCase() + name.substring(1);
        lblStatusMsg.setText("  " + screenName);
    }

    // ─────────────────────────────────────────
    // PUBLIC: set logged-in user
    // ─────────────────────────────────────────
    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            lblUser.setText("  Welcome, " + user.getName() + "  ");
            lblStatusMsg.setText("  Logged in as: " + user.getName());
            lblDb.setText("DB: " + (DBHelper.isConnected() ? "Connected ✓" : "Demo Mode") + "  ");
        }
    }

    public User getCurrentUser() { return currentUser; }

    private void doLogout() {
        int c = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            currentUser = null;
            lblUser.setText("  Not logged in  ");
            sideNav.setVisible(false);
            showPanel("login");
        }
    }
}
