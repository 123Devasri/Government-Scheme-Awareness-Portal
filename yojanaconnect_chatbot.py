"""
╔═══════════════════════════════════════════════════════════════════════════════════╗
║                                                                                   ║
║              YojanaConnect AI  —  Government Scheme Awareness Chatbot v2.0       ║
║                     Intelligent Scheme Discovery & Assistance                    ║
║                                                                                   ║
║  SCHEME ROUTING LOGIC (replaces MindBridge PHQ-9 severity routing)               ║
║  ✅ Profile-Based Scheme Matching       ✅ Category-wise Routing                 ║
║  ✅ Eligibility Assessment Engine       ✅ Document Checklist Builder             ║
║  ✅ Multilingual (EN / HI / TA)         ✅ Fraud Alert Detection                 ║
║  ✅ Deadline Reminder System            ✅ Application Guidance                  ║
║  ✅ AI-Powered Response (Groq LLM)      ✅ Occupation-Based Routing              ║
║                                                                                   ║
╚═══════════════════════════════════════════════════════════════════════════════════╝

WHAT CHANGED FROM MINDBRIDGE:
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  MindBridge                          YojanaConnect
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
  EmotionType (sad/stress/crisis)  →  SchemeCategory (health/agri/housing)
  PHQ9Severity (minimal→severe)    →  OccupationType (farmer/student/business)
  GAD7Severity (anxiety score)     →  SchemeMatchLevel (perfect/good/partial)
  RiskLevel (low/critical)         →  QueryType (search/docs/fraud/deadline)
  ConversationStage (PHQ-9 flow)   →  ConversationStage (scheme discovery flow)
  PHQ-9 questions (depression)     →  SchemeProfileInput (user profile fields)
  GAD-7 questions (anxiety)        →  find-schemes endpoint (profile matching)
  HELPLINES (crisis numbers)       →  FRAUD_ALERTS (fake scheme warnings)
  Hospital/Psychiatrist DB         →  SCHEME_DATABASE (govt scheme records)
  Tutorial/Peer/Consultant routing →  Occupation-based scheme routing
  classify_emotion()               →  classify_query()
  assess_risk_level()              →  assess_scheme_match()
  get_routed_resources()           →  match_by_profile()
  POST /phq9  (depression score)   →  POST /find-schemes (profile matching)
  POST /gad7  (anxiety score)      →  GET  /resource-routing/{occupation}
  GET /resources/crisis            →  GET  /fraud-alerts
  GET /resources/hospitals/{loc}   →  GET  /schemes/{scheme_id}
  GET /resources/tutorials         →  GET  /schemes?category=...
  GET /resources/consultants       →  GET  /documents/{scheme_id}
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

OCCUPATION-BASED ROUTING (replaces PHQ-9 severity routing):
├─ FARMER       → PM Kisan + PM Fasal Bima + PM KUSUM + PM Awas Gramin
├─ STUDENT      → National Scholarship + PM Vidya Lakshmi + Ayushman Bharat
├─ SELF_EMPLOYED→ PM Mudra + Stand-Up India + PM Awas Urban
├─ SALARIED     → PM Awas Urban + PPF + Ayushman Bharat
├─ LABOURER     → Ayushman Bharat + PM Awas Gramin + PM Kisan
├─ HOMEMAKER    → PM Matru Vandana + Sukanya Samriddhi + Beti Bachao
└─ GENERAL      → Ayushman Bharat + PM Mudra + National Scholarship
"""

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field, validator
from typing import List, Optional, Dict, Any
from groq import Groq
import os
import re
import datetime
from enum import Enum
import logging

# ─────────────────────────────────────────────────────────────────
# LOGGING
# ─────────────────────────────────────────────────────────────────
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s | %(levelname)s | %(message)s",
    handlers=[logging.FileHandler("yojanaconnect.log"), logging.StreamHandler()]
)
logger = logging.getLogger(__name__)

app = FastAPI(
    title="YojanaConnect AI — Scheme Awareness Chatbot",
    description="Intelligent chatbot for discovering Indian government schemes",
    version="2.0"
)

# ─────────────────────────────────────────────────────────────────
# GROQ CONFIG
# ─────────────────────────────────────────────────────────────────
client = Groq(api_key=os.getenv("GROQ_API_KEY"))
MODEL_NAME = "llama-3.1-8b-instant"

# ─────────────────────────────────────────────────────────────────
# ENUMS  — renamed from MindBridge enums to scheme-specific enums
# ─────────────────────────────────────────────────────────────────

class SchemeCategory(str, Enum):
    """Replaces MindBridge EmotionType"""
    HEALTH      = "health"
    AGRICULTURE = "agriculture"
    HOUSING     = "housing"
    EDUCATION   = "education"
    BUSINESS    = "business"
    SAVINGS     = "savings"
    WOMEN       = "women"
    DISABILITY  = "disability"
    GENERAL     = "general"

class OccupationType(str, Enum):
    """
    Replaces MindBridge PHQ9Severity + GAD7Severity.
    Instead of depression/anxiety severity, classifies user occupation
    to route to the correct scheme category.
    """
    FARMER        = "farmer"
    STUDENT       = "student"
    SELF_EMPLOYED = "self_employed"
    SALARIED      = "salaried"
    LABOURER      = "labourer"
    HOMEMAKER     = "homemaker"
    GENERAL       = "general"

class SchemeMatchLevel(str, Enum):
    """
    Replaces MindBridge RiskLevel.
    Instead of assessing suicide/crisis risk, assesses scheme eligibility match.
    """
    PERFECT_MATCH = "perfect_match"
    GOOD_MATCH    = "good_match"
    PARTIAL_MATCH = "partial_match"
    LOW_MATCH     = "low_match"

class ConversationStage(str, Enum):
    """
    Replaces MindBridge ConversationStage (PHQ-9 screening journey).
    Now tracks scheme discovery journey instead of mental health journey.
    """
    GREETING           = "greeting"
    PROFILE_COLLECTION = "profile_collection"
    SCHEME_SUGGESTION  = "scheme_suggestion"
    SCHEME_DETAIL      = "scheme_detail"
    DOCUMENT_GUIDE     = "document_guide"
    APPLICATION_HELP   = "application_help"
    FRAUD_ALERT        = "fraud_alert"
    DEADLINE_REMINDER  = "deadline_reminder"
    ESCALATION         = "escalation"

class QueryType(str, Enum):
    """
    Replaces MindBridge ResourceCategory.
    Instead of routing to tutorials/peer-support/consultants/hospitals,
    routes to scheme info by query intent.
    """
    SCHEME_SEARCH = "scheme_search"
    ELIGIBILITY   = "eligibility"
    DOCUMENTS     = "documents"
    APPLICATION   = "application"
    DEADLINE      = "deadline"
    FRAUD_CHECK   = "fraud_check"
    STATUS_CHECK  = "status_check"
    OUT_OF_SCOPE  = "out_of_scope"

class Language(str, Enum):
    ENGLISH = "en"
    HINDI   = "hi"
    TAMIL   = "ta"

