import React, { useState } from 'react';
import { Search, AlertTriangle, Layers, ArrowRight, CheckCircle2, XCircle } from 'lucide-react';

export default function IssueDuplicatesPage({ issues }) {
  const [selectedIssue, setSelectedIssue] = useState(issues[0] || null);

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '1.4rem' }}>Automated Duplicate Issue Radar</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          TF-IDF, Levenshtein & Jaccard token similarity engine for issue deduplication
        </p>
      </div>

      {/* Grid Layout */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1.4fr', gap: '20px' }}>
        
        {/* Issues List */}
        <div className="glass-panel" style={{ padding: '20px' }}>
          <h3 style={{ fontSize: '1.05rem', marginBottom: '16px' }}>Open Repository Issues</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
            {issues.map(iss => (
              <div
                key={iss.id}
                onClick={() => setSelectedIssue(iss)}
                style={{
                  padding: '14px', borderRadius: '12px',
                  background: selectedIssue?.id === iss.id ? 'rgba(99,102,241,0.15)' : 'rgba(255,255,255,0.02)',
                  border: selectedIssue?.id === iss.id ? '1px solid #6366f1' : '1px solid var(--border-color)',
                  cursor: 'pointer',
                  transition: 'all 0.2s ease'
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                  <span className="code-pill">#{iss.issueNumber}</span>
                  {iss.hasDuplicateCandidates && (
                    <span className="badge badge-warning" style={{ fontSize: '0.65rem' }}>
                      <AlertTriangle style={{ width: '10px', height: '10px' }} />
                      Duplicate Flagged
                    </span>
                  )}
                </div>
                <h4 style={{ fontSize: '0.875rem', fontWeight: 600, color: 'var(--text-main)' }}>{iss.title}</h4>
                <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '4px', display: 'block' }}>
                  Submitted by @{iss.authorUsername}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Selected Issue & Duplicate Candidates Panel */}
        {selectedIssue && (
          <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '20px' }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                <span className="code-pill">#{selectedIssue.issueNumber}</span>
                <span className="badge badge-info">{selectedIssue.state}</span>
              </div>
              <h3 style={{ fontSize: '1.2rem', marginBottom: '8px' }}>{selectedIssue.title}</h3>
              <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)', background: 'rgba(0,0,0,0.3)', padding: '12px', borderRadius: '8px' }}>
                {selectedIssue.body}
              </p>
            </div>

            <div>
              <h4 style={{ fontSize: '1rem', marginBottom: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Search style={{ width: '16px', height: '16px', color: '#f59e0b' }} />
                Potential Duplicate Candidate Matches
              </h4>

              {selectedIssue.duplicateMatches && selectedIssue.duplicateMatches.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {selectedIssue.duplicateMatches.map(match => (
                    <div key={match.id} style={{
                      padding: '16px', borderRadius: '12px',
                      background: 'rgba(245,158,11,0.08)',
                      border: '1px solid rgba(245,158,11,0.3)'
                    }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <span className="code-pill">Match #{match.targetIssueNumber}</span>
                          <span style={{ fontWeight: 700, fontSize: '0.9rem' }}>{match.targetIssueTitle}</span>
                        </div>
                        <span className="badge badge-warning" style={{ fontSize: '0.75rem', fontWeight: 800 }}>
                          {(match.similarityScore * 100).toFixed(1)}% Match
                        </span>
                      </div>
                      <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginBottom: '12px' }}>
                        Algorithm flagged high semantic & title similarity.
                      </p>
                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button className="btn btn-secondary btn-sm">Link as Duplicate</button>
                        <button className="btn btn-danger btn-sm">Close Issue</button>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ textAlign: 'center', padding: '32px', color: 'var(--text-muted)' }}>
                  <CheckCircle2 style={{ width: '32px', height: '32px', color: '#10b981', margin: '0 auto 8px' }} />
                  <p>No duplicate candidates detected above similarity threshold (70%)</p>
                </div>
              )}
            </div>

          </div>
        )}

      </div>

    </div>
  );
}
