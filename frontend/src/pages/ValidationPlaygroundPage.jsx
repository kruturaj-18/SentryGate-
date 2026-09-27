import React, { useState } from 'react';
import { Sparkles, Play, CheckCircle2, XCircle, Sliders, Code2, AlertTriangle } from 'lucide-react';

export default function ValidationPlaygroundPage() {
  const [prTitle, setPrTitle] = useState('feat(auth): add multi-factor authentication fallback');
  const [prBody, setPrBody] = useState('This PR implements TOTP 2FA code fallback verification for user security. Fixes #102.');
  const [branchName, setBranchName] = useState('feature/totp-auth');
  const [filesCount, setFilesCount] = useState(5);
  const [additions, setAdditions] = useState(140);
  const [deletions, setDeletions] = useState(15);
  const [hasTests, setHasTests] = useState(true);
  const [hasSecrets, setHasSecrets] = useState(false);

  const [testResult, setTestResult] = useState(null);
  const [isRunning, setIsRunning] = useState(false);

  const runTestBench = () => {
    setIsRunning(true);
    setTimeout(() => {
      // Simulate validation rules
      const rules = [
        {
          code: 'PR_TITLE_FORMAT',
          name: 'PR Title Format',
          passed: /^ (feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9-_]+\))?: .+/i.test(prTitle),
          message: /^ (feat|fix|docs|style|refactor|perf|test|build|ci|chore|revert)(\([a-z0-9-_]+\))?: .+/i.test(prTitle)
            ? 'Valid conventional commit format'
            : 'Title must follow format: type(scope): summary (e.g. feat(auth): add login)'
        },
        {
          code: 'PR_DESCRIPTION_LENGTH',
          name: 'PR Description Length',
          passed: prBody.length >= 20,
          message: prBody.length >= 20
            ? `Description length (${prBody.length} chars) satisfies min 20 requirement`
            : `Description length (${prBody.length} chars) is below required 20 characters`
        },
        {
          code: 'BRANCH_NAMING',
          name: 'Branch Naming Convention',
          passed: /^(feature|feat|fix|bugfix|hotfix|refactor|release)\/.+/i.test(branchName),
          message: /^(feature|feat|fix|bugfix|hotfix|refactor|release)\/.+/i.test(branchName)
            ? `Branch prefix '${branchName.split('/')[0]}' is compliant`
            : 'Branch name must start with feature/, fix/, bugfix/, or refactor/'
        },
        {
          code: 'SECRET_DETECTION',
          name: 'Hardcoded Secret Scanner',
          passed: !hasSecrets,
          message: !hasSecrets
            ? 'No high-entropy secrets or private tokens detected'
            : 'DANGER: Hardcoded AWS API key / RSA private key pattern detected!'
        },
        {
          code: 'CHANGE_SIZE',
          name: 'Change Size Boundary',
          passed: filesCount <= 30 && (additions + deletions) <= 1000,
          message: (filesCount <= 30 && (additions + deletions) <= 1000)
            ? `Changeset (${filesCount} files, ${additions + deletions} lines) is within safe boundary`
            : `PR exceeds limit (${filesCount}/30 files, ${additions + deletions}/1000 lines)`
        },
        {
          code: 'TEST_PRESENCE',
          name: 'Test File Presence',
          passed: hasTests,
          message: hasTests
            ? 'Unit/Integration test files included in PR'
            : 'No test files detected in diff — tests are mandatory'
        }
      ];

      const passedAll = rules.every(r => r.passed);
      const score = Math.round(rules.filter(r => r.passed).length / rules.length * 100);

      setTestResult({ passedAll, score, rules });
      setIsRunning(false);
    }, 400);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
      
      {/* Header */}
      <div>
        <h2 style={{ fontSize: '1.4rem' }}>Interactive Quality Gate Test Bench</h2>
        <p style={{ color: 'var(--text-muted)', fontSize: '0.875rem' }}>
          Simulate and test SentryGate validation rules against sample pull request payloads
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px' }}>
        
        {/* Input Panel */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h3 style={{ fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Sliders style={{ width: '18px', height: '18px', color: '#6366f1' }} />
            PR Payload Inputs
          </h3>

          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, display: 'block', marginBottom: '6px' }}>
              PR Title
            </label>
            <input
              type="text"
              value={prTitle}
              onChange={e => setPrTitle(e.target.value)}
              style={{
                width: '100%', background: 'var(--bg-input)', color: '#fff',
                border: '1px solid var(--border-color)', padding: '10px', borderRadius: '8px',
                fontSize: '0.875rem', fontFamily: 'var(--font-mono)'
              }}
            />
          </div>

          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, display: 'block', marginBottom: '6px' }}>
              PR Body Description
            </label>
            <textarea
              rows={3}
              value={prBody}
              onChange={e => setPrBody(e.target.value)}
              style={{
                width: '100%', background: 'var(--bg-input)', color: '#fff',
                border: '1px solid var(--border-color)', padding: '10px', borderRadius: '8px',
                fontSize: '0.85rem'
              }}
            />
          </div>

          <div>
            <label style={{ fontSize: '0.8rem', color: 'var(--text-muted)', fontWeight: 600, display: 'block', marginBottom: '6px' }}>
              Source Branch Name
            </label>
            <input
              type="text"
              value={branchName}
              onChange={e => setBranchName(e.target.value)}
              style={{
                width: '100%', background: 'var(--bg-input)', color: '#fff',
                border: '1px solid var(--border-color)', padding: '10px', borderRadius: '8px',
                fontSize: '0.875rem', fontFamily: 'var(--font-mono)'
              }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
            <div>
              <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Files Changed</label>
              <input
                type="number"
                value={filesCount}
                onChange={e => setFilesCount(Number(e.target.value))}
                style={{ width: '100%', background: 'var(--bg-input)', color: '#fff', border: '1px solid var(--border-color)', padding: '8px', borderRadius: '6px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Additions</label>
              <input
                type="number"
                value={additions}
                onChange={e => setAdditions(Number(e.target.value))}
                style={{ width: '100%', background: 'var(--bg-input)', color: '#fff', border: '1px solid var(--border-color)', padding: '8px', borderRadius: '6px' }}
              />
            </div>
            <div>
              <label style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Deletions</label>
              <input
                type="number"
                value={deletions}
                onChange={e => setDeletions(Number(e.target.value))}
                style={{ width: '100%', background: 'var(--bg-input)', color: '#fff', border: '1px solid var(--border-color)', padding: '8px', borderRadius: '6px' }}
              />
            </div>
          </div>

          <div style={{ display: 'flex', gap: '16px', marginTop: '4px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.825rem', cursor: 'pointer' }}>
              <input type="checkbox" checked={hasTests} onChange={e => setHasTests(e.target.checked)} />
              Include Test Files
            </label>
            <label style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '0.825rem', color: '#fb7185', cursor: 'pointer' }}>
              <input type="checkbox" checked={hasSecrets} onChange={e => setHasSecrets(e.target.checked)} />
              Simulate Secret Leak
            </label>
          </div>

          <button className="btn btn-primary" onClick={runTestBench} disabled={isRunning} style={{ marginTop: '10px' }}>
            <Play style={{ width: '16px', height: '16px' }} />
            {isRunning ? 'Evaluating Rules...' : 'Run Gate Test Bench'}
          </button>
        </div>

        {/* Evaluation Output Panel */}
        <div className="glass-panel" style={{ padding: '24px', display: 'flex', flexDirection: 'column', gap: '16px' }}>
          <h3 style={{ fontSize: '1.1rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Sparkles style={{ width: '18px', height: '18px', color: '#06b6d4' }} />
            Gate Simulation Results
          </h3>

          {testResult ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div style={{
                background: testResult.passedAll ? 'rgba(16,185,129,0.1)' : 'rgba(244,63,94,0.1)',
                border: `1px solid ${testResult.passedAll ? 'rgba(16,185,129,0.3)' : 'rgba(244,63,94,0.3)'}`,
                padding: '16px', borderRadius: '12px', display: 'flex', justifyContent: 'space-between', alignItems: 'center'
              }}>
                <div>
                  <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Gate Outcome</span>
                  <div style={{ fontSize: '1.3rem', fontWeight: 800, color: testResult.passedAll ? '#34d399' : '#fb7185' }}>
                    {testResult.passedAll ? '✅ GATE PASSED' : '❌ GATE REJECTED'}
                  </div>
                </div>
                <div style={{ fontSize: '1.6rem', fontWeight: 800, color: testResult.passedAll ? '#34d399' : '#fb7185' }}>
                  {testResult.score}%
                </div>
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {testResult.rules.map(r => (
                  <div key={r.code} style={{
                    padding: '12px', borderRadius: '10px',
                    background: 'rgba(255,255,255,0.02)',
                    borderLeft: `3px solid ${r.passed ? '#10b981' : '#f43f5e'}`
                  }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontWeight: 700, fontSize: '0.85rem' }}>{r.name}</span>
                      <span className={`badge ${r.passed ? 'badge-success' : 'badge-danger'}`}>
                        {r.passed ? 'PASS' : 'FAIL'}
                      </span>
                    </div>
                    <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: '4px' }}>{r.message}</p>
                  </div>
                ))}
              </div>
            </div>
          ) : (
            <div style={{ textAlign: 'center', padding: '48px 16px', color: 'var(--text-muted)' }}>
              <Code2 style={{ width: '36px', height: '36px', color: 'var(--text-subtle)', margin: '0 auto 12px' }} />
              <p>Click "Run Gate Test Bench" to simulate SentryGate validation engine execution</p>
            </div>
          )}

        </div>

      </div>

    </div>
  );
}