# ─────────────────────────────────────────────────────────────────
# SCHEME DATABASE
# Replaces MindBridge YOUR_WEBSITE_RESOURCES + HOSPITAL_DATABASE
# ─────────────────────────────────────────────────────────────────
SCHEME_DATABASE: Dict[str, Dict] = {

    "ayushman_bharat": {
        "id": "ayushman_bharat", "name": "Ayushman Bharat PM-JAY",
        "category": SchemeCategory.HEALTH,
        "ministry": "Ministry of Health & Family Welfare",
        "scheme_type": "Central Government",
        "description": "World's largest health insurance — Rs.5 lakh/year cashless hospitalization for BPL families. Over 50 crore beneficiaries covered.",
        "benefit": "Rs. 5 Lakh / year cashless health cover",
        "application_link": "https://pmjay.gov.in", "deadline": "Ongoing",
        "url": "/schemes/health/ayushman",
        "eligibility": {"max_income": 500000, "bpl_required": True,
                        "categories": ["General","OBC","SC/ST","EWS"],
                        "other": "Must be in SECC 2011 database or BPL household"},
        "required_documents": ["Aadhaar Card","Ration Card","Income Certificate","BPL Card","Caste Certificate (SC/ST)","Passport Size Photo"],
        "keywords": ["ayushman","health","pmjay","hospital","insurance","treatment","medical","hospitalization","health card","आयुष्मान","ஆயுஷ்மான்","स्वास्थ्य","சுகாதாரம்"]
    },

    "jan_aushadhi": {
        "id": "jan_aushadhi", "name": "PM Jan Aushadhi Pariyojana",
        "category": SchemeCategory.HEALTH,
        "ministry": "Ministry of Chemicals & Fertilizers", "scheme_type": "Central Government",
        "description": "Quality generic medicines at 50-90% lower cost through Jan Aushadhi Kendras across India.",
        "benefit": "Generic medicines at 50–90% lower cost than branded",
        "application_link": "https://janaushadhi.gov.in", "deadline": "Ongoing",
        "url": "/schemes/health/jan-aushadhi",
        "eligibility": {"max_income": 999999999, "categories": ["All"], "other": "Any citizen — no registration needed"},
        "required_documents": ["No documents required","Doctor prescription for prescription medicines"],
        "keywords": ["medicine","generic medicine","jan aushadhi","cheap medicine","affordable medicine","दवाई","மருந்து","pharmacy","drug store"]
    },

    "pm_kisan": {
        "id": "pm_kisan", "name": "PM Kisan Samman Nidhi",
        "category": SchemeCategory.AGRICULTURE,
        "ministry": "Ministry of Agriculture & Farmers Welfare", "scheme_type": "Central Government",
        "description": "Direct income support Rs.6,000/year in 3 installments of Rs.2,000 each to land-holding farmer families. Credited directly to Aadhaar-linked bank account.",
        "benefit": "Rs. 6,000 / year — 3 installments of Rs. 2,000 each",
        "application_link": "https://pmkisan.gov.in", "deadline": "31 March 2026",
        "url": "/schemes/agriculture/pm-kisan",
        "eligibility": {"occupation": "Farmer", "land_required": True, "max_income": 200000, "categories": ["All"], "other": "Must own cultivable land; Aadhaar-linked bank account mandatory"},
        "required_documents": ["Aadhaar Card (linked to mobile and bank)","Land Records / Khasra / Patta","Bank Passbook (Aadhaar-linked)","Mobile Number linked to bank"],
        "keywords": ["kisan","farmer","pm kisan","agriculture","farming","land","crop","किसान","விவசாய","kisan samman","2000 installment","farm income","agricultural support"]
    },

    "pm_fasal_bima": {
        "id": "pm_fasal_bima", "name": "PM Fasal Bima Yojana",
        "category": SchemeCategory.AGRICULTURE,
        "ministry": "Ministry of Agriculture", "scheme_type": "Central Government",
        "description": "Crop insurance protecting farmers from losses due to natural calamities, pests, and diseases. Premium subsidized heavily by government.",
        "benefit": "Crop loss compensation up to full sum insured",
        "application_link": "https://pmfby.gov.in", "deadline": "31 March 2026 (Rabi)",
        "url": "/schemes/agriculture/fasal-bima",
        "eligibility": {"occupation": "Farmer", "land_required": True, "categories": ["All"], "other": "Applicable for notified crops in notified areas only"},
        "required_documents": ["Aadhaar Card","Land Records / Khasra","Bank Passbook","Sowing Certificate","Premium payment receipt"],
        "keywords": ["fasal","crop insurance","bima","farm loss","crop loss","natural calamity","drought","flood","pest","फसल बीमा","crop damage","farm insurance"]
    },

    "pm_kusum": {
        "id": "pm_kusum", "name": "PM KUSUM Yojana",
        "category": SchemeCategory.AGRICULTURE,
        "ministry": "Ministry of New & Renewable Energy", "scheme_type": "Central Government",
        "description": "Solar pumps and solar power plants for farmers at 90% subsidy for reliable irrigation power.",
        "benefit": "Solar pump with 90% subsidy (60% Govt + 30% bank loan, 10% farmer)",
        "application_link": "https://mnre.gov.in", "deadline": "Ongoing",
        "url": "/schemes/agriculture/kusum",
        "eligibility": {"occupation": "Farmer", "categories": ["All"], "other": "Farmer with agricultural land and water source"},
        "required_documents": ["Aadhaar Card","Land Records","Bank Passbook","Electricity Bill (existing connection if any)"],
        "keywords": ["solar pump","kusum","solar farmer","irrigation power","solar energy","सौर पंप","solar scheme farmer","solar irrigation"]
    },

    "pmay_gramin": {
        "id": "pmay_gramin", "name": "PM Awas Yojana — Gramin",
        "category": SchemeCategory.HOUSING,
        "ministry": "Ministry of Rural Development", "scheme_type": "Central Government",
        "description": "Financial assistance for pucca house construction for homeless/kutcha-house families in rural areas. Rs.1.2 lakh (plains) or Rs.1.5 lakh (hills).",
        "benefit": "Rs. 1.2 to 1.5 Lakh construction assistance",
        "application_link": "https://pmayg.nic.in", "deadline": "Ongoing",
        "url": "/schemes/housing/pmay-gramin",
        "eligibility": {"area": "Rural", "max_income": 300000, "bpl_required": True, "categories": ["All"], "other": "Must be in SECC 2011 database; homeless or kutcha house"},
        "required_documents": ["Aadhaar Card","SECC 2011 Inclusion Letter","Bank Passbook (Aadhaar-linked)","Land Ownership Certificate / Patta","Job Card (MGNREGS)","Caste Certificate (SC/ST)"],
        "keywords": ["awas","house","housing","gramin","rural house","home","pucca","rural housing","आवास","வீடு","pmay gramin","rural home","kutcha house","pm awas"]
    },

    "pmay_urban": {
        "id": "pmay_urban", "name": "PM Awas Yojana — Urban",
        "category": SchemeCategory.HOUSING,
        "ministry": "Ministry of Housing & Urban Affairs", "scheme_type": "Central Government",
        "description": "Affordable urban housing through Credit Linked Subsidy Scheme (CLSS). Interest subsidy on home loans for EWS, LIG, MIG categories.",
        "benefit": "Interest subsidy up to Rs. 2.67 Lakh on home loan",
        "application_link": "https://pmaymis.gov.in", "deadline": "30 April 2026",
        "url": "/schemes/housing/pmay-urban",
        "eligibility": {"area": "Urban", "max_income": 1800000, "categories": ["EWS","LIG","MIG-I","MIG-II"], "other": "First-time home buyer; no pucca house in family name anywhere in India"},
        "required_documents": ["Aadhaar Card","Income Certificate","Address Proof","Bank Passbook","Property / Sale Agreement","Home Loan Sanction Letter"],
        "keywords": ["urban housing","city house","urban awas","home loan subsidy","flat","apartment","शहरी आवास","pmay urban","housing loan","interest subsidy","city home"]
    },

    "national_scholarship": {
        "id": "national_scholarship", "name": "National Scholarship Portal",
        "category": SchemeCategory.EDUCATION,
        "ministry": "Ministry of Education", "scheme_type": "Central Government",
        "description": "Single-window scholarship platform covering pre-matric, post-matric, and merit scholarships for SC/ST/OBC/Minority students. 50+ schemes on one portal.",
        "benefit": "Rs. 1,000 to Rs. 20,000 per month (level-dependent)",
        "application_link": "https://scholarships.gov.in", "deadline": "15 April 2026",
        "url": "/schemes/education/nsp",
        "eligibility": {"categories_required": ["SC/ST","OBC","Minority"], "max_income": 600000, "min_marks": "50% in previous class", "other": "Enrolled in government-recognised institution"},
        "required_documents": ["Aadhaar Card","Caste Certificate (SC/ST/OBC)","Income Certificate","Previous Year Marksheet","Bonafide Certificate from Institution","Bank Passbook"],
        "keywords": ["scholarship","education","student","school","college","fee","sc scholarship","st scholarship","obc scholarship","छात्रवृत्ति","படிப்பு","minority scholarship","pre matric","post matric","study grant"]
    },

    "pm_vidya_lakshmi": {
        "id": "pm_vidya_lakshmi", "name": "PM Vidya Lakshmi Education Loan",
        "category": SchemeCategory.EDUCATION,
        "ministry": "Ministry of Education", "scheme_type": "Central Government",
        "description": "Single-window education loan portal for multiple banks. Collateral-free loans up to Rs.6.5 lakh for higher education in India or abroad.",
        "benefit": "Education loan up to Rs. 6.5 Lakh — no collateral required",
        "application_link": "https://www.vidyalakshmi.co.in", "deadline": "Ongoing",
        "url": "/schemes/education/vidya-lakshmi",
        "eligibility": {"occupation": "Student", "age": "18 years and above", "other": "Confirmed admission in recognised institution for higher education"},
        "required_documents": ["Aadhaar Card","Admission / Offer Letter","Marksheets (10th 12th graduation)","Parent Income Certificate","Bank Passbook","Fee Structure from Institution"],
        "keywords": ["education loan","vidya lakshmi","higher education loan","college loan","student loan","शिक्षा ऋण","கல்வி கடன்","abroad study","university loan","study loan"]
    },

    "pm_mudra": {
        "id": "pm_mudra", "name": "PM Mudra Yojana",
        "category": SchemeCategory.BUSINESS,
        "ministry": "Ministry of Finance", "scheme_type": "Central Government",
        "description": "Collateral-free micro loans to non-farm micro enterprises. Three tiers: Shishu (up to Rs.50K), Kishore (Rs.50K-5L), Tarun (Rs.5L-10L).",
        "benefit": "Loan up to Rs. 10 Lakh — no collateral required",
        "application_link": "https://mudra.org.in", "deadline": "Ongoing",
        "url": "/schemes/business/mudra",
        "eligibility": {"occupation": ["Self-Employed","Business","Entrepreneur"], "age": "18 to 65 years", "categories": ["All"], "other": "Non-farm micro enterprise; no previous loan default"},
        "required_documents": ["Aadhaar Card","PAN Card","Business Plan / Project Report","Bank Statement (last 6 months)","Passport Size Photo","Address Proof of Business","Equipment quotation (if applicable)"],
        "keywords": ["mudra","loan","business loan","self employed","micro loan","small business","entrepreneur","व्यापार","கடன்","shishu","kishore","tarun","startup loan","working capital","50000 loan"]
    },

    "standup_india": {
        "id": "standup_india", "name": "Stand-Up India Scheme",
        "category": SchemeCategory.BUSINESS,
        "ministry": "Ministry of Finance", "scheme_type": "Central Government",
        "description": "Bank loans from Rs.10 lakh to Rs.1 crore for SC/ST and Women entrepreneurs for greenfield enterprises in manufacturing, services, or trading.",
        "benefit": "Loan from Rs. 10 Lakh to Rs. 1 Crore",
        "application_link": "https://www.standupmitra.in", "deadline": "Ongoing",
        "url": "/schemes/business/standup",
        "eligibility": {"categories": ["SC/ST","Women"], "age": "18 years and above", "other": "Greenfield enterprise (first business); active bank account"},
        "required_documents": ["Aadhaar Card","PAN Card","Caste Certificate (SC/ST) or Gender Proof","Business Plan","Bank Statement","Address Proof"],
        "keywords": ["standup india","women business loan","sc loan","st loan","crore loan","greenfield","women entrepreneur","standup mitra"]
    },

    "sukanya_samriddhi": {
        "id": "sukanya_samriddhi", "name": "Sukanya Samriddhi Yojana",
        "category": SchemeCategory.SAVINGS,
        "ministry": "Ministry of Finance", "scheme_type": "Central Government",
        "description": "Savings scheme for girl children at 8.2% p.a. — one of the highest govt rates. Tax-free maturity. Account matures when girl turns 21.",
        "benefit": "8.2% p.a. interest + Section 80C deduction + tax-free maturity",
        "application_link": "https://www.nsiindia.gov.in", "deadline": "Ongoing",
        "url": "/schemes/savings/sukanya",
        "eligibility": {"gender": "Female (girl child)", "max_age": 10, "max_accounts": "2 per family", "other": "Parent or legal guardian opens account"},
        "required_documents": ["Girl Child Birth Certificate","Parent / Guardian Aadhaar Card","Parent / Guardian PAN Card","Address Proof of Parent","Passport Size Photos"],
        "keywords": ["sukanya","girl child savings","daughter savings","girl scheme","sukanya samriddhi","8.2 interest","girl education savings","लड़की योजना","பெண் சேமிப்பு","girl investment","beti savings"]
    },

    "ppf": {
        "id": "ppf", "name": "Public Provident Fund (PPF)",
        "category": SchemeCategory.SAVINGS,
        "ministry": "Ministry of Finance", "scheme_type": "Central Government",
        "description": "Long-term savings at 7.1% p.a. with Section 80C benefits. Deposit Rs.500-Rs.1.5L/year. Matures in 15 years. Partial withdrawal after 7 years.",
        "benefit": "7.1% p.a. + Section 80C tax deduction up to Rs. 1.5 Lakh/year",
        "application_link": "https://www.nsiindia.gov.in", "deadline": "Ongoing",
        "url": "/schemes/savings/ppf",
        "eligibility": {"age": "Any Indian citizen", "categories": ["All"], "other": "One PPF account per person; NRIs not eligible"},
        "required_documents": ["Aadhaar Card","PAN Card","Address Proof","Bank Passbook or Cheque"],
        "keywords": ["ppf","public provident fund","savings","tax saving","long term savings","provident fund","बचत योजना","15 year savings","80c saving","tax free savings"]
    },

    "pm_matru_vandana": {
        "id": "pm_matru_vandana", "name": "PM Matru Vandana Yojana",
        "category": SchemeCategory.WOMEN,
        "ministry": "Ministry of Women & Child Development", "scheme_type": "Central Government",
        "description": "Maternity cash benefit of Rs.6,000 to pregnant/lactating women for first live birth. Paid in 2 installments of Rs.3,000 each.",
        "benefit": "Rs. 6,000 cash in 2 installments of Rs. 3,000 each",
        "application_link": "https://pmmvy.wcd.gov.in", "deadline": "Ongoing",
        "url": "/schemes/women/matru-vandana",
        "eligibility": {"gender": "Pregnant / Lactating Women", "age": "19 years and above", "categories": ["All"], "other": "For first live birth; registered at Anganwadi or health centre"},
        "required_documents": ["Aadhaar Card","Bank Passbook","MCP Card (Mother & Child Protection Card)","Proof of Pregnancy / Birth Certificate","Address Proof"],
        "keywords": ["matru vandana","pregnancy","maternity","pregnant","mother scheme","lactating","women scheme","child birth","महिला योजना","maamata","maternity benefit","pmmvy"]
    },

    "beti_bachao": {
        "id": "beti_bachao", "name": "Beti Bachao Beti Padhao",
        "category": SchemeCategory.WOMEN,
        "ministry": "Ministry of Women & Child Development", "scheme_type": "Central Government",
        "description": "Scheme to address declining child sex ratio and promote girl child welfare, education, and empowerment across India.",
        "benefit": "Financial incentives, awareness programmes, educational support for girls",
        "application_link": "https://wcd.nic.in", "deadline": "Ongoing",
        "url": "/schemes/women/beti-bachao",
        "eligibility": {"gender": "Girl Child", "categories": ["All"], "other": "Families with girl children; focus on districts with low sex ratio"},
        "required_documents": ["Girl Child Birth Certificate","Aadhaar Card of Parents","Address Proof"],
        "keywords": ["beti bachao","girl child","girl education","beti padhao","gender equality","बेटी बचाओ","girl welfare","sex ratio","daughter education"]
    }
}

