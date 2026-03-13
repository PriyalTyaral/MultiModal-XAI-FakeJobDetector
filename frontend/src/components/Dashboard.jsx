import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import '../styles/Dashboard.css';

// Mock data
const mockStats = {
  total: 47,
  fake: 18,
  real: 29,
  avgConfidence: 87.3,
};

const mockChartData = [
  { month: 'Jan', fake: 2, real: 5 },
  { month: 'Feb', fake: 3, real: 4 },
  { month: 'Mar', fake: 1, real: 6 },
  { month: 'Apr', fake: 4, real: 3 },
  { month: 'May', fake: 2, real: 7 },
  { month: 'Jun', fake: 3, real: 2 },
  { month: 'Jul', fake: 3, real: 2 },
];

const mockHistory = [
  { id: 1, title: 'Senior Developer at TechCorp', date: '2026-03-10', result: 'fake', confidence: 92.4 },
  { id: 2, title: 'Marketing Manager at GlobalBrands', date: '2026-03-09', result: 'real', confidence: 88.1 },
  { id: 3, title: 'Data Entry Specialist - Remote', date: '2026-03-08', result: 'fake', confidence: 95.7 },
  { id: 4, title: 'Software Engineer at InnovateTech', date: '2026-03-07', result: 'real', confidence: 76.3 },
  { id: 5, title: 'Customer Service Rep - $5000/week', date: '2026-03-06', result: 'fake', confidence: 98.2 },
  { id: 6, title: 'Product Designer at DesignHub', date: '2026-03-05', result: 'real', confidence: 82.9 },
  { id: 7, title: 'Admin Assistant - Work From Home', date: '2026-03-04', result: 'fake', confidence: 89.5 },
  { id: 8, title: 'Full Stack Developer at StartupXYZ', date: '2026-03-03', result: 'real', confidence: 91.0 },
];

const Dashboard = () => {
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');

  const filteredHistory = mockHistory.filter(item => {
    const matchesSearch = item.title.toLowerCase().includes(search.toLowerCase());
    const matchesFilter = filter === 'all' || item.result === filter;
    return matchesSearch && matchesFilter;
  });

  return (
    <div className="dashboard-page">
      <div className="container">
        <div className="dashboard-header">
          <h1>Dashboard</h1>
          <button className="btn btn-primary" onClick={() => navigate('/analyze')}>
            + New Analysis
          </button>
        </div>

        {/* Stats */}
        <div className="dashboard-stats stagger-children">
          <div className="stat-card">
            <div className="stat-card-icon purple">📊</div>
            <div className="stat-card-value">{mockStats.total}</div>
            <div className="stat-card-label">Total Analyses</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon red">⚠️</div>
            <div className="stat-card-value" style={{ color: 'var(--color-danger)' }}>{mockStats.fake}</div>
            <div className="stat-card-label">Fake Detected</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon green">✅</div>
            <div className="stat-card-value" style={{ color: 'var(--color-success)' }}>{mockStats.real}</div>
            <div className="stat-card-label">Legitimate</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon blue">🎯</div>
            <div className="stat-card-value">{mockStats.avgConfidence}%</div>
            <div className="stat-card-label">Avg Confidence</div>
          </div>
        </div>

        {/* Chart */}
        <div className="dashboard-chart card-elevated">
          <h3>📈 Detection History</h3>
          <div className="dashboard-chart-container">
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={mockChartData} barCategoryGap="20%">
                <CartesianGrid strokeDasharray="3 3" stroke="rgba(255,255,255,0.06)" />
                <XAxis dataKey="month" stroke="var(--text-muted)" fontSize={12} />
                <YAxis stroke="var(--text-muted)" fontSize={12} />
                <Tooltip
                  contentStyle={{
                    background: 'var(--bg-elevated)',
                    border: '1px solid var(--border-subtle)',
                    borderRadius: 'var(--radius-md)',
                    color: 'var(--text-primary)',
                  }}
                />
                <Bar dataKey="fake" fill="#ef4444" radius={[4, 4, 0, 0]} name="Fake" />
                <Bar dataKey="real" fill="#22c55e" radius={[4, 4, 0, 0]} name="Real" />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* History */}
        <div className="dashboard-history card-elevated">
          <div className="dashboard-history-header">
            <h3>📋 Recent Analyses</h3>
            <input
              className="input-field dashboard-search"
              type="text"
              placeholder="Search analyses..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          <div className="dashboard-filters">
            {['all', 'fake', 'real'].map(f => (
              <button
                key={f}
                className={`filter-btn ${filter === f ? 'active' : ''}`}
                onClick={() => setFilter(f)}
              >
                {f === 'all' ? 'All' : f === 'fake' ? '⚠️ Fake' : '✅ Real'}
              </button>
            ))}
          </div>

          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Job Title</th>
                  <th>Date</th>
                  <th>Result</th>
                  <th>Confidence</th>
                </tr>
              </thead>
              <tbody>
                {filteredHistory.map(item => (
                  <tr key={item.id}>
                    <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{item.title}</td>
                    <td>{item.date}</td>
                    <td>
                      <span className={`badge ${item.result === 'fake' ? 'badge-danger' : 'badge-success'}`}>
                        {item.result === 'fake' ? '⚠️ Fake' : '✅ Real'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 600, color: item.confidence > 90 ? 'var(--color-success)' : 'var(--text-secondary)' }}>
                        {item.confidence}%
                      </span>
                    </td>
                  </tr>
                ))}
                {filteredHistory.length === 0 && (
                  <tr>
                    <td colSpan="4" style={{ textAlign: 'center', padding: 'var(--space-8)', color: 'var(--text-muted)' }}>
                      No analyses found matching your search.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
