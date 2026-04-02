import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;
import java.io.File;

// ═══════════════════════════════════════════════════════════════
//  PANELS.java  —  All Swing UI panels for YojanaConnect
//  Contains: LoginPanel, RegisterPanel, DashboardPanel,
//            SchemeFinderPanel, ApplicationPanel,
//            DocumentUploadPanel, ChatbotPanel, AlertsPanel
// ═══════════════════════════════════════════════════════════════

// ─────────────────────────────────────────
// LOGIN PANEL
// ─────────────────────────────────────────
class LoginPanel extends JPanel {

    private JTextField  txtUserId;
    private JPasswordField txtPassword;
    private JCheckBox   chkRemember;
    private JButton     btnLogin, btnRegister, btnCancel;
    private SchemeService service;
    private MainFrame mainFrame;

    public LoginPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        buildUI();
    }

    private void buildUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(212, 208, 200));

        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(new Color(212, 208, 200));
        center.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(128, 128, 128), 1),
            BorderFactory.createEmptyBorder(24, 32, 24, 32)
        ));
        center.setPreferredSize(new Dimension(400, 380));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Logo area
        JLabel lblIcon = new JLabel("🇮🇳 YojanaConnect", SwingConstants.CENTER);
        lblIcon.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblIcon.setForeground(new Color(0, 0, 128));
        gbc.gridx=0; gbc.gridy=0; gbc.gridwidth=2; gbc.insets=new Insets(0,5,2,5);
        center.add(lblIcon, gbc);

        JLabel lblSub = new JLabel("Government Scheme Awareness System", SwingConstants.CENTER);
        lblSub.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblSub.setForeground(Color.GRAY);
        gbc.gridy=1; gbc.insets=new Insets(0,5,16,5);
        center.add(lblSub, gbc);

        JSeparator sep = new JSeparator();
        gbc.gridy=2; gbc.insets=new Insets(0,5,12,5);
        center.add(sep, gbc);

        // User ID
        gbc.gridwidth=1; gbc.insets=new Insets(5,5,2,5);
        gbc.gridx=0; gbc.gridy=3; gbc.weightx=0.3;
        center.add(new JLabel("User ID / Mobile:"), gbc);
        txtUserId = new JTextField("9876543210");
        gbc.gridx=1; gbc.weightx=0.7;
        center.add(txtUserId, gbc);

        // Password
        gbc.gridx=0; gbc.gridy=4; gbc.weightx=0.3;
        center.add(new JLabel("Password:"), gbc);
        txtPassword = new JPasswordField("password");
        gbc.gridx=1; gbc.weightx=0.7;
        center.add(txtPassword, gbc);

        // Remember me
        chkRemember = new JCheckBox("Remember Me", true);
        chkRemember.setBackground(new Color(212, 208, 200));
        gbc.gridx=0; gbc.gridy=5; gbc.gridwidth=2; gbc.insets=new Insets(6,5,6,5);
        center.add(chkRemember, gbc);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnPanel.setBackground(new Color(212, 208, 200));
        btnLogin    = createButton("Login",    new Color(0,0,128), Color.WHITE);
        btnRegister = createButton("Register", null, null);
        btnCancel   = createButton("Cancel",   null, null);
        btnPanel.add(btnLogin);
        btnPanel.add(btnRegister);
        btnPanel.add(btnCancel);
        gbc.gridy=6; gbc.insets=new Insets(10,5,5,5);
        center.add(btnPanel, gbc);

        JLabel lblFooter = new JLabel("Powered by Official Government Data · india.gov.in", SwingConstants.CENTER);
        lblFooter.setFont(new Font("SansSerif", Font.PLAIN, 10));
        lblFooter.setForeground(Color.GRAY);
        gbc.gridy=7; gbc.insets=new Insets(8,5,0,5);
        center.add(lblFooter, gbc);

        add(center);

        // Events
        btnLogin.addActionListener(e -> doLogin());
        txtPassword.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) doLogin();
            }
        });
        btnRegister.addActionListener(e -> mainFrame.showPanel("register"));
        btnCancel.addActionListener(e -> System.exit(0));
    }

    private void doLogin() {
        String uid = txtUserId.getText().trim();
        String pwd = new String(txtPassword.getPassword()).trim();
        if (uid.isEmpty() || pwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter User ID and Password.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        User user = service.validateLogin(uid, pwd);
        if (user != null) {
            mainFrame.setCurrentUser(user);
            mainFrame.showPanel("dashboard");
        } else {
            JOptionPane.showMessageDialog(this, "Invalid credentials. Please try again.", "Login Failed", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(90, 26));
        if (bg != null) { btn.setBackground(bg); btn.setForeground(fg); btn.setOpaque(true); }
        return btn;
    }
}

// ─────────────────────────────────────────
// REGISTER PANEL
// ─────────────────────────────────────────
class RegisterPanel extends JPanel {

    private JTextField txtName, txtAge, txtIncome, txtMobile;
    private JRadioButton rbMale, rbFemale, rbOther;
    private JComboBox<String> cmbCategory, cmbState, cmbOccupation;
    private JCheckBox chkBPL, chkPWD;
    private JButton btnSave, btnCancel;
    private SchemeService service;
    private MainFrame mainFrame;

    public RegisterPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212, 208, 200));

        // Title
        JLabel title = new JLabel("  Register New User", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(0, 0, 128));
        title.setPreferredSize(new Dimension(0, 30));
        add(title, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(new Color(212, 208, 200));
        tabs.addTab("Personal Info", buildPersonalTab());
        tabs.addTab("Address", buildAddressTab());
        tabs.addTab("Account", buildAccountTab());
        add(tabs, BorderLayout.CENTER);

        // Buttons
        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        btnBar.setBackground(new Color(212, 208, 200));
        btnBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(128, 128, 128)));
        btnSave   = new JButton("Save & Register");
        btnCancel = new JButton("Cancel");
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSave.setBackground(new Color(0, 0, 128));
        btnSave.setForeground(Color.WHITE);
        btnSave.setOpaque(true);
        btnBar.add(btnSave);
        btnBar.add(btnCancel);
        add(btnBar, BorderLayout.SOUTH);

        btnSave.addActionListener(e -> doRegister());
        btnCancel.addActionListener(e -> mainFrame.showPanel("login"));
    }

    private JPanel buildPersonalTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 5, 5, 5);
        g.fill = GridBagConstraints.HORIZONTAL;

        addRow(p, g, 0, "Full Name:", txtName = new JTextField());
        addRow(p, g, 1, "Age:", txtAge = new JTextField());

        // Gender radios
        g.gridx=0; g.gridy=2; g.weightx=0;
        p.add(new JLabel("Gender:"), g);
        JPanel gpan = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gpan.setBackground(new Color(212, 208, 200));
        ButtonGroup bg = new ButtonGroup();
        rbMale = new JRadioButton("Male", true); rbMale.setBackground(new Color(212,208,200));
        rbFemale = new JRadioButton("Female"); rbFemale.setBackground(new Color(212,208,200));
        rbOther = new JRadioButton("Other"); rbOther.setBackground(new Color(212,208,200));
        bg.add(rbMale); bg.add(rbFemale); bg.add(rbOther);
        gpan.add(rbMale); gpan.add(rbFemale); gpan.add(rbOther);
        g.gridx=1; g.weightx=1;
        p.add(gpan, g);

        // Category
        cmbCategory = new JComboBox<>(new String[]{"OBC","General","SC/ST","EWS"});
        addRow(p, g, 3, "Category:", cmbCategory);

        // State
        cmbState = new JComboBox<>(new String[]{"Tamil Nadu","Maharashtra","Uttar Pradesh","Karnataka","Andhra Pradesh","Telangana","Rajasthan","Gujarat","West Bengal","Kerala","Bihar","Punjab"});
        addRow(p, g, 4, "State / UT:", cmbState);

        // Occupation
        cmbOccupation = new JComboBox<>(new String[]{"Farmer","Labourer","Self-Employed","Salaried","Student","Healthcare","Construction","Unemployed"});
        addRow(p, g, 5, "Occupation:", cmbOccupation);

        // Income
        addRow(p, g, 6, "Annual Income (Rs.):", txtIncome = new JTextField());

        // Mobile
        addRow(p, g, 7, "Mobile Number:", txtMobile = new JTextField());

        // Checkboxes
        chkBPL = new JCheckBox("BPL Card Holder"); chkBPL.setBackground(new Color(212,208,200));
        chkPWD = new JCheckBox("Person with Disability (PWD)"); chkPWD.setBackground(new Color(212,208,200));
        g.gridx=0; g.gridy=8; g.gridwidth=2;
        p.add(chkBPL, g);
        g.gridy=9;
        p.add(chkPWD, g);

        return p;
    }

    private JPanel buildAddressTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5); g.fill = GridBagConstraints.HORIZONTAL;
        addRow(p, g, 0, "Village / Town:", new JTextField());
        addRow(p, g, 1, "District:", new JTextField());
        addRow(p, g, 2, "Pincode:", new JTextField());
        return p;
    }

    private JPanel buildAccountTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createEmptyBorder(14, 18, 14, 18));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,5,5,5); g.fill = GridBagConstraints.HORIZONTAL;
        addRow(p, g, 0, "Create Password:", new JPasswordField());
        addRow(p, g, 1, "Confirm Password:", new JPasswordField());
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx=0; g.gridy=row; g.gridwidth=1; g.weightx=0;
        p.add(new JLabel(label), g);
        g.gridx=1; g.weightx=1;
        p.add(field, g);
    }

    private void doRegister() {
        if (txtName == null || txtName.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User u = new User();
            u.setName(txtName.getText().trim());
            u.setAge(Integer.parseInt(txtAge.getText().trim()));
            u.setGender(rbMale.isSelected() ? "Male" : rbFemale.isSelected() ? "Female" : "Other");
            u.setCategory((String) cmbCategory.getSelectedItem());
            u.setState((String) cmbState.getSelectedItem());
            u.setOccupation((String) cmbOccupation.getSelectedItem());
            u.setAnnualIncome(Double.parseDouble(txtIncome.getText().trim()));
            u.setMobileNumber(txtMobile.getText().trim());
            u.setBplCardHolder(chkBPL.isSelected());
            u.setPwdStatus(chkPWD.isSelected());
            boolean ok = service.registerUser(u);
            if (ok) {
                JOptionPane.showMessageDialog(this, "Registration successful! Please login.", "Success", JOptionPane.INFORMATION_MESSAGE);
                mainFrame.showPanel("login");
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid age and income values.", "Validation Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

// ─────────────────────────────────────────
// DASHBOARD PANEL
// ─────────────────────────────────────────
class DashboardPanel extends JPanel {

    private SchemeService service;
    private MainFrame mainFrame;
    private User currentUser;

    public DashboardPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        setLayout(new BorderLayout());
        setBackground(new Color(212, 208, 200));
    }

    public void refresh(User user) {
        this.currentUser = user;
        removeAll();
        buildUI();
        revalidate();
        repaint();
    }

    private void buildUI() {
        // Title bar
        JLabel title = new JLabel("  Dashboard — Welcome, " + (currentUser != null ? currentUser.getName() : "User"), SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(Color.WHITE);
        title.setOpaque(true);
        title.setBackground(new Color(0, 0, 128));
        title.setPreferredSize(new Dimension(0, 28));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBackground(new Color(212, 208, 200));
        content.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // Stat cards row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 0));
        statsRow.setBackground(new Color(212, 208, 200));
        statsRow.add(makeStatCard("Eligible Schemes", "12", new Color(0, 0, 128)));
        statsRow.add(makeStatCard("Applications", "3", new Color(0, 0, 128)));
        statsRow.add(makeStatCard("Approved", "1", new Color(0, 102, 0)));
        statsRow.add(makeStatCard("New Alerts", "4", new Color(180, 0, 0)));
        content.add(statsRow, BorderLayout.NORTH);

        // Bottom split
        JPanel middle = new JPanel(new GridLayout(1, 2, 10, 0));
        middle.setBackground(new Color(212, 208, 200));

        // Scheme table
        JPanel schemePan = makeBorderedPanel("Top Matching Schemes");
        String[] cols = {"Scheme Name","Category","Benefit","Match %"};
        Object[][] data = {
            {"Ayushman Bharat PM-JAY","Health","Rs.5 Lakh/yr","98%"},
            {"PM Kisan Samman Nidhi","Agriculture","Rs.6,000/yr","95%"},
            {"PM Awas Yojana Gramin","Housing","Rs.1.5 Lakh","89%"},
            {"National Scholarship","Education","Rs.20K/month","82%"},
            {"PM Mudra Yojana","Business","Loan Rs.10L","78%"},
        };
        JTable tbl = new JTable(data, cols);
        tbl.setSelectionBackground(new Color(0, 0, 128));
        tbl.setSelectionForeground(Color.WHITE);
        tbl.setRowHeight(20);
        tbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tbl.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(new Color(212, 208, 200));
        JScrollPane sp = new JScrollPane(tbl);
        sp.setBorder(BorderFactory.createLineBorder(new Color(128,128,128)));
        schemePan.add(sp, BorderLayout.CENTER);

        JButton btnView = new JButton("Go to Scheme Finder →");
        btnView.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnView.setBackground(new Color(0,0,128));
        btnView.setForeground(Color.WHITE);
        btnView.setOpaque(true);
        JPanel btnWrap = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 4));
        btnWrap.setBackground(new Color(212,208,200));
        btnWrap.add(btnView);
        schemePan.add(btnWrap, BorderLayout.SOUTH);
        btnView.addActionListener(e -> mainFrame.showPanel("finder"));

        // Notifications panel
        JPanel notifPan = makeBorderedPanel("Recent Alerts & Notifications");
        JPanel notifList = new JPanel();
        notifList.setLayout(new BoxLayout(notifList, BoxLayout.Y_AXIS));
        notifList.setBackground(Color.WHITE);

        List<Notification> notifs = service.getNotifications(currentUser != null ? currentUser.getUserId() : 1);
        for (Notification n : notifs) {
            JPanel item = new JPanel(new BorderLayout(6, 0));
            item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 54));
            item.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(210,210,210)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
            ));
            Color bg = n.getType().equals("FRAUD") ? new Color(255,230,230) :
                       n.getType().equals("DEADLINE") ? new Color(255,253,220) :
                       n.getType().equals("APPROVED") ? new Color(230,245,230) :
                       new Color(230,235,255);
            item.setBackground(bg);
            JLabel msgLbl = new JLabel("<html><b>" + n.getMessage().substring(0, Math.min(n.getMessage().length(), 55)) + "...</b></html>");
            msgLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
            JLabel dateLbl = new JLabel(n.getSentDate());
            dateLbl.setFont(new Font("SansSerif", Font.PLAIN, 9));
            dateLbl.setForeground(Color.GRAY);
            item.add(msgLbl, BorderLayout.CENTER);
            item.add(dateLbl, BorderLayout.SOUTH);
            notifList.add(item);
        }
        JScrollPane nsp = new JScrollPane(notifList);
        nsp.setBorder(BorderFactory.createLineBorder(new Color(128,128,128)));
        notifPan.add(nsp, BorderLayout.CENTER);

        middle.add(schemePan);
        middle.add(notifPan);
        content.add(middle, BorderLayout.CENTER);
        add(content, BorderLayout.CENTER);
    }

    private JPanel makeStatCard(String label, String value, Color valueColor) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180,180,180)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx=0; g.gridy=0; g.insets=new Insets(0,0,4,0);
        JLabel valLbl = new JLabel(value, SwingConstants.CENTER);
        valLbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        valLbl.setForeground(valueColor);
        card.add(valLbl, g);
        g.gridy=1;
        JLabel lblLbl = new JLabel(label, SwingConstants.CENTER);
        lblLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblLbl.setForeground(Color.GRAY);
        card.add(lblLbl, g);
        return card;
    }

    private JPanel makeBorderedPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 6));
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(128,128,128)), title,
            TitledBorder.LEFT, TitledBorder.TOP,
            new Font("SansSerif", Font.BOLD, 11)
        ));
        return p;
    }
}