# ─────────────────────────────────────────────────────────────────
# FRAUD ALERT DATABASE
# Replaces MindBridge HELPLINES (crisis phone numbers)
# ─────────────────────────────────────────────────────────────────
FRAUD_ALERTS: List[Dict] = [
    {
        "id": "fraud_001", "fake_scheme_name": "PM Free Laptop Yojana 2026",
        "alert_message": "This scheme does NOT exist. Fraudsters collect Aadhaar and bank details through fake websites. Do not share personal info. Report: cybercrime.gov.in",
        "reported_by": "Ministry of Electronics & IT", "report_date": "11 March 2026",
        "is_active": True,
        "keywords": ["laptop","free laptop","pm laptop","laptop yojana","free computer","pm computer"]
    },
    {
        "id": "fraud_002", "fake_scheme_name": "PM Free Mobile Phone Yojana 2026",
        "alert_message": "No such scheme exists. Fraudsters use fake govt letterheads. No government scheme requires upfront payment for a free mobile. Verify at india.gov.in.",
        "reported_by": "Ministry of Telecommunications", "report_date": "05 March 2026",
        "is_active": True,
        "keywords": ["free mobile","mobile yojana","free phone","pm mobile","free smartphone","free 4g phone"]
    },
    {
        "id": "fraud_003", "fake_scheme_name": "PM Rs.5000 Monthly Cash Transfer for All",
        "alert_message": "Completely fake scheme on WhatsApp. The government does NOT give Rs.5000 monthly to all citizens. Do not click links or share Aadhaar/bank details.",
        "reported_by": "Ministry of Finance", "report_date": "01 March 2026",
        "is_active": True,
        "keywords": ["5000 monthly","rs 5000","free cash","monthly cash","cash transfer all","5000 rupees scheme","free money all citizens"]
    },
    {
        "id": "fraud_004", "fake_scheme_name": "PM Free Gas Cylinder for All Yojana",
        "alert_message": "Fake scheme. Real scheme (PM Ujjwala Yojana) is only for BPL women without gas connection. Any scheme asking OTP or bank details for free cylinders is fraud.",
        "reported_by": "Ministry of Petroleum & Natural Gas", "report_date": "20 February 2026",
        "is_active": True,
        "keywords": ["free cylinder","free gas","free lpg","free cylinder all","pm gas scheme","ujjwala fake","free gas all"]
    }
]

