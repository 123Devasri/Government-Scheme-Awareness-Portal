import java.util.ArrayList;
import java.util.List;

public class SchemeService {

    // ── Dummy data (replace with DAO calls once DB is connected) ──
    private static List<Scheme> schemeDatabase = new ArrayList<>();
    private static List<Application> applicationDatabase = new ArrayList<>();
    private static List<Notification> notifDatabase = new ArrayList<>();
    private static int appIdCounter = 1001;

    static {
        // Seed schemes
        schemeDatabase.add(new Scheme(1, "Ayushman Bharat PM-JAY",
            "Ministry of Health & FW", "Central", "Health",
            "World's largest health insurance scheme providing Rs.5 lakh/year coverage per family for secondary and tertiary care hospitalization.",
            "Rs. 5 Lakh/year health cover",
            "https://pmjay.gov.in", "Ongoing", true, 98,
            new String[]{"Aadhaar Card", "Ration Card", "Income Certificate", "BPL Card"},
            new String[]{"Annual income below Rs.5 lakh", "BPL / SECC household", "No existing ESI/CGHS coverage"}));

        schemeDatabase.add(new Scheme(2, "PM Kisan Samman Nidhi",
            "Ministry of Agriculture", "Central", "Agriculture",
            "Direct income support of Rs.6,000/year in three equal installments to all land-holding farmer families across India.",
            "Rs. 6,000/year (3 installments)",
            "https://pmkisan.gov.in", "31 Mar 2026", true, 95,
            new String[]{"Aadhaar Card", "Land Records (Khasra)", "Bank Passbook"},
            new String[]{"Small or marginal farmer", "Owns cultivable land", "Aadhaar-linked bank account"}));

        schemeDatabase.add(new Scheme(3, "PM Awas Yojana (Gramin)",
            "Ministry of Rural Development", "Central", "Housing",
            "Financial assistance for construction of pucca houses for homeless and kutcha house owners in rural areas.",
            "Rs. 1.2 - 1.5 Lakh construction aid",
            "https://pmayg.nic.in", "Ongoing", true, 89,
            new String[]{"Aadhaar Card", "SECC Letter", "Bank Passbook", "Land Ownership Proof"},
            new String[]{"Rural household", "Homeless or kutcha house", "Included in SECC 2011"}));

        schemeDatabase.add(new Scheme(4, "National Scholarship Portal",
            "Ministry of Education", "Central", "Education",
            "Single-window electronic scholarship system for all government scholarships for SC/ST/OBC/Minority students.",
            "Rs. 1,000 - 20,000/month",
            "https://scholarships.gov.in", "15 Apr 2026", true, 82,
            new String[]{"Aadhaar Card", "Caste Certificate", "Income Certificate", "Marksheet", "Bank Passbook"},
            new String[]{"SC/ST/OBC/Minority student", "Family income below Rs.6 lakh", "Min. 50% in previous class"}));

        schemeDatabase.add(new Scheme(5, "PM Mudra Yojana",
            "Ministry of Finance", "Central", "Business",
            "Collateral-free micro-loans up to Rs.10 lakh for non-corporate, non-farm small/micro enterprises.",
            "Loan up to Rs. 10 Lakh",
            "https://mudra.org.in", "Ongoing", true, 78,
            new String[]{"Aadhaar Card", "PAN Card", "Business Plan", "Bank Statement (6 months)"},
            new String[]{"Non-farm micro enterprise", "Age 18-65 years", "No loan default history"}));

        schemeDatabase.add(new Scheme(6, "Sukanya Samriddhi Yojana",
            "Ministry of Finance", "Central", "Savings",
            "Government savings scheme for girl children offering 8.2% interest p.a. with full tax exemption under 80C.",
            "8.2% p.a. + tax-free maturity",
            "https://nsiindia.gov.in", "Ongoing", true, 72,
            new String[]{"Girl Child Birth Certificate", "Parent Aadhaar & PAN", "Address Proof"},
            new String[]{"Girl child below 10 years", "Max 2 girls per family", "Parent/Guardian to open account"}));

        // Seed notifications
        notifDatabase.add(new Notification(1, 1,
            "FRAUD ALERT: 'PM Free Laptop Yojana 2026' is NOT a real scheme. Do not share Aadhaar.",
            "FRAUD", "11 Mar 2026", false));
        notifDatabase.add(new Notification(2, 1,
            "DEADLINE: PM Kisan Samman Nidhi registration closes 31 March 2026.",
            "DEADLINE", "13 Mar 2026", false));
        notifDatabase.add(new Notification(3, 1,
            "APPROVED: Your Ayushman Bharat application has been approved!",
            "APPROVED", "10 Mar 2026", true));
        notifDatabase.add(new Notification(4, 1,
            "NEW SCHEME: Skill India Digital Free Certification 2026 - Applications open.",
            "NEW_SCHEME", "01 Mar 2026", true));

        // Seed existing applications
        applicationDatabase.add(new Application(101, 1, 1, "Ayushman Bharat PM-JAY",
            "Approved", "02 Mar 2026", "All documents verified. Health card will be delivered."));
        applicationDatabase.add(new Application(102, 1, 2, "PM Kisan Samman Nidhi",
            "Pending", "08 Mar 2026", "Land records verification in progress."));
        applicationDatabase.add(new Application(103, 1, 4, "National Scholarship Portal",
            "Under Review", "14 Mar 2026", "Documents submitted - under review."));
    }

