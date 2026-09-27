import React, { useState } from 'react';
import { Users, ShieldCheck, ShieldAlert, Award, Calendar, GitCommit, CheckCircle2 } from 'lucide-react';

export default function ContributorTrustPage({ contributors, onRecalculateTrust }) {
  const [calculatingId, setCalculatingId] = useState(null);

  const handleRecalculate = async (id) => {
    setCalculatingId(id);
    await onRecalculateTrust(id);
    setTimeout(() => setCalculatingId(null), 600);
  };

  const getTierBadge = (tier) => {
    if (tier === 'TRUSTED') return <span className="badge badge-success"><ShieldCheck style={{ width: '12px', height: '12px' }} /> TRUSTED</span>;
    if (tier === 'HIGH_RISK') return <span className="badge badge-danger"><ShieldAlert style={{ width: '12px', height: '12px' }} /> HIGH RISK</span>;
    return <span className="badge badge-warning">NEUTRAL</span>;
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '1.4rem' }}>Contributor Trust Intelligence</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          Dynamic risk scoring based on PR acceptance rate, build reliability, and account maturity
        </p>
      </div>

      {/* Contributor Cards Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(340px, 1fr))', gap: '20px' }}>
        {contributors.map(c => {
          const score = c.trustScore || {};
          return (
            <div key={c.id} className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
              
              {/* Header Profile */}
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <img src={c.avatarUrl} alt="" style={{ width: '44px', height: '44px', borderRadius: '50%', border: '2px solid var(--border-color)' }} />
                  <div>
                    <h3 style={{ fontSize: '1.1rem' }}>@{c.username}</h3>
                    <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Account age: {score.accountAgeDays || 365} days</span>
                  </div>
                </div>
                {getTierBadge(score.riskTier || 'NEUTRAL')}
              </div>

              {/* Score Display */}
              <div style={{
                background: 'rgba(0,0,0,0.3)', padding: '16px', borderRadius: '12px',
                display: 'flex', justifyContent: 'space-between', alignItems: 'center'
              }}>
                <div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Overall Trust Rating</span>
                  <div style={{ fontSize: '1.6rem', fontWeight: 800, color: (score.overallScore || 0) >= 80 ? '#34d399' : '#fb7185' }}>
                    {score.overallScore || 50}/100
                  </div>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '4px', textAlign: 'right', fontSize: '0.75rem', color: 'var(--text-muted)' }}>
                  <span>PR Acceptance: <strong style={{ color: '#fff' }}>{score.prAcceptanceRate || 0}%</strong></span>
                  <span>Build Success: <strong style={{ color: '#fff' }}>{score.buildSuccessRate || 0}%</strong></span>
                </div>
              </div>

              {/* Stats Bar */}
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '8px', textAlign: 'center', fontSize: '0.75rem' }}>
                <div style={{ background: 'rgba(255,255,255,0.03)', padding: '8px', borderRadius: '8px' }}>
                  <span style={{ color: 'var(--text-subtle)', display: 'block' }}>PRs Submitted</span>
                  <strong style={{ fontSize: '0.95rem' }}>{c.totalPrsSubmitted}</strong>
                </div>
                <div style={{ background: 'rgba(255,255,255,0.03)', padding: '8px', borderRadius: '8px' }}>
                  <span style={{ color: 'var(--text-subtle)', display: 'block' }}>PRs Merged</span>
                  <strong style={{ fontSize: '0.95rem', color: '#34d399' }}>{c.prsMerged}</strong>
                </div>
                <div style={{ background: 'rgba(255,255,255,0.03)', padding: '8px', borderRadius: '8px' }}>
                  <span style={{ color: 'var(--text-subtle)', display: 'block' }}>PRs Rejected</span>
                  <strong style={{ fontSize: '0.95rem', color: '#fb7185' }}>{c.prsRejected}</strong>
                </div>
              </div>

              <button
                className="btn btn-secondary btn-sm"
                onClick={() => handleRecalculate(c.id)}
                disabled={calculatingId === c.id}
                style={{ width: '100%', marginTop: 'auto' }}
              >
                {calculatingId === c.id ? 'Recalculating...' : 'Recalculate Trust Score'}
              </button>

            </div>
          );
        })}
      </div>

    </div>
  );
}
