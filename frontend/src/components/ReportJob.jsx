import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useToast } from './shared/Toast';
import FileUploadZone from './shared/FileUploadZone';
import '../styles/ReportJob.css';

const categories = [
  { key: 'misleading', icon: '🎭', label: 'Misleading Information' },
  { key: 'scam', icon: '💰', label: 'Financial Scam' },
  { key: 'phishing', icon: '🎣', label: 'Phishing Attempt' },
  { key: 'identity', icon: '🪪', label: 'Identity Theft' },
  { key: 'pyramid', icon: '🔺', label: 'Pyramid Scheme / MLM' },
  { key: 'other', icon: '❓', label: 'Other' },
];

const ReportJob = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [form, setForm] = useState({
    url: '',
    category: '',
    description: '',
  });
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState('image');
  const [submitted, setSubmitted] = useState(false);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!form.category) {
      addToast('Please select a category', 'warning');
      return;
    }
    if (!form.description.trim()) {
      addToast('Please provide a description', 'warning');
      return;
    }
    addToast('Report submitted successfully!', 'success');
    setSubmitted(true);
  };

  if (submitted) {
    return (
      <div className="report-page">
        <div className="container">
          <div className="report-card card-elevated">
            <div className="report-success">
              <div className="report-success-icon">✅</div>
              <h3>Report Submitted</h3>
              <p>Thank you for helping keep job seekers safe. We'll review your report and take appropriate action.</p>
              <div style={{ display: 'flex', gap: 'var(--space-4)', justifyContent: 'center' }}>
                <button className="btn btn-primary" onClick={() => navigate('/analyze')}>
                  Analyze a Job
                </button>
                <button className="btn btn-secondary" onClick={() => { setSubmitted(false); setForm({ url: '', category: '', description: '' }); setFile(null); }}>
                  Submit Another Report
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="report-page">
      <div className="container">
        <div className="report-header">
          <h1>Report a <span className="text-gradient">Fake Job</span></h1>
          <p>Help us protect the community by reporting suspicious job postings you've encountered.</p>
        </div>

        <div className="report-card card-elevated">
          <form className="report-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <label className="input-label">Job Posting URL (optional)</label>
              <input
                className="input-field"
                type="url"
                name="url"
                placeholder="https://example.com/job-posting"
                value={form.url}
                onChange={handleChange}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Category *</label>
              <div className="report-categories">
                {categories.map(cat => (
                  <button
                    key={cat.key}
                    type="button"
                    className={`report-category ${form.category === cat.key ? 'active' : ''}`}
                    onClick={() => setForm({ ...form, category: cat.key })}
                  >
                    {cat.icon} {cat.label}
                  </button>
                ))}
              </div>
            </div>

            <div className="input-group">
              <label className="input-label">Description *</label>
              <textarea
                className="input-field input-textarea"
                name="description"
                placeholder="Describe why you believe this job posting is fraudulent. Include any red flags, suspicious communication, or requests for personal information..."
                value={form.description}
                onChange={handleChange}
                rows={5}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Evidence (optional)</label>
              <FileUploadZone
                file={file}
                setFile={setFile}
                fileType={fileType}
                setFileType={setFileType}
              />
            </div>

            <button type="submit" className="btn btn-primary btn-lg" style={{ width: '100%' }}>
              🚨 Submit Report
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default ReportJob;
