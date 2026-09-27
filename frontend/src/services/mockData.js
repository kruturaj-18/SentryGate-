export const MOCK_REPOSITORIES = [
  {
    id: "repo-1",
    name: "sentrygate-core",
    fullName: "sentrygate/sentrygate-core",
    owner: "sentrygate",
    defaultBranch: "main",
    isActive: true,
    qualityGateEnabled: true,
    duplicateDetectionEnabled: true,
    trustScoringEnabled: true,
    createdAt: "2026-01-15T10:00:00Z"
  },
  {
    id: "repo-2",
    name: "cloud-infrastructure-k8s",
    fullName: "sentrygate/cloud-infrastructure-k8s",
    owner: "sentrygate",
    defaultBranch: "master",
    isActive: true,
    qualityGateEnabled: true,
    duplicateDetectionEnabled: true,
    trustScoringEnabled: true,
    createdAt: "2026-02-01T14:30:00Z"
  },
  {
    id: "repo-3",
    name: "react-dashboard-ui",
    fullName: "sentrygate/react-dashboard-ui",
    owner: "sentrygate",
    defaultBranch: "main",
    isActive: true,
    qualityGateEnabled: false,
    duplicateDetectionEnabled: true,
    trustScoringEnabled: true,
    createdAt: "2026-02-18T09:15:00Z"
  }
];

export const MOCK_PULL_REQUESTS = [
  {
    id: "pr-101",
    repositoryId: "repo-1",
    githubPrId: 1001,
    prNumber: 42,
    title: "feat(auth): implement JWT token rotation & revocation list",
    body: "This PR adds full refresh token rotation and Redis backed token revocation checks.\n\nFixes #104.",
    state: "open",
    authorUsername: "alex-developer",
    authorAvatarUrl: "https://avatars.githubusercontent.com/u/123456?v=4",
    sourceBranch: "feature/jwt-rotation",
    targetBranch: "main",
    headSha: "a9f8e7d",
    validationStatus: "PASSED",
    additions: 185,
    deletions: 24,
    changedFilesCount: 6,
    createdAt: "2026-09-26T14:20:00Z"
  },
  {
    id: "pr-102",
    repositoryId: "repo-1",
    githubPrId: 1002,
    prNumber: 43,
    title: "fix: quick patch",
    body: "fix stuff",
    state: "open",
    authorUsername: "newbie-coder",
    authorAvatarUrl: "https://avatars.githubusercontent.com/u/987654?v=4",
    sourceBranch: "fix-stuff",
    targetBranch: "main",
    headSha: "b1c2d3e",
    validationStatus: "FAILED",
    additions: 1250,
    deletions: 12,
    changedFilesCount: 45,
    createdAt: "2026-09-27T08:10:00Z"
  },
  {
    id: "pr-103",
    repositoryId: "repo-1",
    githubPrId: 1003,
    prNumber: 44,
    title: "refactor(database): optimize query performance on IssueSimilarity index",
    body: "Added composite database indices on source_issue_id and target_issue_id.\n\nTested with 1M rows.",
    state: "open",
    authorUsername: "senior-arch",
    authorAvatarUrl: "https://avatars.githubusercontent.com/u/456789?v=4",
    sourceBranch: "refactor/db-indices",
    targetBranch: "main",
    headSha: "c3d4e5f",
    validationStatus: "PASSED",
    additions: 42,
    deletions: 18,
    changedFilesCount: 3,
    createdAt: "2026-09-27T09:00:00Z"
  }
];

export const MOCK_VALIDATION_RESULTS = {
  "pr-101": {
    id: "val-1",
    pullRequestId: "pr-101",
    passed: true,
    score: 95,
    summary: "Evaluated 8 rules: PASSED (95/100)",
    evaluatedAt: "2026-09-26T14:21:05Z",
    results: [
      { id: "r1", ruleCode: "PR_TITLE_FORMAT", passed: true, score: 100, message: "PR title adheres to Conventional Commits: 'feat(auth): ...'" },
      { id: "r2", ruleCode: "PR_DESCRIPTION_LENGTH", passed: true, score: 100, message: "Description length 98 characters exceeds minimum 20" },
      { id: "r3", ruleCode: "BRANCH_NAMING", passed: true, score: 100, message: "Branch name 'feature/jwt-rotation' matches required pattern" },
      { id: "r4", ruleCode: "SECRET_DETECTION", passed: true, score: 100, message: "No hardcoded credentials or private keys detected" },
      { id: "r5", ruleCode: "CHANGE_SIZE", passed: true, score: 90, message: "Changed 6 files and 209 lines (within safe boundary)" },
      { id: "r6", ruleCode: "TEST_PRESENCE", passed: true, score: 100, message: "Test files detected: JwtTokenProviderTest.java" },
      { id: "r7", ruleCode: "STATIC_ANALYSIS", passed: true, score: 90, message: "No high severity static analysis anti-patterns found" },
      { id: "r8", ruleCode: "REQUIRED_FILES", passed: true, score: 100, message: "All required project files (README.md, LICENSE) exist" }
    ]
  },
  "pr-102": {
    id: "val-2",
    pullRequestId: "pr-102",
    passed: false,
    score: 38,
    summary: "Evaluated 8 rules: FAILED (38/100)",
    evaluatedAt: "2026-09-27T08:11:00Z",
    results: [
      { id: "r1", ruleCode: "PR_TITLE_FORMAT", passed: false, score: 0, message: "PR title 'fix: quick patch' is missing required scope prefix" },
      { id: "r2", ruleCode: "PR_DESCRIPTION_LENGTH", passed: false, score: 0, message: "Description 'fix stuff' (9 chars) is below required 20 chars" },
      { id: "r3", ruleCode: "BRANCH_NAMING", passed: false, score: 0, message: "Branch name 'fix-stuff' violates convention (expected feature/*, fix/*, bugfix/*)" },
      { id: "r4", ruleCode: "SECRET_DETECTION", passed: true, score: 100, message: "No hardcoded secrets detected" },
      { id: "r5", ruleCode: "CHANGE_SIZE", passed: false, score: 0, message: "PR modifies 45 files & 1262 lines — exceeds max change size (30 files, 1000 lines)" },
      { id: "r6", ruleCode: "TEST_PRESENCE", passed: false, score: 0, message: "No unit or integration tests included in this PR" },
      { id: "r7", ruleCode: "STATIC_ANALYSIS", passed: true, score: 80, message: "Minor warning: 2 unused variables detected" },
      { id: "r8", ruleCode: "REQUIRED_FILES", passed: true, score: 100, message: "Required files present" }
    ]
  }
};

