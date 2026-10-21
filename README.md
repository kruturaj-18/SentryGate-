# RepoGuardian — SentryGate

> **Intelligent GitHub Repository Quality Management & Automated Gate Enforcement Platform**

RepoGuardian — SentryGate is an enterprise-grade quality management platform designed to prevent low-quality, untested, insecure, or duplicate contributions from diluting open-source and internal software repositories.

---

## 🌟 Key Architecture & Capabilities

### 1. 🛡️ Automated Quality Gate Validation Engine
- **Conventional PR Titles**: Validates PR titles against Conventional Commit patterns (`feat(...)`, `fix(...)`, `refactor(...)`).
- **Secret & Credential Scanner**: Scans changeset diffs for leaked API keys, RSA private keys, and high-entropy credentials.
- **Change Size Boundary**: Enforces quantitative thresholds (max 30 files & 1,000 lines modified).
- **Test Presence Enforcement**: Verifies unit and integration test coverage within PR changesets.
- **AST & Code Quality Analysis**: Detects code smells, complexity anti-patterns, and unhandled branches.
- **Required Governance Files**: Checks repository compliance for mandatory governance files (`README.md`, `LICENSE`, `CONTRIBUTING.md`).

### 2. 🔍 Automated Duplicate Issue Radar
- Multi-metric text similarity engine utilizing **TF-IDF Vectorization**, **Jaccard Token Similarity**, and **Levenshtein Edit Distance**.
- Automatically flags potential duplicate issues upon receipt of GitHub webhooks.

### 3. 👤 Contributor Trust Intelligence
- Multi-dimensional scoring evaluating **PR Acceptance Rates**, **Build Reliability**, and **GitHub Account Maturity**.
- Classifies contributors into actionable risk tiers (`TRUSTED`, `NEUTRAL`, `HIGH_RISK`).

---

## 🚀 Getting Started

### Prerequisites
- Node.js v18+
- Java JDK 17+ (or Maven 3.8+)
- PostgreSQL 14+ (or Docker)

### Running with Docker Compose
```bash
docker-compose up --build
```

### Running Locally

#### Backend (Spring Boot)
```bash
cd backend
mvn spring-boot:run
```

#### Frontend (Vite + React)
```bash
cd frontend
npm install
npm run dev
```
website:https://sentry-gate-one.vercel.app/
---

## 💻 Tech Stack
- **Backend**: Java 17, Spring Boot 3.2, Spring Security, JWT, JPA / Hibernate, Jackson
- **Database**: PostgreSQL 16
- **Frontend**: React 18, Vite, Lucide Icons, Glassmorphism CSS Design System
- **Containerization**: Docker & Docker Compose