// ─────────────────────────────────────────
// SCHEME FINDER PANEL
// ─────────────────────────────────────────
class SchemeFinderPanel extends JPanel {

    private JTextField  txtSearch;
    private JComboBox<String> cmbType, cmbSort;
    private JList<String> lstCategory;
    private JTable tblResults;
    private DefaultTableModel tblModel;
    private JTextArea  txtDetails;
    private JLabel     lblCount;
    private JButton    btnFind, btnReset, btnApply, btnLink;
    private SchemeService service;
    private MainFrame  mainFrame;
    private List<Scheme> currentSchemes = new ArrayList<>();
    private User currentUser;

    public SchemeFinderPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        buildUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        loadSchemes(service.getAllSchemes());
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212, 208, 200));

        JLabel title = new JLabel("  Scheme Finder", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(Color.WHITE); title.setOpaque(true);
        title.setBackground(new Color(0,0,128));
        title.setPreferredSize(new Dimension(0, 28));
        add(title, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, buildFilterPanel(), buildResultsPanel());
        split.setDividerLocation(240);
        split.setDividerSize(4);
        add(split, BorderLayout.CENTER);
        loadSchemes(service.getAllSchemes());
    }

    private JPanel buildFilterPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(128,128,128)), "Search Filters",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 11)));
        p.setPreferredSize(new Dimension(240, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(5,6,4,6); g.fill=GridBagConstraints.HORIZONTAL; g.gridx=0;

        g.gridy=0; p.add(new JLabel("Keyword Search:"), g);
        g.gridy=1;
        JPanel searchRow = new JPanel(new BorderLayout(4,0));
        searchRow.setBackground(new Color(212,208,200));
        txtSearch = new JTextField();
        btnFind   = new JButton("Go");
        btnFind.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnFind.setPreferredSize(new Dimension(38,22));
        searchRow.add(txtSearch, BorderLayout.CENTER);
        searchRow.add(btnFind, BorderLayout.EAST);
        p.add(searchRow, g);

        g.gridy=2; p.add(new JLabel("Scheme Type:"), g);
        g.gridy=3; cmbType = new JComboBox<>(new String[]{"All Schemes","Central Govt","State Govt"});
        p.add(cmbType, g);

        g.gridy=4; p.add(new JLabel("Category:"), g);
        g.gridy=5; g.weighty=1; g.fill=GridBagConstraints.BOTH;
        String[] cats = {"All Categories","Health","Agriculture","Housing","Education","Business","Savings"};
        lstCategory = new JList<>(cats);
        lstCategory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        lstCategory.setSelectedIndex(0);
        lstCategory.setFont(new Font("SansSerif", Font.PLAIN, 12));
        JScrollPane catScroll = new JScrollPane(lstCategory);
        catScroll.setPreferredSize(new Dimension(0, 120));
        p.add(catScroll, g);

        g.gridy=6; g.weighty=0; g.fill=GridBagConstraints.HORIZONTAL;
        btnFind.addActionListener(e -> doSearch());
        p.add(btnFind = new JButton("Find Schemes"), g);
        btnFind.setBackground(new Color(0,0,128)); btnFind.setForeground(Color.WHITE); btnFind.setOpaque(true);
        btnFind.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnFind.addActionListener(e -> doSearch());

        g.gridy=7;
        btnReset = new JButton("Reset Filters");
        btnReset.setFont(new Font("SansSerif", Font.BOLD, 12));
        p.add(btnReset, g);
        btnReset.addActionListener(e -> { txtSearch.setText(""); lstCategory.setSelectedIndex(0); loadSchemes(service.getAllSchemes()); });

        return p;
    }

    private JPanel buildResultsPanel() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setBackground(new Color(212, 208, 200));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Top bar
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(212, 208, 200));
        lblCount = new JLabel("Results: 0 schemes found");
        lblCount.setFont(new Font("SansSerif", Font.BOLD, 12));
        JPanel sortPan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        sortPan.setBackground(new Color(212,208,200));
        sortPan.add(new JLabel("Sort by:"));
        cmbSort = new JComboBox<>(new String[]{"Match %","Name","Category","Benefit"});
        sortPan.add(cmbSort);
        top.add(lblCount, BorderLayout.WEST);
        top.add(sortPan, BorderLayout.EAST);
        p.add(top, BorderLayout.NORTH);

        // Table
        String[] cols = {"#","Scheme Name","Ministry","Category","Benefit","Match","Deadline"};
        tblModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblResults = new JTable(tblModel);
        tblResults.setSelectionBackground(new Color(0,0,128));
        tblResults.setSelectionForeground(Color.WHITE);
        tblResults.setRowHeight(22);
        tblResults.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tblResults.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tblResults.getTableHeader().setBackground(new Color(212,208,200));
        tblResults.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tblResults.getColumnModel().getColumn(0).setPreferredWidth(28);
        tblResults.getColumnModel().getColumn(1).setPreferredWidth(200);
        tblResults.getColumnModel().getColumn(5).setPreferredWidth(55);
        JScrollPane sp = new JScrollPane(tblResults);
        sp.setPreferredSize(new Dimension(0, 200));
        p.add(sp, BorderLayout.CENTER);

        // Detail panel
        JPanel detail = new JPanel(new BorderLayout(8, 0));
        detail.setBackground(new Color(212,208,200));
        detail.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(128,128,128)), "Scheme Details",
            TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif", Font.BOLD, 11)));

        txtDetails = new JTextArea(5, 30);
        txtDetails.setEditable(false);
        txtDetails.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtDetails.setLineWrap(true); txtDetails.setWrapStyleWord(true);
        txtDetails.setText("Select a scheme from the table to view details.");
        JScrollPane dsp = new JScrollPane(txtDetails);
        detail.add(dsp, BorderLayout.CENTER);

        JPanel dBtns = new JPanel(new GridLayout(3, 1, 0, 5));
        dBtns.setBackground(new Color(212,208,200));
        btnApply = new JButton("Apply Now");
        btnLink  = new JButton("Official Website");
        JButton btnSave = new JButton("Save Scheme");
        btnApply.setBackground(new Color(0,0,128)); btnApply.setForeground(Color.WHITE); btnApply.setOpaque(true);
        btnApply.setFont(new Font("SansSerif", Font.BOLD, 12));
        dBtns.add(btnApply); dBtns.add(btnLink); dBtns.add(btnSave);
        detail.add(dBtns, BorderLayout.EAST);
        p.add(detail, BorderLayout.SOUTH);

        // Row selection → show details
        tblResults.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showDetails();
        });

        btnApply.addActionListener(e -> doApply());
        btnLink.addActionListener(e -> {
            int row = tblResults.getSelectedRow();
            if (row >= 0 && row < currentSchemes.size()) {
                JOptionPane.showMessageDialog(this,
                    "Official Link:\n" + currentSchemes.get(row).getApplicationLink(),
                    "Official Website", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        return p;
    }

    private void loadSchemes(List<Scheme> schemes) {
        currentSchemes = schemes;
        tblModel.setRowCount(0);
        int i = 1;
        for (Scheme s : schemes) {
            tblModel.addRow(new Object[]{i++, s.getSchemeName(), s.getMinistry(), s.getCategory(), s.getBenefit(), s.getMatchPercent()+"%", s.getDeadline()});
        }
        lblCount.setText("Results: " + schemes.size() + " schemes found");
    }

    private void doSearch() {
        String kw   = txtSearch.getText().trim();
        String cat  = lstCategory.getSelectedValue();
        String type = (String) cmbType.getSelectedItem();
        List<Scheme> all = kw.isEmpty() ? service.getAllSchemes() : service.searchSchemes(kw);
        List<Scheme> filtered = new ArrayList<>();
        for (Scheme s : all) {
            boolean catOk  = cat == null || cat.equals("All Categories") || s.getCategory().equalsIgnoreCase(cat);
            boolean typeOk = type.equals("All Schemes") || s.getType().equalsIgnoreCase(type.replace(" Govt",""));
            if (catOk && typeOk) filtered.add(s);
        }
        loadSchemes(filtered);
    }

    private void showDetails() {
        int row = tblResults.getSelectedRow();
        if (row < 0 || row >= currentSchemes.size()) return;
        Scheme s = currentSchemes.get(row);
        StringBuilder sb = new StringBuilder();
        sb.append("Name:        ").append(s.getSchemeName()).append("\n");
        sb.append("Ministry:    ").append(s.getMinistry()).append("\n");
        sb.append("Type:        ").append(s.getType()).append(" Government\n");
        sb.append("Category:    ").append(s.getCategory()).append("\n");
        sb.append("Benefit:     ").append(s.getBenefit()).append("\n");
        sb.append("Deadline:    ").append(s.getDeadline()).append("\n\n");
        sb.append("Description:\n").append(s.getDescription()).append("\n\n");
        sb.append("Eligibility Criteria:\n");
        for (String e : s.getEligibilityCriteria()) sb.append("  - ").append(e).append("\n");
        sb.append("\nRequired Documents:\n");
        for (String d : s.getRequiredDocs()) sb.append("  * ").append(d).append("\n");
        txtDetails.setText(sb.toString());
        txtDetails.setCaretPosition(0);
    }

    private void doApply() {
        int row = tblResults.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a scheme first.", "Info", JOptionPane.INFORMATION_MESSAGE); return; }
        Scheme s = currentSchemes.get(row);
        User user = currentUser != null ? currentUser : new User(1,"Demo",35,"Male","Tamil Nadu","Farmer",120000,"OBC",true,false,"9876543210");
        boolean ok = service.applyForScheme(user, s);
        if (ok) {
            JOptionPane.showMessageDialog(this, "Application submitted for:\n" + s.getSchemeName() + "\n\nRequired Documents:\n" + String.join(", ", s.getRequiredDocs()), "Applied Successfully!", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "You have already applied for this scheme.", "Already Applied", JOptionPane.WARNING_MESSAGE);
        }
    }
}

// ─────────────────────────────────────────
// APPLICATION PANEL
// ─────────────────────────────────────────
class ApplicationPanel extends JPanel {

    private JTable tbl;
    private DefaultTableModel model;
    private JTextArea txtStatus;
    private SchemeService service;
    private MainFrame mainFrame;
    private User currentUser;

    public ApplicationPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        buildUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        refreshTable();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212,208,200));

        JLabel title = new JLabel("  My Applications", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(Color.WHITE); title.setOpaque(true);
        title.setBackground(new Color(0,0,128));
        title.setPreferredSize(new Dimension(0, 28));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(0, 8));
        content.setBackground(new Color(212,208,200));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        String[] cols = {"App ID","Scheme Name","Status","Applied Date","Remarks"};
        model = new DefaultTableModel(cols, 0) { public boolean isCellEditable(int r,int c){return false;} };
        tbl = new JTable(model);
        tbl.setRowHeight(22);
        tbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        tbl.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        tbl.getTableHeader().setBackground(new Color(212,208,200));
        tbl.setSelectionBackground(new Color(0,0,128));
        tbl.setSelectionForeground(Color.WHITE);

        // Color rows by status
        tbl.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t,v,sel,foc,r,c);
                if (!sel) {
                    String status = (String) t.getModel().getValueAt(r, 2);
                    if ("Approved".equals(status))     comp.setBackground(new Color(230,245,230));
                    else if ("Pending".equals(status)) comp.setBackground(new Color(255,253,220));
                    else if ("Under Review".equals(status)) comp.setBackground(new Color(230,235,255));
                    else if ("Rejected".equals(status)) comp.setBackground(new Color(255,230,230));
                    else comp.setBackground(Color.WHITE);
                }
                return comp;
            }
        });

        JScrollPane sp = new JScrollPane(tbl);
        content.add(sp, BorderLayout.CENTER);

        // Detail area
        JPanel bottom = new JPanel(new BorderLayout(8, 0));
        bottom.setBackground(new Color(212,208,200));
        bottom.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(128,128,128)),
            "Application Status Details", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif",Font.BOLD,11)));
        txtStatus = new JTextArea(4, 40);
        txtStatus.setEditable(false);
        txtStatus.setFont(new Font("Monospaced", Font.PLAIN, 11));
        txtStatus.setText("Select an application above to see details.");
        bottom.add(new JScrollPane(txtStatus), BorderLayout.CENTER);

        JPanel btnPan = new JPanel(new GridLayout(2,1,0,6));
        btnPan.setBackground(new Color(212,208,200));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnUpload  = new JButton("Upload Docs");
        btnRefresh.setFont(new Font("SansSerif",Font.BOLD,11));
        btnUpload.setFont(new Font("SansSerif",Font.BOLD,11));
        btnPan.add(btnRefresh); btnPan.add(btnUpload);
        bottom.add(btnPan, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);
        add(content, BorderLayout.CENTER);

        tbl.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = tbl.getSelectedRow();
                if (row >= 0) {
                    String remarks = (String) model.getValueAt(row, 4);
                    String status  = (String) model.getValueAt(row, 2);
                    txtStatus.setText("Application: " + model.getValueAt(row,1) + "\nStatus: " + status + "\nRemarks: " + remarks);
                }
            }
        });
        btnRefresh.addActionListener(e -> refreshTable());
        btnUpload.addActionListener(e -> mainFrame.showPanel("upload"));
        refreshTable();
    }

    public void refreshTable() {
        model.setRowCount(0);
        int uid = currentUser != null ? currentUser.getUserId() : 1;
        List<Application> apps = service.getApplicationsByUser(uid);
        for (Application a : apps) {
            model.addRow(new Object[]{a.getApplicationId(), a.getSchemeName(), a.getStatus(), a.getAppliedDate(), a.getRemarks()});
        }
    }
}

