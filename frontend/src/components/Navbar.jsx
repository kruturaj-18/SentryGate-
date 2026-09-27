import React, { useState } from 'react';
import { Shield, Bell, Github, ChevronDown, CheckCircle2, AlertTriangle, Sparkles } from 'lucide-react';

export default function Navbar({ selectedRepo, onSelectRepo, repos }) {
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications] = useState([
    { id: 1, title: 'Quality Gate Failed', message: 'PR #43 failed change size & branch naming rules', time: '10m ago', unread: true },
    { id: 2, title: 'Duplicate Issue Flagged', message: 'Issue #104 has 88.5% similarity with Issue #88', time: '1h ago', unread: true },
    { id: 3, title: 'Contributor Trust Alert', message: 'User @newbie-coder flagged as HIGH_RISK', time: '2h ago', unread: false },
  ]);

  return (
    <header className="glass-panel" style={{
      borderRadius: '0',
      borderBottom: 'var(--glass-border)',
      borderTop: 'none', borderLeft: 'none', borderRight: 'none',
      padding: '12px 24px',
      position: 'sticky', top: 0, zIndex: 50
    }}>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
        
        {/* Logo & Brand */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '40px', height: '40px', borderRadius: '12px',
            background: 'linear-gradient(135deg, rgba(99,102,241,0.2) 0%, rgba(6,182,212,0.2) 100%)',
            border: '1px solid rgba(99,102,241,0.4)',
            display: 'flex', alignItems: 'center', justifyContent: 'center'
          }}>
            <Shield style={{ color: '#6366f1', width: '22px', height: '22px' }} />
          </div>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
              <span style={{ fontWeight: 800, fontSize: '1.2rem', background: 'linear-gradient(135deg, #fff 0%, #cbd5e1 100%)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
                RepoGuardian
              </span>
              <span className="badge badge-info" style={{ fontSize: '0.65rem', padding: '2px 6px' }}>SentryGate v1.0</span>
            </div>
            <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Intelligent GitHub Quality Platform</p>
          </div>
        </div>

        {/* Center Repo Selector */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          <span style={{ fontSize: '0.85rem', color: 'var(--text-subtle)', fontWeight: 600 }}>Active Scope:</span>
          <div style={{ position: 'relative' }}>
            <select
              value={selectedRepo?.id || ''}
              onChange={(e) => {
                const repo = repos.find(r => r.id === e.target.value);
                if (repo) onSelectRepo(repo);
              }}
              style={{
                background: 'var(--bg-input)',
                color: 'var(--text-main)',
                border: '1px solid var(--border-color)',
                padding: '8px 36px 8px 14px',
                borderRadius: '10px',
                fontSize: '0.875rem',
                fontWeight: 600,
                cursor: 'pointer',
                outline: 'none',
                appearance: 'none',
                minWidth: '220px'
              }}
            >
              {repos.map(r => (
                <option key={r.id} value={r.id} style={{ background: '#0d121d' }}>
                  {r.fullName}
                </option>
              ))}
            </select>
            <ChevronDown style={{ position: 'absolute', right: '12px', top: '50%', transform: 'translateY(-50%)', width: '16px', height: '16px', color: 'var(--text-muted)', pointerEvents: 'none' }} />
          </div>
        </div>

        {/* Right Section (Notifications & Profile) */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
          
          {/* Status Indicator */}
          <div className="glass-panel" style={{ padding: '6px 12px', borderRadius: '20px', display: 'flex', alignItems: 'center', gap: '8px', fontSize: '0.8rem' }}>
            <span style={{ width: '8px', height: '8px', borderRadius: '50%', background: '#10b981', boxShadow: '0 0 8px #10b981' }}></span>
            <span style={{ fontWeight: 600, color: '#34d399' }}>Gate Engine Active</span>
          </div>

          {/* Notifications Dropdown */}
          <div style={{ position: 'relative' }}>
            <button
              onClick={() => setShowNotifications(!showNotifications)}
              className="btn btn-secondary"
              style={{ padding: '8px', borderRadius: '10px', position: 'relative' }}
            >
              <Bell style={{ width: '18px', height: '18px', color: 'var(--text-muted)' }} />
              {notifications.some(n => n.unread) && (
                <span style={{
                  position: 'absolute', top: '4px', right: '4px',
                  width: '8px', height: '8px', borderRadius: '50%',
                  background: '#f43f5e'
                }} />
              )}
            </button>

            {showNotifications && (
              <div className="glass-panel animate-fade-in" style={{
                position: 'absolute', right: 0, top: '48px', width: '320px',
                padding: '16px', zIndex: 100, borderRadius: '14px'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '12px' }}>
                  <h4 style={{ fontSize: '0.9rem' }}>Notifications</h4>
                  <span className="badge badge-info">{notifications.filter(n => n.unread).length} New</span>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {notifications.map(n => (
                    <div key={n.id} style={{
                      padding: '10px', borderRadius: '8px',
                      background: n.unread ? 'rgba(99,102,241,0.08)' : 'rgba(255,255,255,0.02)',
                      borderLeft: n.unread ? '3px solid #6366f1' : '3px solid transparent'
                    }}>
                      <p style={{ fontWeight: 600, fontSize: '0.825rem' }}>{n.title}</p>
                      <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: '2px' }}>{n.message}</p>
                      <span style={{ fontSize: '0.68rem', color: 'var(--text-subtle)', marginTop: '4px', display: 'block' }}>{n.time}</span>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* User Profile */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', paddingLeft: '8px', borderLeft: '1px solid var(--border-color)' }}>
            <img
              src="https://avatars.githubusercontent.com/u/123456?v=4"
              alt="User Avatar"
              style={{ width: '36px', height: '36px', borderRadius: '50%', border: '2px solid rgba(99,102,241,0.5)' }}
            />
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: '0.85rem', fontWeight: 700 }}>alex-developer</span>
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>Lead Maintainer</span>
            </div>
          </div>

        </div>
      </div>
    </header>
  );
}
