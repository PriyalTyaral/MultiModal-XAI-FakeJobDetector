import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyzeJobWithExplanation, analyzeJobFile } from '../services/api';
import { useToast } from './shared/Toast';
import { useAuth } from '../context/AuthContext';
import { LoadingSpinner } from './shared/LoadingSpinner';
import FileUploadZone from './shared/FileUploadZone';
import '../styles/JobAnalysis.css';

/**
 * JobAnalysis Component
 * Provides the main interface for users to analyze job postings.
 * Supports switching between two modes: "Text Input" and "File Upload".
 */
const JobAnalysis = () => {
  // State for active tab ('text' or 'file')
  const [activeTab, setActiveTab] = useState('text');
  
  // State for Domain Verification
  const [domainInput, setDomainInput] = useState('');
  
  // State for text input mode
  const [jobText, setJobText] = useState('');
  
  // State for file upload mode
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState('image'); // default type
  
  // Global loading state during API calls
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  
  // App context
  const { addToast } = useToast();
  const { user } = useAuth();

  /**
   * Handles the submission of the text-based job analysis form.
   * Validates input, shows loading state, calls API, and redirects to result page.
   */
  const handleTextSubmit = async (e) => {
    e.preventDefault();
    if (!jobText.trim()) {
      addToast('Please enter a job description to analyze.', 'warning');
      return;
    }
    setLoading(true);
    try {
      const actualUserId = user ? (user.userId || user.id || user._id) : '';
      console.log('--- DEBUG: Submitting Text Analysis ---');
      console.log('User Object:', user);
      console.log('Actual UserID resolved:', actualUserId);
      const result = await analyzeJobWithExplanation(jobText, domainInput, actualUserId, 10, 'json');
      navigate('/result', { state: { result } });
    } catch (err) {
      console.error(err);
      addToast('Error analyzing job posting. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  /**
   * Handles the submission of the file-based job analysis form.
   * Makes sure a file is selected, sets loading state, calls API, and redirects to result page.
   */
  const handleFileSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      addToast('Please select a file to upload.', 'warning');
      return;
    }
    setLoading(true);
    try {
      const actualUserId = user ? (user.userId || user.id || user._id) : '';
      console.log('--- DEBUG: Submitting File Analysis ---');
      console.log('User Object:', user);
      console.log('Actual UserID resolved:', actualUserId);
      const result = await analyzeJobFile(file, fileType, domainInput, actualUserId);
      navigate('/result', { state: { result } });
    } catch (err) {
      console.error(err);
      addToast('Error analyzing file. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="analysis-page">
      <div className="container">
        <div className="analysis-header">
          <h1>Analyze a <span className="text-gradient">Job Posting</span></h1>
          <p>Paste text or upload a file to check if a job posting is legitimate</p>
        </div>

        <div className="analysis-card card-elevated">
          {/* Tabs */}
          <div className="analysis-tabs">
            <div className="tabs">
              <button
                className={`tab ${activeTab === 'text' ? 'active' : ''}`}
                onClick={() => setActiveTab('text')}
              >
                📝 Text Input
              </button>
              <button
                className={`tab ${activeTab === 'file' ? 'active' : ''}`}
                onClick={() => setActiveTab('file')}
              >
                📁 File Upload
              </button>
            </div>
          </div>

          {/* Text Tab */}
          {activeTab === 'text' && (
            <form onSubmit={handleTextSubmit}>
              {/* Optional Domain Verification Input */}
              <div className="analysis-input-group" style={{ marginBottom: 'var(--space-4)' }}>
                <label style={{ display: 'block', marginBottom: 'var(--space-2)', fontWeight: 'var(--font-weight-medium)', color: 'var(--text-secondary)' }}>
                  Company Domain or Recruiter Email (Optional)
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g., recruiter@gmail.com, https://google-careers.xyz"
                  value={domainInput}
                  onChange={(e) => setDomainInput(e.target.value)}
                />
              </div>

              <div className="analysis-textarea-wrapper">
                <textarea
                  className="analysis-textarea"
                  placeholder="Paste the job description text here...&#10;&#10;Include details like job title, company name, requirements, salary, location, etc."
                  value={jobText}
                  onChange={(e) => setJobText(e.target.value)}
                />
                <div className="analysis-char-count">
                  {jobText.length.toLocaleString()} characters
                </div>
              </div>
              <div className="analysis-submit">
                <span className="analysis-submit-hint">
                  Tip: Include as much detail as possible for better accuracy
                </span>
                <button
                  type="submit"
                  className="btn btn-primary btn-lg"
                  disabled={loading || !jobText.trim()}
                >
                  🔍 Analyze Text
                </button>
              </div>
            </form>
          )}

          {/* File Tab */}
          {activeTab === 'file' && (
            <form onSubmit={handleFileSubmit}>
              {/* Optional Domain Verification Input */}
              <div className="analysis-input-group" style={{ marginBottom: 'var(--space-4)' }}>
                <label style={{ display: 'block', marginBottom: 'var(--space-2)', fontWeight: 'var(--font-weight-medium)', color: 'var(--text-secondary)' }}>
                  Company Domain or Recruiter Email (Optional)
                </label>
                <input
                  type="text"
                  className="input-field"
                  placeholder="e.g., recruiter@gmail.com, https://google-careers.xyz"
                  value={domainInput}
                  onChange={(e) => setDomainInput(e.target.value)}
                />
              </div>

              <FileUploadZone
                file={file}
                setFile={setFile}
                fileType={fileType}
                setFileType={setFileType}
              />
              <div className="analysis-submit" style={{ marginTop: 'var(--space-6)' }}>
                <span className="analysis-submit-hint">
                  Supported: Images, Audio, Video, Documents
                </span>
                <button
                  type="submit"
                  className="btn btn-primary btn-lg"
                  disabled={loading || !file}
                >
                  🔍 Analyze File
                </button>
              </div>
            </form>
          )}
        </div>
      </div>

      {/* Loading Overlay */}
      {loading && (
        <div className="analysis-loading-overlay">
          <LoadingSpinner size={56} text="Analyzing your job posting with AI..." />
        </div>
      )}
    </div>
  );
};

export default JobAnalysis;