    // ── Find matching schemes for a user ──
    public List<Scheme> findMatchingSchemes(User user) {
        List<Scheme> matched = new ArrayList<>();
        for (Scheme s : schemeDatabase) {
            if (s.isEligible(user)) {
                matched.add(s);
            }
        }
        return matched;
    }

    // ── Search schemes by keyword ──
    public List<Scheme> searchSchemes(String keyword) {
        List<Scheme> results = new ArrayList<>();
        String kw = keyword.toLowerCase();
        for (Scheme s : schemeDatabase) {
            if (s.getSchemeName().toLowerCase().contains(kw) ||
                s.getCategory().toLowerCase().contains(kw) ||
                s.getDescription().toLowerCase().contains(kw) ||
                s.getMinistry().toLowerCase().contains(kw)) {
                results.add(s);
            }
        }
        return results;
    }

    // ── Get all schemes ──
    public List<Scheme> getAllSchemes() {
        return new ArrayList<>(schemeDatabase);
    }

    // ── Get scheme by ID ──
    public Scheme getSchemeById(int id) {
        for (Scheme s : schemeDatabase) {
            if (s.getSchemeId() == id) return s;
        }
        return null;
    }

    // ── Apply for a scheme ──
    public boolean applyForScheme(User user, Scheme scheme) {
        // Check for duplicate application
        for (Application a : applicationDatabase) {
            if (a.getUserId() == user.getUserId() && a.getSchemeId() == scheme.getSchemeId()) {
                return false; // Already applied
            }
        }
        Application app = new Application(
            appIdCounter++, user.getUserId(), scheme.getSchemeId(),
            scheme.getSchemeName(), "Pending",
            new java.text.SimpleDateFormat("dd MMM yyyy").format(new java.util.Date()),
            "Application submitted. Documents pending verification."
        );
        applicationDatabase.add(app);
        return true;
    }

    // ── Get applications by user ──
    public List<Application> getApplicationsByUser(int userId) {
        List<Application> result = new ArrayList<>();
        for (Application a : applicationDatabase) {
            if (a.getUserId() == userId) result.add(a);
        }
        return result;
    }

    // ── Verify documents (simulated AI) ──
    public boolean verifyDocuments(List<Document> docs) {
        return !docs.isEmpty();
    }

    // ── Get notifications for user ──
    public List<Notification> getNotifications(int userId) {
        List<Notification> result = new ArrayList<>();
        for (Notification n : notifDatabase) {
            if (n.getUserId() == userId) result.add(n);
        }
        return result;
    }

    // ── Chatbot response ──
    public String getChatbotResponse(String input) {
        String m = input.toLowerCase();
        if (m.contains("ayushman") || m.contains("health") || m.contains("pmjay"))
            return "Ayushman Bharat PM-JAY gives Rs.5 lakh/year health cover for BPL families.\nDocuments: Aadhaar, Ration Card, Income Certificate.\nApply at: pmjay.gov.in";
        if (m.contains("kisan") || m.contains("farmer") || m.contains("krishi"))
            return "PM-KISAN gives Rs.6,000/year to farmers in 3 installments.\nDocuments: Aadhaar, Land Records, Bank Passbook.\nDeadline: 31 March 2026\nRegister at: pmkisan.gov.in";
        if (m.contains("awas") || m.contains("house") || m.contains("housing"))
            return "PM Awas Yojana gives Rs.1.5 lakh for rural house construction.\nApply through your Gram Panchayat.\nDocuments: Aadhaar, SECC Letter, Land Proof.";
        if (m.contains("mudra") || m.contains("loan") || m.contains("business"))
            return "PM Mudra Yojana gives collateral-free loans up to Rs.10 lakh.\nTiers: Shishu (50K), Kishore (5L), Tarun (10L).\nVisit any bank or mudra.org.in";
        if (m.contains("scholar") || m.contains("student") || m.contains("education"))
            return "National Scholarship Portal has 50+ scholarships for SC/ST/OBC students.\nApply at: scholarships.gov.in\nDeadline: 15 April 2026";
        if (m.contains("sukanya") || m.contains("girl") || m.contains("daughter"))
            return "Sukanya Samriddhi Yojana: 8.2% p.a. for girl children below 10.\nOpen at any post office or bank. Tax-free returns!";
        if (m.contains("document") || m.contains("docs") || m.contains("required"))
            return "Most schemes require:\n- Aadhaar Card\n- Income Certificate\n- Bank Passbook\n- Caste Certificate (if applicable)\n- Residence Proof";
        if (m.contains("fake") || m.contains("fraud") || m.contains("scam"))
            return "FRAUD ALERT: Always verify schemes at india.gov.in\nKnown fake schemes: 'PM Free Laptop Yojana'\nReport fakes at: cybercrime.gov.in";
        if (m.contains("deadline") || m.contains("last date"))
            return "Upcoming Deadlines:\n- PM Kisan: 31 March 2026\n- National Scholarship: 15 April 2026\nCheck Alerts tab for full list.";
        return "I can help with any government scheme!\nAsk about: Ayushman Bharat, PM-KISAN,\nMudra Loan, Scholarships, PM Awas Yojana,\nor type 'documents' for document info.";
    }

    // ── Validate login ──
    public User validateLogin(String userId, String password) {
        // Demo: accept any login, return demo user
        if (!userId.isEmpty() && !password.isEmpty()) {
            return new User(1, "Ravi Kumar", 35, "Male", "Tamil Nadu",
                "Farmer", 120000, "OBC", true, false, "9876543210");
        }
        return null;
    }

    // ── Register new user ──
    public boolean registerUser(User user) {
        return true; // In real app, save to DB via UserDAO
    }
}