# ─────────────────────────────────────────────────────────────────
# DEADLINE DATABASE
# Replaces MindBridge ROUTING_RULES timing urgency
# ─────────────────────────────────────────────────────────────────
DEADLINES: List[Dict] = [
    {"scheme": "PM Kisan Samman Nidhi", "deadline": "31 March 2026", "days_remaining": 18, "urgency": "HIGH",
     "action": "Register immediately at pmkisan.gov.in", "documents": ["Aadhaar Card","Land Records","Bank Passbook"], "category": "Agriculture"},
    {"scheme": "PM Fasal Bima Yojana (Rabi)", "deadline": "31 March 2026", "days_remaining": 18, "urgency": "HIGH",
     "action": "Apply through nearest bank or Common Service Centre", "documents": ["Aadhaar Card","Land Records","Sowing Certificate"], "category": "Agriculture"},
    {"scheme": "National Scholarship Portal", "deadline": "15 April 2026", "days_remaining": 33, "urgency": "MEDIUM",
     "action": "Apply at scholarships.gov.in with marksheets and income certificate", "documents": ["Aadhaar Card","Caste Certificate","Marksheet"], "category": "Education"},
    {"scheme": "PM Awas Yojana Urban", "deadline": "30 April 2026", "days_remaining": 48, "urgency": "MEDIUM",
     "action": "Apply at pmaymis.gov.in through bank or urban local body", "documents": ["Aadhaar Card","Income Certificate","Property Documents"], "category": "Housing"}
]

# ─────────────────────────────────────────────────────────────────
# OCCUPATION ROUTING
# Replaces MindBridge ROUTING_RULES (PHQ-9 severity routing)
# ─────────────────────────────────────────────────────────────────
OCCUPATION_ROUTING: Dict[str, Dict] = {
    OccupationType.FARMER: {
        "label": "Farmer", "message": "As a farmer, you are eligible for direct income support, crop insurance, and solar pump schemes.",
        "primary_schemes": ["pm_kisan","pm_fasal_bima","pm_kusum"],
        "secondary_schemes": ["pmay_gramin","ayushman_bharat"],
        "documents_needed": ["Aadhaar Card","Land Records / Khasra","Bank Passbook"],
        "guidance": "Register at pmkisan.gov.in first. Also apply at your nearest CSC (Common Service Centre).",
        "action_button": {"text": "Find Farmer Schemes", "url": "/schemes?category=agriculture", "action": "redirect"}
    },
    OccupationType.STUDENT: {
        "label": "Student", "message": "As a student, you can avail scholarships, education loans, and fee waivers.",
        "primary_schemes": ["national_scholarship","pm_vidya_lakshmi"],
        "secondary_schemes": ["ayushman_bharat"],
        "documents_needed": ["Aadhaar Card","Caste Certificate","Marksheet","Bonafide Certificate"],
        "guidance": "Apply at scholarships.gov.in before April 15 deadline. For loans: vidyalakshmi.co.in.",
        "action_button": {"text": "Find Education Schemes", "url": "/schemes?category=education", "action": "redirect"}
    },
    OccupationType.SELF_EMPLOYED: {
        "label": "Self-Employed / Business", "message": "As a self-employed person, you can get collateral-free business loans through PM Mudra Yojana.",
        "primary_schemes": ["pm_mudra","standup_india"],
        "secondary_schemes": ["ayushman_bharat","pmay_urban"],
        "documents_needed": ["Aadhaar Card","PAN Card","Business Plan","Bank Statement (6 months)"],
        "guidance": "Visit any bank or NBFC to apply for Mudra loan. No collateral for loans up to Rs.10 lakh.",
        "action_button": {"text": "Find Business Schemes", "url": "/schemes?category=business", "action": "redirect"}
    },
    OccupationType.SALARIED: {
        "label": "Salaried Employee", "message": "As a salaried employee, you can benefit from housing, health, and savings schemes.",
        "primary_schemes": ["pmay_urban","ppf"],
        "secondary_schemes": ["ayushman_bharat","sukanya_samriddhi"],
        "documents_needed": ["Aadhaar Card","PAN Card","Salary Slip","Bank Passbook"],
        "guidance": "Check PM Awas Yojana Urban for housing loan subsidy. Open PPF for tax-free savings.",
        "action_button": {"text": "Find Salaried Schemes", "url": "/schemes?category=housing", "action": "redirect"}
    },
    OccupationType.LABOURER: {
        "label": "Daily Wage Labourer", "message": "As a labourer, you are eligible for free health cover, housing assistance, and income support.",
        "primary_schemes": ["ayushman_bharat","pmay_gramin"],
        "secondary_schemes": ["pm_kisan"],
        "documents_needed": ["Aadhaar Card","Ration Card","BPL Card","MGNREGS Job Card"],
        "guidance": "Get Ayushman Bharat card from nearest CSC — gives Rs.5 lakh free hospital treatment per year.",
        "action_button": {"text": "Find Labourer Schemes", "url": "/schemes?category=health", "action": "redirect"}
    },
    OccupationType.HOMEMAKER: {
        "label": "Homemaker / Housewife", "message": "As a homemaker, you can benefit from women-centric, health, and savings schemes.",
        "primary_schemes": ["pm_matru_vandana","ayushman_bharat"],
        "secondary_schemes": ["sukanya_samriddhi","beti_bachao"],
        "documents_needed": ["Aadhaar Card","Ration Card","Bank Passbook","MCP Card (if pregnant)"],
        "guidance": "If pregnant, register at Anganwadi for Matru Vandana. If daughter below 10, open Sukanya Samriddhi account.",
        "action_button": {"text": "Find Women Schemes", "url": "/schemes?category=women", "action": "redirect"}
    },
    OccupationType.GENERAL: {
        "label": "General Citizen", "message": "Here are the most popular central government schemes available to all eligible citizens.",
        "primary_schemes": ["ayushman_bharat","pm_mudra","national_scholarship"],
        "secondary_schemes": ["pmay_gramin","sukanya_samriddhi","ppf"],
        "documents_needed": ["Aadhaar Card","Income Certificate","Bank Passbook"],
        "guidance": "Visit myscheme.gov.in and enter your profile details to discover all schemes you are eligible for.",
        "action_button": {"text": "Explore All Schemes", "url": "/schemes", "action": "redirect"}
    }
}