// ─────────────────────────────────────────
// DOCUMENT UPLOAD PANEL
// ─────────────────────────────────────────
class DocumentUploadPanel extends JPanel {

    private JComboBox<String> cmbScheme;
    private JTextField txtName;
    private JTextField[] txtFiles = new JTextField[4];
    private JProgressBar progressBar;
    private JTextArea txtLog;
    private JLabel lblStatus;
    private JButton btnVerify;
    private SchemeService service;
    private MainFrame mainFrame;
    private List<Document> uploadedDocs = new ArrayList<>();

    public DocumentUploadPanel(MainFrame mainFrame, SchemeService service) {
        this.mainFrame = mainFrame;
        this.service   = service;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212,208,200));

        JLabel title = new JLabel("  Upload Documents — AI Verification", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));
        title.setForeground(Color.WHITE); title.setOpaque(true);
        title.setBackground(new Color(0,0,128));
        title.setPreferredSize(new Dimension(0, 28));
        add(title, BorderLayout.NORTH);

        JPanel content = new JPanel(new GridLayout(1, 2, 12, 0));
        content.setBackground(new Color(212,208,200));
        content.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        content.add(buildLeftPanel());
        content.add(buildRightPanel());
        add(content, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        bottom.setBackground(new Color(212,208,200));
        bottom.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(128,128,128)));
        btnVerify = new JButton("  AI Verify & Submit Application  ");
        btnVerify.setFont(new Font("SansSerif",Font.BOLD,13));
        btnVerify.setBackground(new Color(0,100,0));
        btnVerify.setForeground(Color.WHITE); btnVerify.setOpaque(true);
        JButton btnClear = new JButton("Clear All");
        JButton btnCancel = new JButton("Cancel");
        bottom.add(btnVerify); bottom.add(btnClear); bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        btnVerify.addActionListener(e -> doVerify());
        btnClear.addActionListener(e -> doClear());
        btnCancel.addActionListener(e -> mainFrame.showPanel("dashboard"));
    }

    private JPanel buildLeftPanel() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(new Color(212,208,200));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(128,128,128)),
            "Application & Document Upload", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif",Font.BOLD,11)));

        GridBagConstraints g = new GridBagConstraints();
        g.insets=new Insets(5,6,4,6); g.fill=GridBagConstraints.HORIZONTAL; g.gridx=0;

        g.gridy=0; p.add(new JLabel("Select Scheme:"), g);
        g.gridy=1; cmbScheme = new JComboBox<>(new String[]{
            "Ayushman Bharat PM-JAY","PM Kisan Samman Nidhi","PM Awas Yojana","National Scholarship Portal","PM Mudra Yojana"});
        p.add(cmbScheme, g);

        g.gridy=2; p.add(new JLabel("Applicant Name:"), g);
        g.gridy=3; txtName = new JTextField("Ravi Kumar"); p.add(txtName, g);

        g.gridy=4; p.add(new JLabel("Required Documents:"), g);
        JPanel reqPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        reqPanel.setBackground(new Color(212,208,200));
        String[] reqDocs = {"Aadhaar Card","Ration Card","Income Certificate","BPL Card"};
        for (String d : reqDocs) {
            JLabel chip = new JLabel("  " + d + "  ");
            chip.setFont(new Font("SansSerif",Font.BOLD,10));
            chip.setOpaque(true); chip.setBackground(new Color(255,255,180));
            chip.setBorder(BorderFactory.createLineBorder(new Color(180,180,0)));
            reqPanel.add(chip);
        }
        g.gridy=5; p.add(reqPanel, g);

        g.gridy=6; p.add(new JSeparator(), g);
        g.gridy=7; p.add(new JLabel("Upload Files (Browse one by one):"), g);

        String[] docLabels = {"Aadhaar Card:","Income Certificate:","Ration Card:","Bank Passbook:"};
        for (int i = 0; i < 4; i++) {
            g.gridy = 8 + (i*2);
            p.add(new JLabel(docLabels[i]), g);
            g.gridy = 9 + (i*2);
            JPanel row = new JPanel(new BorderLayout(4,0));
            row.setBackground(new Color(212,208,200));
            txtFiles[i] = new JTextField();
            txtFiles[i].setFont(new Font("Monospaced",Font.PLAIN,11));
            JButton browse = new JButton("Browse...");
            browse.setFont(new Font("SansSerif",Font.PLAIN,11));
            final int idx = i;
            browse.addActionListener(e -> browseFile(idx));
            row.add(txtFiles[i], BorderLayout.CENTER);
            row.add(browse, BorderLayout.EAST);
            p.add(row, g);
        }

        g.gridy=17; p.add(new JLabel("Verification Progress:"), g);
        g.gridy=18;
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        progressBar.setString("Ready");
        progressBar.setFont(new Font("SansSerif",Font.BOLD,10));
        p.add(progressBar, g);

        return p;
    }

    private JPanel buildRightPanel() {
        JPanel p = new JPanel(new BorderLayout(0,8));
        p.setBackground(new Color(212,208,200));
        p.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(128,128,128)),
            "AI Verification Log", TitledBorder.LEFT, TitledBorder.TOP, new Font("SansSerif",Font.BOLD,11)));

        txtLog = new JTextArea();
        txtLog.setEditable(false);
        txtLog.setFont(new Font("Monospaced",Font.PLAIN,11));
        txtLog.setBackground(new Color(20,20,20));
        txtLog.setForeground(new Color(0,230,0));
        txtLog.setText("[SYSTEM] YojanaConnect AI Verifier Ready\n[INFO]   Upload documents and click Verify\n");
        p.add(new JScrollPane(txtLog), BorderLayout.CENTER);

        lblStatus = new JLabel("Status: Waiting for documents...");
        lblStatus.setFont(new Font("SansSerif",Font.BOLD,11));
        lblStatus.setForeground(new Color(0,0,128));
        lblStatus.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
        p.add(lblStatus, BorderLayout.SOUTH);
        return p;
    }

    private void browseFile(int idx) {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Select Document");
        fc.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Documents (PDF, JPG, PNG)","pdf","jpg","jpeg","png"));
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            txtFiles[idx].setText(f.getAbsolutePath());
            appendLog("[OK]    File selected: " + f.getName());
        }
    }

    private void doVerify() {
        boolean anyFile = false;
        for (JTextField tf : txtFiles) {
            if (!tf.getText().trim().isEmpty()) { anyFile = true; break; }
        }
        if (!anyFile) {
            JOptionPane.showMessageDialog(this, "Please browse and select at least one document file.", "No Files", JOptionPane.WARNING_MESSAGE);
            return;
        }

        btnVerify.setEnabled(false);
        progressBar.setValue(0);
        progressBar.setString("Starting...");
        appendLog("\n[INFO]  Starting AI verification...");

        // Simulate progress with timer
        Timer timer = new Timer(200, null);
        int[] step = {0};
        String[] steps = {
            "[SCAN]  Reading document metadata...",
            "[OCR]   Extracting text from documents...",
            "[AI]    Verifying Aadhaar authenticity...",
            "[AI]    Verifying Income Certificate...",
            "[CHECK] Matching against eligibility criteria...",
            "[VALID] All documents verified successfully!",
            "[SUBMIT] Submitting application to government portal...",
            "[DONE]  Application submitted! ID: APP" + (int)(Math.random()*9000+1000)
        };
        timer.addActionListener(e -> {
            if (step[0] < steps.length) {
                int pct = (step[0]+1)*100/steps.length;
                progressBar.setValue(pct);
                progressBar.setString(pct + "%");
                appendLog(steps[step[0]]);
                step[0]++;
            } else {
                ((Timer)e.getSource()).stop();
                progressBar.setValue(100);
                progressBar.setString("Complete!");
                lblStatus.setText("Status: VERIFIED & SUBMITTED ✓");
                lblStatus.setForeground(new Color(0,128,0));
                btnVerify.setEnabled(true);
                JOptionPane.showMessageDialog(this,
                    "Documents verified successfully!\nApplication submitted to government portal.\nYou will receive a confirmation SMS within 24 hours.",
                    "Verification Complete", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        timer.start();
    }

    private void doClear() {
        for (JTextField tf : txtFiles) tf.setText("");
        progressBar.setValue(0); progressBar.setString("Ready");
        txtLog.setText("[SYSTEM] YojanaConnect AI Verifier Ready\n[INFO]   Upload documents and click Verify\n");
        lblStatus.setText("Status: Waiting for documents...");
        lblStatus.setForeground(new Color(0,0,128));
    }

    private void appendLog(String msg) {
        txtLog.append(msg + "\n");
        txtLog.setCaretPosition(txtLog.getDocument().getLength());
    }
}

// ─────────────────────────────────────────
// CHATBOT PANEL
// ─────────────────────────────────────────
class ChatbotPanel extends JPanel {

    private JTextPane chatArea;
    private JTextField txtInput;
    private JComboBox<String> cmbLang;
    private JLabel lblVoiceStatus;
    private SchemeService service;

    public ChatbotPanel(MainFrame mainFrame, SchemeService service) {
        this.service = service;
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212,208,200));

        // Header
        JPanel head = new JPanel(new BorderLayout());
        head.setBackground(new Color(0,0,128));
        JLabel title = new JLabel("  YojanaBot — AI Scheme Assistant");
        title.setFont(new Font("SansSerif",Font.BOLD,13));
        title.setForeground(Color.WHITE);
        JPanel langPan = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        langPan.setBackground(new Color(0,0,128));
        langPan.add(new JLabel("<html><font color='white'>Voice Language:</font></html>"));
        cmbLang = new JComboBox<>(new String[]{"English (en-IN)","Hindi / हिंदी (hi-IN)","Tamil / தமிழ் (ta-IN)"});
        cmbLang.setFont(new Font("SansSerif",Font.PLAIN,11));
        langPan.add(cmbLang);
        head.add(title, BorderLayout.WEST);
        head.add(langPan, BorderLayout.EAST);
        head.setPreferredSize(new Dimension(0, 32));
        add(head, BorderLayout.NORTH);

        // Chat area
        chatArea = new JTextPane();
        chatArea.setEditable(false);
        chatArea.setFont(new Font("SansSerif",Font.PLAIN,12));
        chatArea.setBackground(new Color(245,245,245));
        JScrollPane sp = new JScrollPane(chatArea);
        sp.setBorder(BorderFactory.createLineBorder(new Color(128,128,128)));
        add(sp, BorderLayout.CENTER);

        // Add welcome message
        appendBot("Namaste! I am YojanaBot.\n\nAsk me anything about government schemes — eligibility, benefits, required documents, deadlines, or fraud alerts!\n\nYou can type OR use Voice Input in English, Hindi, or Tamil.");
        appendBot("Quick tips — try asking:\n  • \"Am I eligible for Ayushman Bharat?\"\n  • \"PM Kisan documents needed\"\n  • \"Fake scheme warning\"\n  • \"Upcoming deadlines\"");

        // Bottom input area
        JPanel inputArea = new JPanel(new BorderLayout(0, 5));
        inputArea.setBackground(new Color(212,208,200));
        inputArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1,0,0,0,new Color(128,128,128)),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));

        // Voice button
        JPanel voiceRow = new JPanel(new BorderLayout(6,0));
        voiceRow.setBackground(new Color(212,208,200));
        JButton btnVoice = new JButton("  Click to Speak  (Voice Input — EN / हिंदी / தமிழ்)");
        btnVoice.setFont(new Font("SansSerif",Font.BOLD,12));
        btnVoice.setBackground(new Color(30,80,160));
        btnVoice.setForeground(Color.WHITE); btnVoice.setOpaque(true);
        lblVoiceStatus = new JLabel("Voice: Ready");
        lblVoiceStatus.setFont(new Font("SansSerif",Font.PLAIN,11));
        lblVoiceStatus.setForeground(new Color(0,128,0));
        voiceRow.add(btnVoice, BorderLayout.CENTER);
        voiceRow.add(lblVoiceStatus, BorderLayout.EAST);
        inputArea.add(voiceRow, BorderLayout.NORTH);

        // Text input row
        JPanel textRow = new JPanel(new BorderLayout(6,0));
        textRow.setBackground(new Color(212,208,200));
        txtInput = new JTextField();
        txtInput.setFont(new Font("SansSerif",Font.PLAIN,13));
        txtInput.setToolTipText("Type your question here or press Enter to send");
        JButton btnSend  = new JButton("Send");
        JButton btnClear = new JButton("Clear");
        btnSend.setFont(new Font("SansSerif",Font.BOLD,12));
        btnSend.setBackground(new Color(0,0,128));
        btnSend.setForeground(Color.WHITE); btnSend.setOpaque(true);
        btnSend.setPreferredSize(new Dimension(70,28));
        btnClear.setFont(new Font("SansSerif",Font.BOLD,12));
        btnClear.setPreferredSize(new Dimension(65,28));
        JPanel btnPan = new JPanel(new FlowLayout(FlowLayout.LEFT,4,0));
        btnPan.setBackground(new Color(212,208,200));
        btnPan.add(btnSend); btnPan.add(btnClear);
        textRow.add(txtInput, BorderLayout.CENTER);
        textRow.add(btnPan, BorderLayout.EAST);
        inputArea.add(textRow, BorderLayout.CENTER);
        add(inputArea, BorderLayout.SOUTH);

        btnSend.addActionListener(e -> sendMessage());
        txtInput.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) { if (e.getKeyCode()==KeyEvent.VK_ENTER) sendMessage(); }
        });
        btnClear.addActionListener(e -> { chatArea.setText(""); appendBot("Chat cleared. Ask me anything!"); });
        btnVoice.addActionListener(e -> {
            lblVoiceStatus.setText("Voice: Listening...");
            lblVoiceStatus.setForeground(new Color(180,0,0));
            // Simulate voice (real impl needs javax.speech or web API)
            Timer t = new Timer(1500, ev -> {
                lblVoiceStatus.setText("Voice: Ready");
                lblVoiceStatus.setForeground(new Color(0,128,0));
                txtInput.setText("What schemes are available for farmers?");
                sendMessage();
            });
            t.setRepeats(false); t.start();
        });
    }

    private void sendMessage() {
        String msg = txtInput.getText().trim();
        if (msg.isEmpty()) return;
        appendUser(msg);
        txtInput.setText("");

        // Typing indicator
        appendBot("...");

        Timer t = new Timer(500, e -> {
            try {
                // Remove "..."
                javax.swing.text.Document doc = chatArea.getDocument();
                String text = doc.getText(0, doc.getLength());
                int idx = text.lastIndexOf("...");
                if (idx >= 0) doc.remove(idx, 3);
            } catch (Exception ex) {}
            String reply = service.getChatbotResponse(msg);
            appendBot(reply);
        });
        t.setRepeats(false); t.start();
    }

    private void appendUser(String msg) {
        try {
            javax.swing.text.StyledDocument doc = chatArea.getStyledDocument();
            javax.swing.text.Style userStyle = chatArea.addStyle("user", null);
            javax.swing.text.StyleConstants.setBackground(userStyle, new Color(220,235,255));
            javax.swing.text.StyleConstants.setForeground(userStyle, new Color(0,0,100));
            javax.swing.text.StyleConstants.setBold(userStyle, true);
            doc.insertString(doc.getLength(), "\nYou: " + msg + "\n", userStyle);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {}
    }

    private void appendBot(String msg) {
        try {
            javax.swing.text.StyledDocument doc = chatArea.getStyledDocument();
            javax.swing.text.Style botStyle = chatArea.addStyle("bot", null);
            javax.swing.text.StyleConstants.setForeground(botStyle, new Color(0, 80, 0));
            javax.swing.text.StyleConstants.setBackground(botStyle, new Color(240,255,240));
            doc.insertString(doc.getLength(), "\nYojanaBot: " + msg + "\n", botStyle);
            chatArea.setCaretPosition(doc.getLength());
        } catch (Exception e) {}
    }
}

