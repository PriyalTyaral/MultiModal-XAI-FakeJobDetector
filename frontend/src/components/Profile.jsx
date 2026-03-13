import React, { useState } from 'react';
import '../styles/Profile.css';

const mockUser = {
  name: 'John Doe',
  email: 'john.doe@example.com',
  joined: 'January 2026',
  analyses: 47,
  reports: 5,
  fakeFound: 18,
};

const mockActivity = [
  { id: 1, type: 'fake', icon: '⚠️', title: 'Flagged "Data Entry - $5000/week"', date: '2 hours ago' },
  { id: 2, type: 'real', icon: '✅', title: 'Verified "Software Engineer at InnovateTech"', date: '1 day ago' },
  { id: 3, type: 'report', icon: '🚨', title: 'Reported suspicious job listing', date: '2 days ago' },
  { id: 4, type: 'fake', icon: '⚠️', title: 'Flagged "Remote Admin - No Experience"', date: '3 days ago' },
  { id: 5, type: 'real', icon: '✅', title: 'Verified "Marketing Manager at GlobalBrands"', date: '4 days ago' },
];

const Profile = () => {
  const [settings, setSettings] = useState({
    emailNotifications: true,
    analysisAlerts: true,
    weeklyReport: false,
    darkMode: true,
  });

  const toggleSetting = (key) => {
    setSettings({ ...settings, [key]: !settings[key] });
  };

  const settingsConfig = [
    { key: 'emailNotifications', title: 'Email Notifications', desc: 'Receive email alerts for flagged jobs' },
    { key: 'analysisAlerts', title: 'Analysis Alerts', desc: 'Get notified when analysis is complete' },
    { key: 'weeklyReport', title: 'Weekly Report', desc: 'Receive a weekly summary of your activity' },
    { key: 'darkMode', title: 'Dark Mode', desc: 'Use dark theme for the interface' },
  ];

  return (
    <div className="profile-page">
      <div className="container">
        <div className="profile-header">
          <h1>Profile</h1>
        </div>

        <div className="profile-grid">
          {/* Left - Profile Card */}
          <div className="profile-card card-elevated">
            <div className="profile-avatar">
              {mockUser.name.charAt(0)}
            </div>
            <div className="profile-name">{mockUser.name}</div>
            <div className="profile-email">{mockUser.email}</div>
            <div className="profile-joined">Joined {mockUser.joined}</div>

            <div className="profile-stats-mini">
              <div className="profile-stat-mini">
                <div className="profile-stat-mini-value">{mockUser.analyses}</div>
                <div className="profile-stat-mini-label">Analyses</div>
              </div>
              <div className="profile-stat-mini">
                <div className="profile-stat-mini-value">{mockUser.fakeFound}</div>
                <div className="profile-stat-mini-label">Fake Found</div>
              </div>
              <div className="profile-stat-mini">
                <div className="profile-stat-mini-value">{mockUser.reports}</div>
                <div className="profile-stat-mini-label">Reports</div>
              </div>
            </div>
          </div>

          {/* Right - Content */}
          <div className="profile-content">
            {/* Settings */}
            <div className="profile-section card-elevated">
              <h3>⚙️ Settings</h3>
              {settingsConfig.map(item => (
                <div key={item.key} className="settings-item">
                  <div className="settings-info">
                    <h4>{item.title}</h4>
                    <p>{item.desc}</p>
                  </div>
                  <label className="toggle">
                    <input
                      type="checkbox"
                      checked={settings[item.key]}
                      onChange={() => toggleSetting(item.key)}
                    />
                    <span className="toggle-slider"></span>
                  </label>
                </div>
              ))}
            </div>

            {/* Activity */}
            <div className="profile-section card-elevated">
              <h3>📋 Recent Activity</h3>
              <div className="activity-list">
                {mockActivity.map(item => (
                  <div key={item.id} className="activity-item">
                    <div className={`activity-icon ${item.type}`}>{item.icon}</div>
                    <div className="activity-info">
                      <div className="activity-title">{item.title}</div>
                      <div className="activity-date">{item.date}</div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Profile;