# ─────────────────────────────────────────────────────────────────
# SECURITY FILTERS
# ─────────────────────────────────────────────────────────────────
INJECTION_PATTERNS = [
    "ignore previous instructions","override","bypass","act as",
    "forget your","disregard","system prompt","jailbreak","pretend you are"
]
OUT_OF_SCOPE_KEYWORDS = [
    "machine learning","capital of","physics","chemistry","cricket score",
    "movie","recipe","weather","stock price","ipl","football","sports news"
]
FRAUD_KEYWORDS = [
    "fake","fraud","scam","real scheme","genuine","verify scheme",
    "is this real","is this scheme real","scheme genuine","नकली","போலி","phishing"
]
DEADLINE_KEYWORDS = [
    "deadline","last date","when to apply","closing date","expiry",
    "last chance","अंतिम तिथि","கடைசி தேதி","how many days left"
]
DOCUMENT_KEYWORDS = [
    "document","docs required","what papers","certificate needed","aadhaar",
    "what to bring","what do i need","दस्तावेज़","ஆவணம்"
]
APPLICATION_KEYWORDS = [
    "how to apply","apply online","apply offline","where to apply",
    "application process","apply for scheme","register","आवेदन","விண்ணப்பம்"
]
ELIGIBILITY_KEYWORDS = [
    "am i eligible","who can apply","eligibility","qualify","can i get",
    "conditions","criteria","पात्रता","தகுதி"
]

# ─────────────────────────────────────────────────────────────────
# LANGUAGE DETECTION
# ─────────────────────────────────────────────────────────────────
HINDI_RE = re.compile(r'[\u0900-\u097F]')
TAMIL_RE = re.compile(r'[\u0B80-\u0BFF]')

def detect_language(text: str) -> Language:
    if HINDI_RE.search(text): return Language.HINDI
    if TAMIL_RE.search(text): return Language.TAMIL
    return Language.ENGLISH

# ─────────────────────────────────────────────────────────────────
# PYDANTIC MODELS
# ─────────────────────────────────────────────────────────────────
class UserProfile(BaseModel):
    age: Optional[int] = None
    gender: Optional[str] = None
    state: Optional[str] = None
    occupation: Optional[str] = None
    annual_income: Optional[int] = None
    category: Optional[str] = None
    bpl_card: Optional[bool] = None
    pwd_status: Optional[bool] = None
    has_daughter: Optional[bool] = None

class ConversationMessage(BaseModel):
    role: str
    content: str
    timestamp: datetime.datetime = Field(default_factory=datetime.datetime.now)
    language_detected: Optional[str] = None
    query_type: Optional[str] = None

class ConversationContext(BaseModel):
    """Replaces MindBridge ConversationContext — tracks scheme journey not mental health"""
    user_id: str
    state_location: Optional[str] = None
    stage: ConversationStage = ConversationStage.GREETING
    messages: List[ConversationMessage] = []
    user_profile: UserProfile = UserProfile()
    matched_schemes: List[str] = []
    fraud_checks_done: List[str] = []
    language: Language = Language.ENGLISH
    escalation_done: bool = False

    def add_message(self, role: str, content: str, lang: Optional[Language] = None, qtype: Optional[str] = None):
        self.messages.append(ConversationMessage(
            role=role, content=content,
            language_detected=lang.value if lang else None,
            query_type=qtype
        ))

class ChatInput(BaseModel):
    """Replaces MindBridge EmotionInput"""
    message: str = Field(..., min_length=1, max_length=1000)
    user_id: str = Field(default="default_user")
    state: Optional[str] = None
    language: Optional[str] = None

class SchemeProfileInput(BaseModel):
    """
    Replaces MindBridge PHQ9Input + GAD7Input.
    Instead of 9-question depression scoring or 7-question anxiety scoring,
    we collect 9 profile fields for scheme eligibility matching.
    """
    user_id: str = Field(default="default_user")
    age: Optional[int] = None
    gender: Optional[str] = None
    state: Optional[str] = None
    occupation: Optional[str] = None
    annual_income: Optional[int] = None
    category: Optional[str] = None
    bpl_card: Optional[bool] = None
    pwd_status: Optional[bool] = None
    has_daughter: Optional[bool] = None

    @validator("annual_income")
    def validate_income(cls, v):
        if v is not None and v < 0:
            raise ValueError("Annual income cannot be negative")
        return v

    @validator("age")
    def validate_age(cls, v):
        if v is not None and (v < 0 or v > 120):
            raise ValueError("Age must be between 0 and 120")
        return v

class ChatResponse(BaseModel):
    """Replaces MindBridge ChatResponse"""
    reply: str
    language: str
    query_type: str
    conversation_stage: str
    schemes_found: Optional[List[Dict[str, Any]]] = None
    documents_needed: Optional[List[str]] = None
    fraud_alert: Optional[Dict[str, Any]] = None
    deadlines: Optional[List[Dict[str, Any]]] = None
    routing_button: Optional[Dict[str, str]] = None
    scheme_match_level: Optional[str] = None

# ─────────────────────────────────────────────────────────────────
# CONVERSATION STORE
# ─────────────────────────────────────────────────────────────────
conversation_store: Dict[str, ConversationContext] = {}

def get_or_create_context(user_id: str, state: Optional[str] = None) -> ConversationContext:
    if user_id not in conversation_store:
        conversation_store[user_id] = ConversationContext(user_id=user_id, state_location=state)
        logger.info(f"New session: user={user_id} state={state}")
    return conversation_store[user_id]

# ─────────────────────────────────────────────────────────────────
# GROQ LLM CALL
# ─────────────────────────────────────────────────────────────────
def groq_generate(messages: List[Dict], temperature: float = 0.4, tokens: int = 220) -> str:
    try:
        resp = client.chat.completions.create(model=MODEL_NAME, temperature=temperature, max_tokens=tokens, messages=messages)
        return resp.choices[0].message.content.strip()
    except Exception as e:
        logger.error(f"Groq error: {e}")
        return "I am having trouble right now. Please try again."

# ─────────────────────────────────────────────────────────────────
# QUERY CLASSIFIER
# Replaces MindBridge classify_emotion() — detects scheme intent not emotion
# ─────────────────────────────────────────────────────────────────
def classify_query(message: str) -> QueryType:
    m = message.lower()
    if any(k in m for k in FRAUD_KEYWORDS):     return QueryType.FRAUD_CHECK
    if any(k in m for k in DEADLINE_KEYWORDS):  return QueryType.DEADLINE
    if any(k in m for k in DOCUMENT_KEYWORDS):  return QueryType.DOCUMENTS
    if any(k in m for k in APPLICATION_KEYWORDS): return QueryType.APPLICATION
    if any(k in m for k in ELIGIBILITY_KEYWORDS): return QueryType.ELIGIBILITY
    if any(k in m for k in OUT_OF_SCOPE_KEYWORDS): return QueryType.OUT_OF_SCOPE
    return QueryType.SCHEME_SEARCH