export const MOCK_ISSUES = [
  {
    id: "issue-1",
    repositoryId: "repo-1",
    githubIssueId: 2001,
    issueNumber: 104,
    title: "JWT authentication fails when token expires during active session",
    body: "When user session token expires, API requests return 401 without providing refresh token flow.",
    state: "open",
    authorUsername: "user-alpha",
    authorAvatarUrl: "https://avatars.githubusercontent.com/u/111111?v=4",
    hasDuplicateCandidates: true,
    createdAt: "2026-09-20T11:00:00Z",
    duplicateMatches: [
      {
        id: "sim-1",
        sourceIssueId: "issue-1",
        targetIssueId: "issue-2",
        targetIssueNumber: 88,
        targetIssueTitle: "Auth token expiration throws 401 instead of refreshing automatically",
        similarityScore: 0.8850,
        isDuplicateCandidate: true,
        status: "open"
      },
      {
        id: "sim-2",
        sourceIssueId: "issue-1",
        targetIssueId: "issue-3",
        targetIssueNumber: 52,
        targetIssueTitle: "User gets logged out unexpectedly after 1 hour session",
        similarityScore: 0.7240,
        isDuplicateCandidate: true,
        status: "closed"
      }
    ]
  },
  {
    id: "issue-2",
    repositoryId: "repo-1",
    githubIssueId: 2002,
    issueNumber: 88,
    title: "Auth token expiration throws 401 instead of refreshing automatically",
    body: "After token expires, the client frontend receives 401 Unauthorized instead of renewing JWT.",
    state: "open",
    authorUsername: "user-beta",
    authorAvatarUrl: "https://avatars.githubusercontent.com/u/222222?v=4",
    hasDuplicateCandidates: true,
    createdAt: "2026-09-15T09:30:00Z",
    duplicateMatches: []
  }
];

export const MOCK_CONTRIBUTORS = [
  {
    id: "contrib-1",
    repositoryId: "repo-1",
    username: "alex-developer",
    avatarUrl: "https://avatars.githubusercontent.com/u/123456?v=4",
    totalPrsSubmitted: 28,
    prsMerged: 26,
    prsRejected: 2,
    successfulBuilds: 48,
    failedBuilds: 3,
    githubAccountCreatedAt: "2021-03-10T00:00:00Z",
    createdAt: "2026-01-15T00:00:00Z",
    trustScore: {
      id: "ts-1",
      contributorId: "contrib-1",
      overallScore: 92.4,
      prAcceptanceRate: 92.8,
      buildSuccessRate: 94.1,
      reviewQualityScore: 90.0,
      accountAgeDays: 1662,
      riskTier: "TRUSTED",
      calculatedAt: "2026-09-27T09:00:00Z"
    }
  },
  {
    id: "contrib-2",
    repositoryId: "repo-1",
    username: "newbie-coder",
    avatarUrl: "https://avatars.githubusercontent.com/u/987654?v=4",
    totalPrsSubmitted: 3,
    prsMerged: 1,
    prsRejected: 2,
    successfulBuilds: 2,
    failedBuilds: 5,
    githubAccountCreatedAt: "2026-09-20T00:00:00Z",
    createdAt: "2026-09-20T00:00:00Z",
    trustScore: {
      id: "ts-2",
      contributorId: "contrib-2",
      overallScore: 42.1,
      prAcceptanceRate: 33.3,
      buildSuccessRate: 28.5,
      reviewQualityScore: 50.0,
      accountAgeDays: 7,
      riskTier: "HIGH_RISK",
      calculatedAt: "2026-09-27T08:15:00Z"
    }
  }
];

export const MOCK_DASHBOARD_SUMMARY = {
  totalRepositories: 3,
  totalPullRequests: 44,
  totalIssues: 28,
  totalContributors: 14,
  passRatePercentage: 88.5,
  metrics: {
    passedValidations: 39,
    failedValidations: 5,
    duplicateIssuesDetected: 6,
    highRiskContributors: 1
  }
};
