import React, { useState, useEffect } from 'react';
import Navbar from './components/Navbar';
import Sidebar from './components/Sidebar';
import DashboardPage from './pages/DashboardPage';
import RepositoriesPage from './pages/RepositoriesPage';
import PullRequestsPage from './pages/PullRequestsPage';
import IssueDuplicatesPage from './pages/IssueDuplicatesPage';
import ContributorTrustPage from './pages/ContributorTrustPage';
import ValidationPlaygroundPage from './pages/ValidationPlaygroundPage';

import { api } from './services/api';
import { 
  MOCK_REPOSITORIES, 
  MOCK_PULL_REQUESTS, 
  MOCK_ISSUES, 
  MOCK_CONTRIBUTORS, 
  MOCK_DASHBOARD_SUMMARY 
} from './services/mockData';

export default function App() {
  const [activeTab, setActiveTab] = useState('dashboard');
  const [repos, setRepos] = useState(MOCK_REPOSITORIES);
  const [selectedRepo, setSelectedRepo] = useState(MOCK_REPOSITORIES[0]);
  const [pullRequests, setPullRequests] = useState(MOCK_PULL_REQUESTS);
  const [issues, setIssues] = useState(MOCK_ISSUES);
  const [contributors, setContributors] = useState(MOCK_CONTRIBUTORS);
  const [dashboardSummary, setDashboardSummary] = useState(MOCK_DASHBOARD_SUMMARY);

  // Attempt to load live backend data on mount, fallback to mock data seamlessly
  useEffect(() => {
    async function loadData() {
      try {
        const liveRepos = await api.getRepositories();
        if (liveRepos && liveRepos.length > 0) {
          setRepos(liveRepos);
          setSelectedRepo(liveRepos[0]);
        }
      } catch (e) {
        console.log("Backend offline or not logged in, operating in interactive fallback mode.");
      }
    }
    loadData();
  }, []);

  const handleToggleGate = async (repoId, enabled) => {
    setRepos(prev => prev.map(r => r.id === repoId ? { ...r, qualityGateEnabled: enabled } : r));
    try {
      await api.toggleQualityGate(repoId, enabled);
    } catch (e) {
      // Handled silently
    }
  };

  const handleSyncRepos = async () => {
    try {
      const synced = await api.syncRepositories();
      if (synced && synced.length > 0) setRepos(synced);
    } catch (e) {
      // Mock refresh
      setRepos([...MOCK_REPOSITORIES]);
    }
  };

  const handleValidatePr = async (prId) => {
    try {
      await api.validatePullRequest(prId);
    } catch (e) {
      // Mock validation toggle
      setPullRequests(prev => prev.map(pr => pr.id === prId ? { ...pr, validationStatus: pr.validationStatus === 'PASSED' ? 'FAILED' : 'PASSED' } : pr));
    }
  };

  const handleRecalculateTrust = async (contributorId) => {
    try {
      await api.calculateTrustScore(contributorId);
    } catch (e) {
      setContributors(prev => prev.map(c => c.id === contributorId ? {
        ...c,
        trustScore: { ...c.trustScore, overallScore: Math.min(100, (c.trustScore?.overallScore || 50) + 5) }
      } : c));
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      {/* Top Navbar */}
      <Navbar
        selectedRepo={selectedRepo}
        onSelectRepo={setSelectedRepo}
        repos={repos}
      />

      {/* Main Layout Container */}
      <div style={{ display: 'flex', flex: 1 }}>
        
        {/* Left Drawer / Sidebar */}
        <Sidebar activeTab={activeTab} setActiveTab={setActiveTab} />

        {/* Content Area */}
        <main style={{ flex: 1, padding: '32px 40px', maxWidth: '1400px', margin: '0 auto', width: '100%' }}>
          {activeTab === 'dashboard' && (
            <DashboardPage
              summary={dashboardSummary}
              pullRequests={pullRequests}
              onNavigate={setActiveTab}
            />
          )}

          {activeTab === 'repositories' && (
            <RepositoriesPage
              repos={repos}
              onToggleGate={handleToggleGate}
              onSync={handleSyncRepos}
            />
          )}

          {activeTab === 'pull-requests' && (
            <PullRequestsPage
              pullRequests={pullRequests}
              onValidatePr={handleValidatePr}
            />
          )}

          {activeTab === 'issue-duplicates' && (
            <IssueDuplicatesPage
              issues={issues}
            />
          )}

          {activeTab === 'contributor-trust' && (
            <ContributorTrustPage
              contributors={contributors}
              onRecalculateTrust={handleRecalculateTrust}
            />
          )}

          {activeTab === 'playground' && (
            <ValidationPlaygroundPage />
          )}
        </main>

      </div>

    </div>
  );
}