# ─────────────────────────────────────────────────────────────────
# SCHEME SEARCH
# Replaces MindBridge get_routed_resources() — scheme matching not hospital routing
# ─────────────────────────────────────────────────────────────────
def search_by_keyword(message: str) -> List[Dict]:
    m = message.lower()
    return [s for s in SCHEME_DATABASE.values() if any(kw in m for kw in s["keywords"])]

def match_by_profile(profile: UserProfile) -> List[Dict]:
    """
    Replaces MindBridge PHQ-9 + GAD-7 severity scoring + routing.
    Instead of computing a depression score and routing to tutorials/hospitals,
    we analyse occupation/income/BPL to route to matching government schemes.
    """
    occ = (profile.occupation or "").lower()
    bpl = profile.bpl_card or False
    inc = profile.annual_income or 999999

    if any(x in occ for x in ["farm","kisan","agriculture","crop"]): key = OccupationType.FARMER
    elif "student" in occ:                                             key = OccupationType.STUDENT
    elif any(x in occ for x in ["self","business","entrepreneur","shop"]): key = OccupationType.SELF_EMPLOYED
    elif any(x in occ for x in ["salaried","employee","job","office"]): key = OccupationType.SALARIED
    elif any(x in occ for x in ["labour","worker","daily wage","mazdoor"]): key = OccupationType.LABOURER
    elif any(x in occ for x in ["homemaker","housewife","home maker"]): key = OccupationType.HOMEMAKER
    elif bpl:                                                           key = OccupationType.LABOURER
    else:                                                               key = OccupationType.GENERAL

    rule    = OCCUPATION_ROUTING[key]
    results = []
    for sid in rule["primary_schemes"] + rule["secondary_schemes"]:
        s = SCHEME_DATABASE.get(sid)
        if not s: continue
        if inc <= s["eligibility"].get("max_income", 999999999):
            results.append(s)
    return results

def assess_scheme_match(scheme: Dict, profile: UserProfile) -> SchemeMatchLevel:
    """
    Replaces MindBridge assess_risk_level() — scheme eligibility match not risk assessment
    """
    score, max_score = 0, 0
    elig = scheme["eligibility"]
    if "max_income" in elig and profile.annual_income is not None:
        max_score += 1
        if profile.annual_income <= elig["max_income"]: score += 1
    if elig.get("bpl_required") and profile.bpl_card is not None:
        max_score += 1
        if profile.bpl_card: score += 1
    if "categories" in elig and profile.category:
        max_score += 1
        if "All" in elig["categories"] or profile.category in elig["categories"]: score += 1
    if "gender" in elig and profile.gender:
        max_score += 1
        if profile.gender.lower() in elig["gender"].lower(): score += 1
    if max_score == 0: return SchemeMatchLevel.GOOD_MATCH
    r = score / max_score
    if r == 1.0: return SchemeMatchLevel.PERFECT_MATCH
    if r >= 0.7: return SchemeMatchLevel.GOOD_MATCH
    if r >= 0.4: return SchemeMatchLevel.PARTIAL_MATCH
    return SchemeMatchLevel.LOW_MATCH

def check_fraud(message: str) -> Optional[Dict]:
    """Replaces MindBridge crisis detection — detects fake schemes not suicidal language"""
    m = message.lower()
    for alert in FRAUD_ALERTS:
        if any(kw in m for kw in alert["keywords"]) and alert["is_active"]:
            return alert
    return None

def build_routing_button(schemes: List[Dict]) -> Optional[Dict[str, str]]:
    """Replaces MindBridge 'Book Consultant' / 'Find Hospital' buttons"""
    if not schemes:
        return {"text": "Explore All Schemes", "url": "https://myscheme.gov.in", "action": "redirect"}
    mapping = {
        SchemeCategory.HEALTH:      ("Apply for Health Scheme",   "/schemes/health"),
        SchemeCategory.AGRICULTURE: ("Apply for Farm Scheme",     "/schemes/agriculture"),
        SchemeCategory.HOUSING:     ("Apply for Housing Scheme",  "/schemes/housing"),
        SchemeCategory.EDUCATION:   ("Apply for Scholarship",     "/schemes/education"),
        SchemeCategory.BUSINESS:    ("Get Business Loan",         "/schemes/business"),
        SchemeCategory.SAVINGS:     ("Open Savings Account",      "/schemes/savings"),
        SchemeCategory.WOMEN:       ("Apply for Women's Scheme",  "/schemes/women"),
    }
    text, url = mapping.get(schemes[0]["category"], ("View Scheme", "/schemes"))
    return {"text": text, "url": url, "action": "redirect_to_scheme"}

