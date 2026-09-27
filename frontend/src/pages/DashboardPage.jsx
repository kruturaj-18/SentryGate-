import React from 'react';
import { 
  GitPullRequest, 
  CheckCircle2, 
  XCircle, 
  GitFork, 
  Users, 
  AlertOctagon, 
  TrendingUp, 
  ArrowUpRight,
  ShieldAlert,
  Search
} from 'lucide-react';

export default function DashboardPage({ summary, pullRequests, onNavigate }) {
  const passedPrs = pullRequests.filter(pr => pr.validationStatus === 'PASSED').length;
  const failedPrs = pullRequests.filter(pr => pr.validationStatus === 'FAILED').length;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Top Banner */}
      <div className="glass-panel" style={{
        padding: '24px 32px',
        background: 'linear-gradient(135deg, rgba(99,102,241,0.15) 0%, rgba(6,182,212,0.1) 100%)',
        border: '1px solid rgba(99,102,241,0.3)',
        display: 'flex',
        justify: 'space-between',
        alignItems: 'center'
      }}>
        <div>
          <h2 style={{ fontSize: '1.6rem', marginBottom: '6px' }}>Quality Control Dashboard</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.9rem' }}>
            Real-time quality gates, automated pull request validation, and contributor trust intelligence.
          </p>
        </div>
        <button className="btn btn-primary" onClick={() => onNavigate('playground')}>
          <TrendingUp style={{ width: '18px', height: '18px' }} />
          Launch Test Bench
        </button>
      </div>

      {/* KPI Stat Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
        
        {/* Pass Rate */}
        <div className="glass-panel glass-panel-interactive" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Gate Pass Rate</span>
            <div style={{ background: 'rgba(16,185,129,0.15)', padding: '8px', borderRadius: '10px' }}>
              <CheckCircle2 style={{ color: '#10b981', width: '20px', height: '20px' }} />
            </div>
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#34d399' }}>
            {summary.passRatePercentage || 88.5}%
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', marginTop: '8px', fontSize: '0.75rem', color: '#10b981' }}>
            <TrendingUp style={{ width: '14px', height: '14px' }} />
            <span>+4.2% from last week</span>
          </div>
        </div>

        {/* Total Repos */}
        <div className="glass-panel glass-panel-interactive" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Monitored Repos</span>
            <div style={{ background: 'rgba(99,102,241,0.15)', padding: '8px', borderRadius: '10px' }}>
              <GitFork style={{ color: '#6366f1', width: '20px', height: '20px' }} />
            </div>
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800 }}>
            {summary.totalRepositories || 3}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', display: 'block', marginTop: '8px' }}>
            All quality gates active
          </span>
        </div>

        {/* PRs Validated */}
        <div className="glass-panel glass-panel-interactive" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Evaluated PRs</span>
            <div style={{ background: 'rgba(6,182,212,0.15)', padding: '8px', borderRadius: '10px' }}>
              <GitPullRequest style={{ color: '#06b6d4', width: '20px', height: '20px' }} />
            </div>
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800 }}>
            {summary.totalPullRequests || 44}
          </div>
          <div style={{ display: 'flex', gap: '8px', marginTop: '8px', fontSize: '0.75rem' }}>
            <span style={{ color: '#34d399', fontWeight: 600 }}>{passedPrs} Passed</span>
            <span style={{ color: 'var(--text-subtle)' }}>•</span>
            <span style={{ color: '#fb7185', fontWeight: 600 }}>{failedPrs} Failed</span>
          </div>
        </div>

        {/* Duplicate Issues */}
        <div className="glass-panel glass-panel-interactive" style={{ padding: '20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Duplicates Intercepted</span>
            <div style={{ background: 'rgba(245,158,11,0.15)', padding: '8px', borderRadius: '10px' }}>
              <Search style={{ color: '#f59e0b', width: '20px', height: '20px' }} />
            </div>
          </div>
          <div style={{ fontSize: '1.8rem', fontWeight: 800, color: '#fbbf24' }}>
            {summary.metrics?.duplicateIssuesDetected || 6}
          </div>
          <span style={{ fontSize: '0.75rem', color: 'var(--text-subtle)', display: 'block', marginTop: '8px' }}>
            Levenshtein & Jaccard AI algorithm
          </span>
        </div>

      </div>

      {/* Main Content Split (Recent PR Validations & Risk Matrix) */}
      <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr', gap: '20px' }}>
        
        {/* Recent PR Validations Table */}
        <div className="glass-panel" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
            <div>
              <h3 style={{ fontSize: '1.1rem' }}>Recent PR Gate Executions</h3>
              <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Latest pull requests evaluated by SentryGate quality rules</p>
            </div>
            <button className="btn btn-secondary btn-sm" onClick={() => onNavigate('pull-requests')}>
              View All PRs
              <ArrowUpRight style={{ width: '14px', height: '14px' }} />
            </button>
          </div>

          <div style={{ overflowX: 'auto' }}>
            <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
              <thead>
                <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)' }}>
                  <th style={{ padding: '10px 12px' }}>PR</th>
                  <th style={{ padding: '10px 12px' }}>Title</th>
                  <th style={{ padding: '10px 12px' }}>Author</th>
                  <th style={{ padding: '10px 12px' }}>Gate Status</th>
                  <th style={{ padding: '10px 12px' }}>Changes</th>
                </tr>
              </thead>
              <tbody>
                {pullRequests.map(pr => (
                  <tr key={pr.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                    <td style={{ padding: '12px' }}>
                      <span className="code-pill">#{pr.prNumber}</span>
                    </td>
                    <td style={{ padding: '12px', fontWeight: 600 }}>{pr.title}</td>
                    <td style={{ padding: '12px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                        <img src={pr.authorAvatarUrl} alt="" style={{ width: '22px', height: '22px', borderRadius: '50%' }} />
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>@{pr.authorUsername}</span>
                      </div>
                    </td>
                    <td style={{ padding: '12px' }}>
                      <span className={`badge ${pr.validationStatus === 'PASSED' ? 'badge-success' : 'badge-danger'}`}>
                        {pr.validationStatus === 'PASSED' ? <CheckCircle2 style={{ width: '12px', height: '12px' }} /> : <XCircle style={{ width: '12px', height: '12px' }} />}
                        {pr.validationStatus}
                      </span>
                    </td>
                    <td style={{ padding: '12px', fontSize: '0.8rem' }}>
                      <span style={{ color: '#34d399' }}>+{pr.additions}</span> / <span style={{ color: '#fb7185' }}>-{pr.deletions}</span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* Contributor Risk & Security Summary Card */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h3 style={{ fontSize: '1.1rem' }}>Contributor Risk Index</h3>

          <div style={{ background: 'rgba(244,63,94,0.08)', border: '1px solid rgba(244,63,94,0.2)', padding: '16px', borderRadius: '12px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '8px' }}>
              <ShieldAlert style={{ color: '#f43f5e', width: '20px', height: '20px' }} />
              <span style={{ fontWeight: 700, color: '#fb7185', fontSize: '0.9rem' }}>1 High-Risk Contributor</span>
            </div>
            <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
              Contributor @newbie-coder account age is &lt;7 days with 66.7% PR rejection rate.
            </p>
          </div>

          <div style={{ marginTop: '8px' }}>
            <span style={{ fontSize: '0.8rem', color: 'var(--text-subtle)', fontWeight: 600 }}>Active Rules Enforced</span>
            <ul style={{ listStyle: 'none', display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '10px' }}>
              <li style={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', fontSize: '0.825rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>• Secret Detection</span>
                <span className="badge badge-success" style={{ marginLeft: 'auto' }}>Active</span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', fontSize: '0.825rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>• Conventional PR Titles</span>
                <span className="badge badge-success" style={{ marginLeft: 'auto' }}>Active</span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', fontSize: '0.825rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>• Test Presence Enforcement</span>
                <span className="badge badge-success" style={{ marginLeft: 'auto' }}>Active</span>
              </li>
              <li style={{ display: 'flex', alignItems: 'center', justifyBetween: 'space-between', fontSize: '0.825rem' }}>
                <span style={{ color: 'var(--text-muted)' }}>• Max Change Size Boundary</span>
                <span className="badge badge-success" style={{ marginLeft: 'auto' }}>Active</span>
              </li>
            </ul>
          </div>

          <button className="btn btn-secondary btn-sm" style={{ marginTop: 'auto', width: '100%' }} onClick={() => onNavigate('contributor-trust')}>
            Manage Contributor Trust
          </button>
        </div>

      </div>

    </div>
  );
}