// ─────────────────────────────────────────
// ALERTS PANEL
// ─────────────────────────────────────────
class AlertsPanel extends JPanel {

    private JPanel alertList;
    private SchemeService service;
    private User currentUser;

    public AlertsPanel(MainFrame mainFrame, SchemeService service) {
        this.service = service;
        buildUI();
    }

    public void setUser(User user) {
        this.currentUser = user;
        refreshAlerts();
    }

    private void buildUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(212,208,200));

        JLabel title = new JLabel("  Alerts & Notifications", SwingConstants.LEFT);
        title.setFont(new Font("SansSerif",Font.BOLD,13));
        title.setForeground(Color.WHITE); title.setOpaque(true);
        title.setBackground(new Color(0,0,128));
        title.setPreferredSize(new Dimension(0,28));
        add(title, BorderLayout.NORTH);

        alertList = new JPanel();
        alertList.setLayout(new BoxLayout(alertList, BoxLayout.Y_AXIS));
        alertList.setBackground(new Color(212,208,200));

        JScrollPane sp = new JScrollPane(alertList);
        sp.setBorder(BorderFactory.createEmptyBorder(10,12,10,12));
        sp.setBackground(new Color(212,208,200));
        add(sp, BorderLayout.CENTER);

        JPanel btnBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        btnBar.setBackground(new Color(212,208,200));
        btnBar.setBorder(BorderFactory.createMatteBorder(1,0,0,0,new Color(128,128,128)));
        JButton btnMarkAll = new JButton("Mark All Read");
        JButton btnClear   = new JButton("Clear All");
        btnBar.add(btnMarkAll); btnBar.add(btnClear);
        add(btnBar, BorderLayout.SOUTH);

        btnMarkAll.addActionListener(e -> JOptionPane.showMessageDialog(this, "All alerts marked as read.", "Done", JOptionPane.INFORMATION_MESSAGE));
        btnClear.addActionListener(e -> { alertList.removeAll(); alertList.revalidate(); alertList.repaint(); });

        refreshAlerts();
    }

    public void refreshAlerts() {
        alertList.removeAll();
        int uid = currentUser != null ? currentUser.getUserId() : 1;
        List<Notification> notifs = service.getNotifications(uid);
        for (Notification n : notifs) {
            alertList.add(makeAlertCard(n));
            alertList.add(Box.createVerticalStrut(8));
        }
        alertList.revalidate(); alertList.repaint();
    }

    private JPanel makeAlertCard(Notification n) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(getAlertBorderColor(n.getType())),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        card.setBackground(getAlertBgColor(n.getType()));

        JLabel icon = new JLabel(getAlertIcon(n.getType()));
        icon.setFont(new Font("SansSerif",Font.PLAIN,22));
        card.add(icon, BorderLayout.WEST);

        JPanel info = new JPanel(new BorderLayout(0,3));
        info.setBackground(getAlertBgColor(n.getType()));
        JLabel msg = new JLabel("<html><b>" + n.getMessage() + "</b></html>");
        msg.setFont(new Font("SansSerif",Font.PLAIN,12));
        msg.setForeground(getAlertTextColor(n.getType()));
        JLabel date = new JLabel(n.getSentDate() + "  ·  " + (n.isRead() ? "Read" : "Unread"));
        date.setFont(new Font("SansSerif",Font.PLAIN,10));
        date.setForeground(Color.GRAY);
        info.add(msg, BorderLayout.CENTER);
        info.add(date, BorderLayout.SOUTH);
        card.add(info, BorderLayout.CENTER);
        return card;
    }

    private Color getAlertBgColor(String type) {
        switch(type) {
            case "FRAUD":    return new Color(255,230,230);
            case "DEADLINE": return new Color(255,253,220);
            case "APPROVED": return new Color(230,245,230);
            default:         return new Color(230,235,255);
        }
    }
    private Color getAlertBorderColor(String type) {
        switch(type) {
            case "FRAUD":    return new Color(200,0,0);
            case "DEADLINE": return new Color(200,140,0);
            case "APPROVED": return new Color(0,150,0);
            default:         return new Color(50,70,200);
        }
    }
    private Color getAlertTextColor(String type) {
        switch(type) {
            case "FRAUD":    return new Color(180,0,0);
            case "DEADLINE": return new Color(160,100,0);
            case "APPROVED": return new Color(0,120,0);
            default:         return new Color(30,50,180);
        }
    }
    private String getAlertIcon(String type) {
        switch(type) {
            case "FRAUD":    return "⚠";
            case "DEADLINE": return "⏰";
            case "APPROVED": return "✓";
            default:         return "★";
        }
    }
}
