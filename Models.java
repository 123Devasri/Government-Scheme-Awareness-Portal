import java.util.Date;
import java.util.List;
import java.util.ArrayList;

// ─────────────────────────────────────────
// User Model
// ─────────────────────────────────────────
class User {
    private int userId;
    private String name;
    private int age;
    private String gender;
    private String state;
    private String occupation;
    private double annualIncome;
    private String category;
    private boolean bplCardHolder;
    private boolean pwdStatus;
    private String mobileNumber;

    public User() {}

    public User(int userId, String name, int age, String gender,
                String state, String occupation, double annualIncome,
                String category, boolean bplCardHolder, boolean pwdStatus, String mobileNumber) {
        this.userId       = userId;
        this.name         = name;
        this.age          = age;
        this.gender       = gender;
        this.state        = state;
        this.occupation   = occupation;
        this.annualIncome = annualIncome;
        this.category     = category;
        this.bplCardHolder = bplCardHolder;
        this.pwdStatus    = pwdStatus;
        this.mobileNumber = mobileNumber;
    }

    // Getters
    public int    getUserId()       { return userId; }
    public String getName()         { return name; }
    public int    getAge()          { return age; }
    public String getGender()       { return gender; }
    public String getState()        { return state; }
    public String getOccupation()   { return occupation; }
    public double getAnnualIncome() { return annualIncome; }
    public String getCategory()     { return category; }
    public boolean isBplCardHolder(){ return bplCardHolder; }
    public boolean isPwdStatus()    { return pwdStatus; }
    public String getMobileNumber() { return mobileNumber; }

    // Setters
    public void setUserId(int userId)             { this.userId = userId; }
    public void setName(String name)              { this.name = name; }
    public void setAge(int age)                   { this.age = age; }
    public void setGender(String gender)          { this.gender = gender; }
    public void setState(String state)            { this.state = state; }
    public void setOccupation(String occupation)  { this.occupation = occupation; }
    public void setAnnualIncome(double income)    { this.annualIncome = income; }
    public void setCategory(String category)      { this.category = category; }
    public void setBplCardHolder(boolean bpl)     { this.bplCardHolder = bpl; }
    public void setPwdStatus(boolean pwd)         { this.pwdStatus = pwd; }
    public void setMobileNumber(String mobile)    { this.mobileNumber = mobile; }

    public String getProfile() {
        return "User[" + name + ", Age:" + age + ", " + state + ", " + category + "]";
    }
}

// ─────────────────────────────────────────
// Scheme Model
// ─────────────────────────────────────────
class Scheme {
    private int    schemeId;
    private String schemeName;
    private String ministry;
    private String type;          // "Central" or "State"
    private String category;      // Health, Agriculture, Housing, etc.
    private String description;
    private String benefit;
    private String applicationLink;
    private String deadline;
    private boolean isVerified;
    private int     matchPercent;
    private String[] requiredDocs;
    private String[] eligibilityCriteria;

    public Scheme() {}

    public Scheme(int schemeId, String schemeName, String ministry, String type,
                  String category, String description, String benefit,
                  String applicationLink, String deadline, boolean isVerified,
                  int matchPercent, String[] requiredDocs, String[] eligibilityCriteria) {
        this.schemeId           = schemeId;
        this.schemeName         = schemeName;
        this.ministry           = ministry;
        this.type               = type;
        this.category           = category;
        this.description        = description;
        this.benefit            = benefit;
        this.applicationLink    = applicationLink;
        this.deadline           = deadline;
        this.isVerified         = isVerified;
        this.matchPercent       = matchPercent;
        this.requiredDocs       = requiredDocs;
        this.eligibilityCriteria = eligibilityCriteria;
    }

    // Getters
    public int      getSchemeId()        { return schemeId; }
    public String   getSchemeName()      { return schemeName; }
    public String   getMinistry()        { return ministry; }
    public String   getType()            { return type; }
    public String   getCategory()        { return category; }
    public String   getDescription()     { return description; }
    public String   getBenefit()         { return benefit; }
    public String   getApplicationLink() { return applicationLink; }
    public String   getDeadline()        { return deadline; }
    public boolean  isVerified()         { return isVerified; }
    public int      getMatchPercent()    { return matchPercent; }
    public String[] getRequiredDocs()    { return requiredDocs; }
    public String[] getEligibilityCriteria() { return eligibilityCriteria; }

