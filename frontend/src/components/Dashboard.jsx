import React, { useState, useEffect } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { useAuth } from '../context/AuthContext';
import { getUserDashboard } from '../services/api';
import '../styles/Dashboard.css';

const Dashboard = () => {
  const navigate = useNavigate();
  const { user } = useAuth();
  
  const [search, setSearch] = useState('');
  const [filter, setFilter] = useState('all');
  
  const [stats, setStats] = useState({ total: 0, fake: 0, real: 0, avgConfidence: 0 });
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);

  // Re-map the raw history data into a chart-friendly format
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    const fetchDashboard = async () => {
      if (!user) return;
      try {
        const actualUserId = user.userId || user.id || user._id;
        const data = await getUserDashboard(actualUserId);
        setStats(data.stats);
        setHistory(data.history);
        
        // Very basic aggregation for chart (group by month string derived from ISO localdatetime)
        const grouped = data.history.reduce((acc, curr) => {
          const date = new Date(curr.createdAt);
          const month = date.toLocaleString('default', { month: 'short' });
          if (!acc[month]) acc[month] = { fake: 0, real: 0 };
          
          if (curr.result?.toLowerCase() === 'fake') {
            acc[month].fake += 1;
          } else {
            acc[month].real += 1;
          }
          return acc;
        }, {});
        
        const cData = Object.keys(grouped).map(key => ({
          month: key,
          fake: grouped[key].fake,
          real: grouped[key].real
        }));
        cData.sort((a,b) => {
           const months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];
           return months.indexOf(a.month) - months.indexOf(b.month);
        });
        setChartData(cData);

      } catch (err) {
        console.error("Failed to load dashboard data", err);
      } finally {
        setLoading(false);
      }
    };
    
    fetchDashboard();
  }, [user]);

  // Protect route
  if (!user) {
    return <Navigate to="/signin" replace />;
  }

  const filteredHistory = history.filter(item => {
    // Basic search on text snippet since we no longer have "title"
    const snippet = item.text ? item.text.substring(0, 50).toLowerCase() : 'Anonymous Job';
    const matchesSearch = snippet.includes(search.toLowerCase());
    const filterNorm = filter.toUpperCase();
    const matchesFilter = filter === 'all' || item.result?.toUpperCase() === filterNorm;
    return matchesSearch && matchesFilter;
  });

  if (loading) {
    return (
      <div className="dashboard-page" style={{display: 'flex', justifyContent: 'center', alignItems: 'center'}}>
        <h2>Loading Dashboard...</h2>
      </div>
    );
  }

  return (
    <div className="dashboard-page">
      <div className="container">
        <div className="dashboard-header">
          <h1>Dashboard</h1>
          <button className="btn btn-primary" onClick={() => navigate('/analyze')}>
            + New Analysis
          </button>
        </div>

        <div className="dashboard-stats stagger-children">
          <div className="stat-card">
            <div className="stat-card-icon purple">📊</div>
            <div className="stat-card-value">{stats.total}</div>
            <div className="stat-card-label">Total Analyses</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon red">⚠️</div>
            <div className="stat-card-value" style={{ color: 'var(--color-danger)' }}>{stats.fake}</div>
            <div className="stat-card-label">Fake Detected</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon green">✅</div>
            <div className="stat-card-value" style={{ color: 'var(--color-success)' }}>{stats.real}</div>
            <div className="stat-card-label">Legitimate</div>
          </div>
          <div className="stat-card">
            <div className="stat-card-icon blue">🎯</div>
            <div className="stat-card-value">{stats.avgConfidence}%</div>
            <div className="stat-card-label">Avg Fake Confidence</div>
          </div>
        </div>

        {/* Chart */}
        <div className="dashboard-chart card-elevated">
          <h3>📈 Detection History</h3>
          <div className="dashboard-chart-container">
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={chartData} barCategoryGap="20%">
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
            ) : (
               <div style={{color: 'var(--text-muted)', textAlign: 'center', paddingTop: '4rem'}}>Not enough data yet to visualize.</div>
            )}
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
                    <td style={{ color: 'var(--text-primary)', fontWeight: 500 }}>
                      {item.text ? item.text.substring(0, 40) + '...' : 'Analyzed Job Data'}
                    </td>
                    <td>{new Date(item.createdAt).toLocaleDateString()}</td>
                    <td>
                      <span className={`badge ${item.result?.toUpperCase() === 'FAKE' ? 'badge-danger' : 'badge-success'}`}>
                        {item.result?.toUpperCase() === 'FAKE' ? '⚠️ Fake' : '✅ Real'}
                      </span>
                    </td>
                    <td>
                      <span style={{ fontWeight: 600, color: (item.fake_confidence || 0) > 50 ? 'var(--color-danger)' : 'var(--color-success)' }}>
                        {(item.fake_confidence || 0).toFixed(1)}%
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
