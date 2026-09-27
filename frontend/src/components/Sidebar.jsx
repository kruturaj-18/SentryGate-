import React from 'react';
import { 
  LayoutDashboard, 
  GitPullRequest, 
  GitFork, 
  Layers, 
  Users, 
  ShieldCheck, 
  Sliders, 
  Sparkles,
  Search,
  FileCode
} from 'lucide-react';

export default function Sidebar({ activeTab, setActiveTab }) {
  const menuItems = [
    { id: 'dashboard', label: 'Overview', icon: LayoutDashboard },
    { id: 'repositories', label: 'Repositories & Rules', icon: GitFork },
    { id: 'pull-requests', label: 'Pull Request Gates', icon: GitPullRequest, badge: 'Live' },
    { id: 'issue-duplicates', label: 'Duplicate Issue Radar', icon: Search },
    { id: 'contributor-trust', label: 'Contributor Trust', icon: Users },
    { id: 'playground', label: 'Rule Test Bench', icon: Sparkles, badge: 'Interactive' },
  ];

  return (
    <aside style={{
      width: '240px',
      minWidth: '240px',
      background: 'rgba(13, 18, 29, 0.85)',
      backdropFilter: 'blur(16px)',
      borderRight: 'var(--glass-border)',
      minHeight: 'calc(100vh - 65px)',
      padding: '24px 14px',
      display: 'flex',
      flexDirection: 'column',
      justify: 'space-between'
    }}>
      <div>
        <div style={{ paddingLeft: '10px', marginBottom: '16px' }}>
          <span style={{ fontSize: '0.7rem', fontWeight: 800, color: 'var(--text-subtle)', letterSpacing: '1px', textTransform: 'uppercase' }}>
            Main Menu
          </span>
        </div>

        <nav style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {menuItems.map(item => {
            const Icon = item.icon;
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => setActiveTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justify: 'space-between',
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: '10px',
                  border: 'none',
                  background: isActive 
                    ? 'linear-gradient(135deg, rgba(99,102,241,0.2) 0%, rgba(79,70,229,0.1) 100%)' 
                    : 'transparent',
                  color: isActive ? '#ffffff' : 'var(--text-muted)',
                  borderLeft: isActive ? '3px solid #6366f1' : '3px solid transparent',
                  fontWeight: isActive ? 700 : 500,
                  fontSize: '0.875rem',
                  cursor: 'pointer',
                  transition: 'all 0.18s ease'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <Icon style={{ width: '18px', height: '18px', color: isActive ? '#6366f1' : 'var(--text-subtle)' }} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`badge ${item.badge === 'Interactive' ? 'badge-info' : 'badge-success'}`} style={{ fontSize: '0.625rem', padding: '1px 5px' }}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>
      </div>

      {/* Footer Info Box */}
      <div className="glass-panel" style={{ padding: '14px', borderRadius: '12px', background: 'rgba(99,102,241,0.05)' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '6px' }}>
          <ShieldCheck style={{ color: '#10b981', width: '16px', height: '16px' }} />
          <span style={{ fontSize: '0.8rem', fontWeight: 700 }}>Zero-Bypass Policy</span>
        </div>
        <p style={{ fontSize: '0.72rem', color: 'var(--text-muted)', lineHeight: '1.3' }}>
          All PR check runs enforce automated AST analysis & quality thresholds.
        </p>
      </div>
    </aside>
  );
}
