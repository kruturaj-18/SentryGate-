import React, { useState } from 'react';
import { GitPullRequest, CheckCircle2, XCircle, AlertTriangle, Eye, RefreshCw, FileText, ArrowRight } from 'lucide-react';
import { MOCK_VALIDATION_RESULTS } from '../services/mockData';

export default function PullRequestsPage({ pullRequests, onValidatePr }) {
  const [selectedPrValidation, setSelectedPrValidation] = useState(null);
  const [validatingId, setValidatingId] = useState(null);

  const handleInspect = (pr) => {
    const val = MOCK_VALIDATION_RESULTS[pr.id] || {
      id: "val-default",
      pullRequestId: pr.id,
      passed: pr.validationStatus === 'PASSED',
      score: pr.validationStatus === 'PASSED' ? 95 : 38,
      summary: `Evaluated 8 rules: ${pr.validationStatus}`,
      evaluatedAt: pr.createdAt,
      results: [
        { id: "r1", ruleCode: "PR_TITLE_FORMAT", passed: pr.validationStatus === 'PASSED', score: pr.validationStatus === 'PASSED' ? 100 : 0, message: pr.validationStatus === 'PASSED' ? 'Valid Conventional Commit title' : 'Invalid title format' },
        { id: "r2", ruleCode: "PR_DESCRIPTION_LENGTH", passed: true, score: 100, message: 'Description exceeds min length' },
        { id: "r3", ruleCode: "BRANCH_NAMING", passed: pr.validationStatus === 'PASSED', score: pr.validationStatus === 'PASSED' ? 100 : 0, message: pr.validationStatus === 'PASSED' ? 'Branch pattern matches' : 'Non-standard branch prefix' },
        { id: "r4", ruleCode: "SECRET_DETECTION", passed: true, score: 100, message: 'No hardcoded credentials found' },
        { id: "r5", ruleCode: "CHANGE_SIZE", passed: pr.validationStatus === 'PASSED', score: pr.validationStatus === 'PASSED' ? 90 : 0, message: pr.validationStatus === 'PASSED' ? 'Diff size within bounds' : 'Exceeds max lines boundary' },
        { id: "r6", ruleCode: "TEST_PRESENCE", passed: pr.validationStatus === 'PASSED', score: pr.validationStatus === 'PASSED' ? 100 : 0, message: pr.validationStatus === 'PASSED' ? 'Tests included' : 'No tests found in changeset' }
      ]
    };
    setSelectedPrValidation({ pr, validation: val });
  };

  const handleRunValidation = async (pr) => {
    setValidatingId(pr.id);
    await onValidatePr(pr.id);
    setTimeout(() => {
      setValidatingId(null);
      handleInspect(pr);
    }, 600);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '1.4rem' }}>Pull Request Quality Gates</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          Inspect automated gate checks, security scans, and change size analysis per pull request
        </p>
      </div>

      {/* PR Table */}
      <div className="glass-panel" style={{ padding: '24px' }}>
        <div style={{ overflowX: 'auto' }}>
          <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '0.875rem' }}>
            <thead>
              <tr style={{ borderBottom: '1px solid var(--border-color)', color: 'var(--text-subtle)' }}>
                <th style={{ padding: '12px' }}>PR #</th>
                <th style={{ padding: '12px' }}>Pull Request Title</th>
                <th style={{ padding: '12px' }}>Author</th>
                <th style={{ padding: '12px' }}>Branch</th>
                <th style={{ padding: '12px' }}>Gate Status</th>
                <th style={{ padding: '12px' }}>Lines Changed</th>
                <th style={{ padding: '12px', textAlign: 'right' }}>Action</th>
              </tr>
            </thead>
            <tbody>
              {pullRequests.map(pr => (
                <tr key={pr.id} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)' }}>
                  <td style={{ padding: '14px 12px' }}>
                    <span className="code-pill">#{pr.prNumber}</span>
                  </td>
                  <td style={{ padding: '14px 12px', fontWeight: 600 }}>{pr.title}</td>
                  <td style={{ padding: '14px 12px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <img src={pr.authorAvatarUrl} alt="" style={{ width: '24px', height: '24px', borderRadius: '50%' }} />
                      <span style={{ fontSize: '0.825rem', color: 'var(--text-muted)' }}>@{pr.authorUsername}</span>
                    </div>
                  </td>
                  <td style={{ padding: '14px 12px' }}>
                    <span className="code-pill" style={{ color: 'var(--accent-cyan)' }}>{pr.sourceBranch}</span>
                  </td>
                  <td style={{ padding: '14px 12px' }}>
                    <span className={`badge ${pr.validationStatus === 'PASSED' ? 'badge-success' : 'badge-danger'}`}>
                      {pr.validationStatus === 'PASSED' ? <CheckCircle2 style={{ width: '12px', height: '12px' }} /> : <XCircle style={{ width: '12px', height: '12px' }} />}
                      {pr.validationStatus}
                    </span>
                  </td>
                  <td style={{ padding: '14px 12px', fontSize: '0.8rem' }}>
                    <span style={{ color: '#34d399', fontWeight: 600 }}>+{pr.additions}</span> / <span style={{ color: '#fb7185', fontWeight: 600 }}>-{pr.deletions}</span>
                  </td>
                  <td style={{ padding: '14px 12px', textAlign: 'right' }}>
                    <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
                      <button className="btn btn-secondary btn-sm" onClick={() => handleInspect(pr)}>
                        <Eye style={{ width: '14px', height: '14px' }} />
                        Inspect Gate
                      </button>
                      <button className="btn btn-primary btn-sm" onClick={() => handleRunValidation(pr)} disabled={validatingId === pr.id}>
                        <RefreshCw style={{ width: '14px', height: '14px', animation: validatingId === pr.id ? 'spin 1s linear infinite' : 'none' }} />
                        Re-run Gate
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* PR Validation Inspection Modal */}
      {selectedPrValidation && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, bottom: 0,
          background: 'rgba(0,0,0,0.8)', backdropFilter: 'blur(8px)',
          display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 100
        }}>
          <div className="glass-panel animate-fade-in" style={{ width: '680px', maxWidth: '92vw', maxHeight: '90vh', overflowY: 'auto', padding: '32px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' }}>
              <div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <span className="code-pill">#{selectedPrValidation.pr.prNumber}</span>
                  <h3 style={{ fontSize: '1.25rem' }}>{selectedPrValidation.pr.title}</h3>
                </div>
                <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)', marginTop: '4px' }}>
                  Branch <span className="code-pill">{selectedPrValidation.pr.sourceBranch}</span> into <span className="code-pill">{selectedPrValidation.pr.targetBranch}</span>
                </p>
              </div>
              <button className="btn btn-secondary btn-sm" onClick={() => setSelectedPrValidation(null)}>Close</button>
            </div>

            {/* Overall Gate Score Pill */}
            <div style={{
              background: selectedPrValidation.validation.passed ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
              border: `1px solid ${selectedPrValidation.validation.passed ? 'rgba(16,185,129,0.3)' : 'rgba(244,63,94,0.3)'}`,
              padding: '16px', borderRadius: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '24px'
            }}>
              <div>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600 }}>Overall Quality Result</span>
                <div style={{ fontSize: '1.4rem', fontWeight: 800, color: selectedPrValidation.validation.passed ? '#34d399' : '#fb7185', marginTop: '2px' }}>
                  {selectedPrValidation.validation.passed ? '✅ PASSED — GATE APPROVED' : '❌ FAILED — GATE REJECTED'}
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>Score</span>
                <div style={{ fontSize: '1.8rem', fontWeight: 800, color: selectedPrValidation.validation.passed ? '#34d399' : '#fb7185' }}>
                  {selectedPrValidation.validation.score}/100
                </div>
              </div>
            </div>

            {/* Individual Rule Breakdown */}
            <h4 style={{ fontSize: '1rem', marginBottom: '14px' }}>Evaluated Rule Breakdown</h4>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              {selectedPrValidation.validation.results.map(r => (
                <div key={r.id} style={{
                  padding: '12px 16px', borderRadius: '10px',
                  background: 'rgba(255,255,255,0.02)',
                  borderLeft: `4px solid ${r.passed ? '#10b981' : '#f43f5e'}`,
                  display: 'flex', justifyContent: 'space-between', alignItems: 'center'
                }}>
                  <div>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <span style={{ fontWeight: 700, fontSize: '0.875rem' }}>{r.ruleCode}</span>
                      <span className={`badge ${r.passed ? 'badge-success' : 'badge-danger'}`} style={{ fontSize: '0.625rem' }}>
                        {r.passed ? 'PASS' : 'FAIL'}
                      </span>
                    </div>
                    <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>{r.message}</p>
                  </div>
                  <span style={{ fontWeight: 700, fontSize: '0.9rem', color: r.passed ? '#34d399' : '#fb7185' }}>
                    {r.score} pt
                  </span>
                </div>
              ))}
            </div>

          </div>
        </div>
      )}

    </div>
  );
}