# ─────────────────────────────────────────────────────────────────
# CHAT ENDPOINT
# Replaces MindBridge POST /chat (emotion detect + PHQ-9 suggestion)
# Now: intent classify → scheme/fraud/docs/deadline handler
# ─────────────────────────────────────────────────────────────────
@app.post("/chat", response_model=ChatResponse)
def chat(data: ChatInput):
    try:
        context = get_or_create_context(data.user_id, data.state)
        message = data.message.strip()

        if any(p in message.lower() for p in INJECTION_PATTERNS):
            logger.warning(f"Injection blocked: user={data.user_id}")
            return ChatResponse(reply="I can only answer questions about government schemes.", language="en", query_type="blocked", conversation_stage=context.stage.value)

        lang  = detect_language(message)
        qtype = classify_query(message)
        context.language = lang
        context.add_message("user", message, lang=lang, qtype=qtype.value)

        if context.stage == ConversationStage.GREETING:
            context.stage = ConversationStage.SCHEME_SUGGESTION

        # OUT OF SCOPE
        if qtype == QueryType.OUT_OF_SCOPE:
            reply = ("I can only help with Indian government scheme information. "
                     "Ask me about health, agriculture, housing, education, business, "
                     "savings, or women's schemes — or about eligibility, documents, deadlines, or fraud alerts.")
            context.add_message("assistant", reply, lang=lang)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value, conversation_stage=context.stage.value)

        # FRAUD CHECK — replaces MindBridge crisis/suicide detection + helplines
        if qtype == QueryType.FRAUD_CHECK:
            fraud = check_fraud(message)
            if fraud:
                context.escalation_done = True
                context.fraud_checks_done.append(fraud["fake_scheme_name"])
                context.stage = ConversationStage.FRAUD_ALERT
                reply = (f"FRAUD ALERT — '{fraud['fake_scheme_name']}' is NOT a real scheme!\n\n"
                         f"{fraud['alert_message']}\n\n"
                         f"Reported by: {fraud['reported_by']} ({fraud['report_date']})\n\n"
                         "Always verify schemes at india.gov.in or myscheme.gov.in.")
                context.add_message("assistant", reply)
                logger.warning(f"FRAUD: user={data.user_id} scheme={fraud['fake_scheme_name']}")
                return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value,
                                    fraud_alert=fraud, conversation_stage=ConversationStage.FRAUD_ALERT.value,
                                    routing_button={"text": "Report Fraud", "url": "https://cybercrime.gov.in", "action": "report"})
            reply = ("No specific fraud alert found for that scheme. Safety rules:\n"
                     "1. Verify any scheme at india.gov.in before applying.\n"
                     "2. Government schemes are always FREE — never pay to apply.\n"
                     "3. Never share OTP, bank details, or password.\n"
                     "4. Report fraud at cybercrime.gov.in or call 1930.")
            context.add_message("assistant", reply)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value, conversation_stage=context.stage.value)

        # DEADLINE
        if qtype == QueryType.DEADLINE:
            context.stage = ConversationStage.DEADLINE_REMINDER
            urgent   = [d for d in DEADLINES if d["urgency"] == "HIGH"]
            upcoming = [d for d in DEADLINES if d["urgency"] == "MEDIUM"]
            reply = "Upcoming scheme deadlines:\n\n"
            if urgent:
                reply += "URGENT — Apply immediately:\n"
                for d in urgent:
                    reply += f"• {d['scheme']}: {d['deadline']} ({d['days_remaining']} days left)\n  → {d['action']}\n"
            if upcoming:
                reply += "\nUpcoming:\n"
                for d in upcoming:
                    reply += f"• {d['scheme']}: {d['deadline']} ({d['days_remaining']} days left)\n  → {d['action']}\n"
            context.add_message("assistant", reply)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value, deadlines=DEADLINES,
                                conversation_stage=context.stage.value,
                                routing_button={"text": "Apply Now", "url": "https://myscheme.gov.in", "action": "redirect"})

        # DOCUMENTS
        if qtype == QueryType.DOCUMENTS:
            context.stage = ConversationStage.DOCUMENT_GUIDE
            schemes = search_by_keyword(message)
            if schemes:
                s    = schemes[0]
                docs = s["required_documents"]
                reply = (f"For {s['name']}, you need:\n\n" +
                         "\n".join(f"  {i+1}. {d}" for i, d in enumerate(docs)) +
                         "\n\nTips:\n• Keep original + self-attested photocopy of each.\n"
                         "• Aadhaar must be linked to mobile number and bank account.\n"
                         "• Income certificate must be issued within the last 6 months.")
            else:
                docs  = ["Aadhaar Card","Income Certificate","Bank Passbook","Caste Certificate (if applicable)","Address Proof"]
                reply = ("Most government schemes need these documents:\n\n" +
                         "\n".join(f"  {i+1}. {d}" for i, d in enumerate(docs)) +
                         "\n\nTell me which specific scheme you are asking about for an exact list.")
            context.add_message("assistant", reply)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value,
                                documents_needed=docs, schemes_found=schemes[:1] if schemes else None,
                                conversation_stage=context.stage.value)

        # APPLICATION GUIDANCE
        if qtype == QueryType.APPLICATION:
            context.stage = ConversationStage.APPLICATION_HELP
            schemes = search_by_keyword(message)
            if schemes:
                s = schemes[0]
                reply = (f"How to apply for {s['name']}:\n\n"
                         f"Step 1: Visit {s['application_link']}\n"
                         f"Step 2: Register with Aadhaar + mobile number\n"
                         f"Step 3: Fill your profile details\n"
                         f"Step 4: Upload required documents (PDF/JPG, max 2 MB each)\n"
                         f"Step 5: Submit and save the reference number\n\n"
                         f"Deadline: {s['deadline']}\n"
                         "Offline: Visit nearest Common Service Centre (CSC).")
                button = {"text": f"Apply for {s['name']}", "url": s["application_link"], "action": "apply_now"}
            else:
                reply = ("To apply for any central government scheme:\n\n"
                         "Step 1: Visit myscheme.gov.in\n"
                         "Step 2: Enter your profile details\n"
                         "Step 3: Browse matching schemes\n"
                         "Step 4: Click Apply — redirected to official portal\n"
                         "Offline: Visit nearest CSC or Gram Panchayat.")
                button = {"text": "Find & Apply", "url": "https://myscheme.gov.in", "action": "redirect"}
            context.add_message("assistant", reply)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value,
                                schemes_found=schemes[:1] if schemes else None,
                                conversation_stage=context.stage.value, routing_button=button)

        # ELIGIBILITY
        if qtype == QueryType.ELIGIBILITY:
            schemes = search_by_keyword(message)
            if schemes:
                s    = schemes[0]
                elig = s["eligibility"]
                reply = (f"Eligibility for {s['name']}:\n\n" +
                         "\n".join(f"  • {k.replace('_',' ').title()}: {v}" for k, v in elig.items()) +
                         f"\n\nBenefit: {s['benefit']}\nDeadline: {s['deadline']}")
            else:
                reply = ("Please tell me which scheme you want to check eligibility for.\n"
                         "Example: 'Am I eligible for Ayushman Bharat?' or 'Who can apply for PM Kisan?'")
            context.add_message("assistant", reply)
            return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value,
                                schemes_found=schemes[:1] if schemes else None,
                                conversation_stage=context.stage.value,
                                routing_button=build_routing_button(schemes))

        # DEFAULT: SCHEME SEARCH
        schemes = search_by_keyword(message)
        button  = build_routing_button(schemes)
        if schemes:
            s = schemes[0]
            context.matched_schemes.append(s["id"])
            reply = groq_generate([
                {"role": "system", "content": (
                    "You are YojanaBot, a helpful Indian government scheme assistant. "
                    "Explain schemes clearly in 4-5 lines. Mention benefit amount, "
                    "who is eligible, and official website. Keep it simple and accurate.")},
                {"role": "user", "content": (
                    f"Explain briefly:\nName: {s['name']}\nBenefit: {s['benefit']}\n"
                    f"Description: {s['description']}\nOfficial site: {s['application_link']}\n"
                    f"Deadline: {s['deadline']}")}
            ], temperature=0.35, tokens=220)
        else:
            reply = ("Namaste! I am YojanaBot.\n\nI could not find a scheme for your query. Ask me about:\n"
                     "  • Health schemes (Ayushman Bharat, Jan Aushadhi)\n"
                     "  • Agriculture schemes (PM Kisan, Fasal Bima, KUSUM)\n"
                     "  • Housing schemes (PM Awas Yojana Rural / Urban)\n"
                     "  • Education schemes (National Scholarship, Education Loan)\n"
                     "  • Business loans (PM Mudra, Stand-Up India)\n"
                     "  • Savings schemes (Sukanya Samriddhi, PPF)\n"
                     "  • Women schemes (Matru Vandana, Beti Bachao)\n"
                     "Or type 'fake scheme' to check for fraud alerts.")
        context.add_message("assistant", reply)
        return ChatResponse(reply=reply, language=lang.value, query_type=qtype.value,
                            schemes_found=[{k: v for k, v in s.items() if k != "keywords"} for s in schemes[:3]] if schemes else None,
                            conversation_stage=context.stage.value, routing_button=button)

    except Exception as e:
        logger.error(f"Chat error: {e}")
        raise HTTPException(status_code=500, detail="Internal server error")