    public String getDetails() {
        return schemeName + " | " + ministry + " | " + benefit;
    }

    public boolean isEligible(User user) {
        return matchPercent >= 70;
    }
}

// ─────────────────────────────────────────
// Application Model
// ─────────────────────────────────────────
class Application {
    private int    applicationId;
    private int    userId;
    private int    schemeId;
    private String schemeName;
    private String status;     // Pending, Under Review, Approved, Rejected
    private String appliedDate;
    private String remarks;

    public Application() {}

    public Application(int applicationId, int userId, int schemeId,
                       String schemeName, String status, String appliedDate, String remarks) {
        this.applicationId = applicationId;
        this.userId        = userId;
        this.schemeId      = schemeId;
        this.schemeName    = schemeName;
        this.status        = status;
        this.appliedDate   = appliedDate;
        this.remarks       = remarks;
    }

    // Getters
    public int    getApplicationId() { return applicationId; }
    public int    getUserId()        { return userId; }
    public int    getSchemeId()      { return schemeId; }
    public String getSchemeName()    { return schemeName; }
    public String getStatus()        { return status; }
    public String getAppliedDate()   { return appliedDate; }
    public String getRemarks()       { return remarks; }

    // Setters
    public void setStatus(String status)   { this.status = status; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public void updateStatus(String newStatus) { this.status = newStatus; }
}

// ─────────────────────────────────────────
// Document Model
// ─────────────────────────────────────────
class Document {
    private int    documentId;
    private int    userId;
    private int    applicationId;
    private String docType;
    private String filePath;
    private boolean isVerified;
    private String uploadedDate;

    public Document() {}

    public Document(int documentId, int userId, int applicationId,
                    String docType, String filePath, boolean isVerified, String uploadedDate) {
        this.documentId     = documentId;
        this.userId         = userId;
        this.applicationId  = applicationId;
        this.docType        = docType;
        this.filePath       = filePath;
        this.isVerified     = isVerified;
        this.uploadedDate   = uploadedDate;
    }

    // Getters
    public int     getDocumentId()    { return documentId; }
    public int     getUserId()        { return userId; }
    public int     getApplicationId() { return applicationId; }
    public String  getDocType()       { return docType; }
    public String  getFilePath()      { return filePath; }
    public boolean isVerified()       { return isVerified; }
    public String  getUploadedDate()  { return uploadedDate; }

    // Setters
    public void setVerified(boolean verified) { this.isVerified = verified; }
    public void setFilePath(String path)      { this.filePath = path; }

    public boolean verify() {
        this.isVerified = (filePath != null && !filePath.isEmpty());
        return this.isVerified;
    }

    public String getDocPath() { return filePath; }
}

// ─────────────────────────────────────────
// Notification Model
// ─────────────────────────────────────────
class Notification {
    private int    notifId;
    private int    userId;
    private String message;
    private String type;     // ALERT, DEADLINE, APPROVED, NEW_SCHEME, FRAUD
    private String sentDate;
    private boolean isRead;

    public Notification() {}

    public Notification(int notifId, int userId, String message,
                        String type, String sentDate, boolean isRead) {
        this.notifId  = notifId;
        this.userId   = userId;
        this.message  = message;
        this.type     = type;
        this.sentDate = sentDate;
        this.isRead   = isRead;
    }

    // Getters
    public int     getNotifId()  { return notifId; }
    public int     getUserId()   { return userId; }
    public String  getMessage()  { return message; }
    public String  getType()     { return type; }
    public String  getSentDate() { return sentDate; }
    public boolean isRead()      { return isRead; }

    // Setters
    public void setRead(boolean read) { this.isRead = read; }

    public void send()        { System.out.println("Notification sent: " + message); }
    public void markAsRead()  { this.isRead = true; }
}
