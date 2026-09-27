// API Service for SentryGate Frontend

const API_BASE = '/api/v1';

async function fetchJson(endpoint, options = {}) {
  const token = localStorage.getItem('sentrygate_jwt');
  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    if (!res.ok) {
      const errorData = await res.json().catch(() => ({ message: res.statusText }));
      throw new Error(errorData.message || `Request failed with status ${res.status}`);
    }
    return await res.json();
  } catch (err) {
    console.warn(`API Request Error [${endpoint}]:`, err.message);
    throw err;
  }
}

export const api = {
  // Auth
  githubLogin: (accessToken) => fetchJson('/auth/github', { method: 'POST', body: JSON.stringify({ accessToken }) }),
  getCurrentUser: () => fetchJson('/auth/me'),

  // Repositories
  getRepositories: () => fetchJson('/repositories'),
  syncRepositories: () => fetchJson('/repositories/sync', { method: 'POST' }),
  getRepository: (id) => fetchJson(`/repositories/${id}`),
  toggleQualityGate: (id, enabled) => fetchJson(`/repositories/${id}/toggle-gate?enabled=${enabled}`, { method: 'PUT' }),
  getRepositoryRules: (id) => fetchJson(`/repositories/${id}/rules`),
  updateRepositoryRule: (id, ruleCode, enabled, config) => fetchJson(`/repositories/${id}/rules`, {
    method: 'PUT',
    body: JSON.stringify({ ruleCode, enabled, config })
  }),

  // Pull Requests & Validation
  getPullRequests: (repoId) => fetchJson(`/pull-requests/repo/${repoId}`),
  getPullRequest: (id) => fetchJson(`/pull-requests/${id}`),
  validatePullRequest: (id) => fetchJson(`/pull-requests/${id}/validate`, { method: 'POST' }),
  getValidationHistory: (id) => fetchJson(`/pull-requests/${id}/validations`),

  // Issues & Duplicate Detection
  getIssues: (repoId) => fetchJson(`/issues/repo/${repoId}`),
  getIssue: (id) => fetchJson(`/issues/${id}`),

  // Contributors & Trust Scoring
  getContributors: (repoId) => fetchJson(`/contributors/repo/${repoId}`),
  getContributor: (id) => fetchJson(`/contributors/${id}`),
  calculateTrustScore: (id) => fetchJson(`/contributors/${id}/calculate-trust`, { method: 'POST' }),
  getTrustHistory: (id) => fetchJson(`/contributors/${id}/trust-history`),

  // Dashboard & Analytics
  getDashboardSummary: () => fetchJson('/dashboard/summary'),

  // Notifications
  getNotifications: () => fetchJson('/notifications'),
  markNotificationRead: (id) => fetchJson(`/notifications/${id}/read`, { method: 'PUT' }),
};