# ─────────────────────────────────────────────────────────────────
# FIND-SCHEMES — replaces MindBridge POST /phq9 + POST /gad7
# ─────────────────────────────────────────────────────────────────
@app.post("/find-schemes")
def find_schemes(data: SchemeProfileInput):
    """
    Replaces MindBridge POST /phq9 (PHQ-9 depression scoring + severity routing)
    and POST /gad7 (GAD-7 anxiety scoring + severity routing).
    Instead: user submits occupation/income/category profile → gets matched schemes.
    """
    try:
        profile = UserProfile(age=data.age, gender=data.gender, state=data.state,
                              occupation=data.occupation, annual_income=data.annual_income,
                              category=data.category, bpl_card=data.bpl_card,
                              pwd_status=data.pwd_status, has_daughter=data.has_daughter)
        matched = match_by_profile(profile)
        occ = (data.occupation or "").lower()
        if any(x in occ for x in ["farm","kisan"]):          key = OccupationType.FARMER
        elif "student" in occ:                                key = OccupationType.STUDENT
        elif any(x in occ for x in ["self","business"]):     key = OccupationType.SELF_EMPLOYED
        elif any(x in occ for x in ["salaried","job"]):      key = OccupationType.SALARIED
        elif any(x in occ for x in ["labour","worker"]):     key = OccupationType.LABOURER
        elif any(x in occ for x in ["homemaker","housewife"]): key = OccupationType.HOMEMAKER
        elif data.bpl_card:                                   key = OccupationType.LABOURER
        else:                                                 key = OccupationType.GENERAL

        rule = OCCUPATION_ROUTING[key]
        schemes_out = []
        for s in matched:
            ml = assess_scheme_match(s, profile)
            schemes_out.append({"scheme_id": s["id"], "name": s["name"], "category": s["category"],
                                 "benefit": s["benefit"], "deadline": s["deadline"],
                                 "apply_at": s["application_link"], "required_documents": s["required_documents"],
                                 "url": s["url"], "match_level": ml.value})

        logger.info(f"find-schemes: user={data.user_id} occ={data.occupation} matched={len(matched)}")
        return {
            "profile_summary": {"occupation": data.occupation, "annual_income": data.annual_income,
                                 "category": data.category, "bpl_card": data.bpl_card, "state": data.state},
            "routing_message": rule["message"], "guidance": rule["guidance"],
            "schemes_matched": len(matched), "schemes": schemes_out,
            "documents_to_prepare": rule["documents_needed"], "action_button": rule["action_button"]
        }
    except Exception as e:
        logger.error(f"find-schemes error: {e}")
        raise HTTPException(status_code=500, detail="Error finding schemes")

# ─────────────────────────────────────────────────────────────────
# SCHEME ENDPOINTS — replace MindBridge /resources/tutorials, /resources/consultants
# ─────────────────────────────────────────────────────────────────
@app.get("/schemes")
def list_schemes(category: Optional[str] = None):
    results = [s for s in SCHEME_DATABASE.values() if not category or s["category"] == category]
    return {"total": len(results), "schemes": [{k: v for k, v in s.items() if k != "keywords"} for s in results]}

@app.get("/schemes/{scheme_id}")
def get_scheme(scheme_id: str):
    s = SCHEME_DATABASE.get(scheme_id)
    if not s: raise HTTPException(status_code=404, detail="Scheme not found")
    return {k: v for k, v in s.items() if k != "keywords"}

@app.get("/documents/{scheme_id}")
def get_documents(scheme_id: str):
    s = SCHEME_DATABASE.get(scheme_id)
    if not s: raise HTTPException(status_code=404, detail="Scheme not found")
    return {"scheme": s["name"], "required_documents": s["required_documents"],
            "note": "Keep original + self-attested photocopy of each document",
            "tip": "Aadhaar must be linked to mobile and bank for most schemes"}

# ─────────────────────────────────────────────────────────────────
# FRAUD ALERTS — replaces MindBridge GET /resources/crisis (helplines)
# ─────────────────────────────────────────────────────────────────
@app.get("/fraud-alerts")
def get_fraud_alerts():
    return {"total": len(FRAUD_ALERTS), "alerts": [a for a in FRAUD_ALERTS if a["is_active"]],
            "safety_tip": "Always verify schemes at india.gov.in before applying.",
            "report_fraud": "cybercrime.gov.in or call 1930"}

@app.get("/fraud-alerts/check")
def check_fraud_api(scheme_name: str):
    name_lower = scheme_name.lower()
    for alert in FRAUD_ALERTS:
        if any(kw in name_lower for kw in alert["keywords"]) and alert["is_active"]:
            return {"is_fraud": True, "alert": alert}
    return {"is_fraud": False, "message": "No fraud alert found. Still verify at india.gov.in."}

# ─────────────────────────────────────────────────────────────────
# DEADLINES
# ─────────────────────────────────────────────────────────────────
@app.get("/deadlines")
def get_deadlines():
    return {"urgent_deadlines": [d for d in DEADLINES if d["urgency"] == "HIGH"],
            "upcoming_deadlines": [d for d in DEADLINES if d["urgency"] == "MEDIUM"],
            "total": len(DEADLINES)}

# ─────────────────────────────────────────────────────────────────
# RESOURCE ROUTING — replaces MindBridge GET /resource-routing/{severity}
# Instead of PHQ-9 severity routing (minimal→severe → tutorials/hospitals),
# we route by occupation (farmer→agriculture, student→education, etc.)
# ─────────────────────────────────────────────────────────────────
@app.get("/resource-routing/{occupation}")
def get_resource_routing(occupation: str, bpl: bool = False, state: Optional[str] = None):
    """
    Replaces MindBridge GET /resource-routing/{severity}.
    MindBridge routed: minimal→tutorials, mild→peer-support, moderate→consultants, severe→hospitals
    YojanaConnect routes: farmer→agriculture, student→education, self_employed→business, etc.
    """
    occ = occupation.lower()
    if any(x in occ for x in ["farm","kisan"]):          key = OccupationType.FARMER
    elif "student" in occ:                                key = OccupationType.STUDENT
    elif any(x in occ for x in ["self","business"]):     key = OccupationType.SELF_EMPLOYED
    elif any(x in occ for x in ["salaried","job"]):      key = OccupationType.SALARIED
    elif any(x in occ for x in ["labour","worker"]):     key = OccupationType.LABOURER
    elif any(x in occ for x in ["home","housewife"]):    key = OccupationType.HOMEMAKER
    elif bpl:                                             key = OccupationType.LABOURER
    else:                                                 key = OccupationType.GENERAL

    rule = OCCUPATION_ROUTING[key]
    primary   = [SCHEME_DATABASE[s] for s in rule["primary_schemes"] if s in SCHEME_DATABASE]
    secondary = [SCHEME_DATABASE[s] for s in rule["secondary_schemes"] if s in SCHEME_DATABASE]

    return {
        "occupation": occupation, "routing_label": rule["label"],
        "routing_message": rule["message"], "guidance": rule["guidance"],
        "primary_schemes":   [{"name": s["name"], "benefit": s["benefit"], "apply_at": s["application_link"], "url": s["url"]} for s in primary],
        "secondary_schemes": [{"name": s["name"], "benefit": s["benefit"], "apply_at": s["application_link"], "url": s["url"]} for s in secondary],
        "documents_needed": rule["documents_needed"], "action_button": rule["action_button"]
    }

# ─────────────────────────────────────────────────────────────────
# USER PROGRESS — same as MindBridge /user-progress/{user_id}
# ─────────────────────────────────────────────────────────────────
@app.get("/user-progress/{user_id}")
def get_user_progress(user_id: str):
    ctx = get_or_create_context(user_id)
    return {"user_id": user_id, "stage": ctx.stage.value, "language": ctx.language.value,
            "schemes_viewed": ctx.matched_schemes, "fraud_checks_done": ctx.fraud_checks_done,
            "escalation_done": ctx.escalation_done, "messages_exchanged": len(ctx.messages),
            "state": ctx.state_location}

# ─────────────────────────────────────────────────────────────────
# HOME & HEALTH
# ─────────────────────────────────────────────────────────────────
@app.get("/")
def home():
    return {
        "service": "YojanaConnect AI — Government Scheme Awareness Chatbot",
        "version": "2.0",
        "endpoints": {
            "chat":          "POST /chat",
            "find_schemes":  "POST /find-schemes",
            "list_schemes":  "GET  /schemes?category=health",
            "scheme_detail": "GET  /schemes/{scheme_id}",
            "documents":     "GET  /documents/{scheme_id}",
            "fraud_alerts":  "GET  /fraud-alerts",
            "fraud_check":   "GET  /fraud-alerts/check?scheme_name=...",
            "deadlines":     "GET  /deadlines",
            "routing":       "GET  /resource-routing/{occupation}",
            "user_progress": "GET  /user-progress/{user_id}"
        }
    }

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "YojanaConnect Chatbot v2.0",
            "timestamp": datetime.datetime.now().isoformat(),
            "schemes_loaded": len(SCHEME_DATABASE), "fraud_alerts": len(FRAUD_ALERTS)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
