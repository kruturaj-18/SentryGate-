import React, { useState } from 'react';
import { GitFork, Shield, RefreshCw, Sliders, CheckCircle2, ToggleLeft, ToggleRight, Info } from 'lucide-react';

export default function RepositoriesPage({ repos, onToggleGate, onSync }) {
  const [selectedRepoRules, setSelectedRepoRules] = useState(null);
  const [isSyncing, setIsSyncing] = useState(false);

  const defaultRules = [
    { code: 'PR_TITLE_FORMAT', name: 'PR Title Convention', desc: 'Enforces Conventional Commits format (e.g., feat(scope): desc)', enabled: true },
    { code: 'PR_DESCRIPTION_LENGTH', name: 'PR Description Length', desc: 'Requires at least 20 characters of detailed explanation', enabled: true },
    { code: 'BRANCH_NAMING', name: 'Branch Naming Pattern', desc: 'Validates branch prefix (feature/*, fix/*, refactor/*)', enabled: true },
    { code: 'SECRET_DETECTION', name: 'Hardcoded Secret Scanner', desc: 'Blocks API keys, private RSA keys, and passwords', enabled: true },
    { code: 'CHANGE_SIZE', name: 'Max Diff Size Boundary', desc: 'Flags large PRs exceeding 30 files or 1,000 lines changed', enabled: true },
    { code: 'TEST_PRESENCE', name: 'Test File Presence', desc: 'Requires corresponding unit or integration test files', enabled: true },
    { code: 'STATIC_ANALYSIS', name: 'AST Static Code Quality', desc: 'Scans for anti-patterns and unused variables', enabled: true },
    { code: 'REQUIRED_FILES', name: 'Required Files Check', desc: 'Verifies existence of README.md, LICENSE, and CONTRIBUTING.md', enabled: true },
  ];

  const handleSyncClick = async () => {
    setIsSyncing(true);
    await onSync();
    setTimeout(() => setIsSyncing(false), 800);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h2 style={{ fontSize: '1.4rem' }}>Monitored Repositories & Quality Gates</h2>
          <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
            Configure active validation rules, toggle quality gates, and sync with GitHub
          </p>
        </div>
        <button className="btn btn-primary" onClick={handleSyncClick} disabled={isSyncing}>
          <RefreshCw style={{ width: '16px', height: '16px', animation: isSyncing ? 'spin 1s linear infinite' : 'none' }} />
          {isSyncing ? 'Syncing...' : 'Sync GitHub Repos'}
        </button>
      </div>

      {/* Repositories Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '20px' }}>
        {repos.map(repo => (
          <div key={repo.id} className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', justifyContent: 'space-between', gap: '16px' }}>
            <div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <div style={{ padding: '10px', borderRadius: '10px', background: 'rgba(99,102,241,0.12)', border: '1px solid rgba(99,102,241,0.3)' }}>
                    <GitFork style={{ color: '#6366f1', width: '20px', height: '20px' }} />
                  </div>
                  <div>
                    <h3 style={{ fontSize: '1.05rem' }}>{repo.name}</h3>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{repo.fullName}</span>
                  </div>
                </div>
                <span className="badge badge-info" style={{ fontSize: '0.65rem' }}>{repo.defaultBranch}</span>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginTop: '16px', fontSize: '0.85rem' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Quality Gate Engine</span>
                  <button
                    onClick={() => onToggleGate(repo.id, !repo.qualityGateEnabled)}
                    style={{ background: 'none', border: 'none', cursor: 'pointer', display: 'flex', alignItems: 'center' }}
                  >
                    {repo.qualityGateEnabled ? (
                      <ToggleRight style={{ width: '32px', height: '32px', color: '#10b981' }} />
                    ) : (
                      <ToggleLeft style={{ width: '32px', height: '32px', color: 'var(--text-subtle)' }} />
                    )}
                  </button>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Duplicate Issue Detection</span>
                  <span style={{ color: '#34d399', fontWeight: 600 }}>Active</span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <span style={{ color: 'var(--text-muted)' }}>Contributor Trust Scoring</span>
                  <span style={{ color: '#34d399', fontWeight: 600 }}>Active</span>
                </div>
              </div>
            </div>

            <button className="btn btn-secondary btn-sm" style={{ width: '100%' }} onClick={() => setSelectedRepoRules(repo)}>
              <Sliders style={{ width: '14px', height: '14px' }} />
              Configure Validation Rules (8 Active)
            </button>
          </div>
        ))}
      </div>

      {/* Rules Config Modal */}
      {selectedRepoRules && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.75)', backdropFilter: 'blur(8px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100
        }}>
          <div className="glass-panel animate-fade-in" style={{ width: '560px', maxWidth: '90vw', maxHeight: '85vh', overflowY: 'auto', padding: '28px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <div>
                <h3 style={{ fontSize: '1.2rem' }}>Quality Rules — {selectedRepoRules.name}</h3>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Enable or disable specific automated checks for this repo</p>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => setSelectedRepoRules(null)}>Close</button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {defaultRules.map(rule => (
                <div key={rule.code} style={{
                  padding: '14px', borderRadius: '10px',
                  background: 'rgba(255,255,255,0.03)', border: '1px solid var(--border-color)',
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                }}>
                  <div>
                    <h4 style={{ fontSize: '0.9rem', marginBottom: '2px' }}>{rule.name}</h4>
                    <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>{rule.desc}</p>
                  </div>
                  <span className="badge badge-success">Enabled</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
