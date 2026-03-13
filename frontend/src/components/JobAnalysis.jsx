import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { analyzeJob, analyzeJobFile } from '../services/api';
import { useToast } from './shared/Toast';
import { LoadingSpinner } from './shared/LoadingSpinner';
import FileUploadZone from './shared/FileUploadZone';
import '../styles/JobAnalysis.css';

const JobAnalysis = () => {
  const [activeTab, setActiveTab] = useState('text');
  const [jobText, setJobText] = useState('');
  const [file, setFile] = useState(null);
  const [fileType, setFileType] = useState('image');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const { addToast } = useToast();

  const handleTextSubmit = async (e) => {
    e.preventDefault();
    if (!jobText.trim()) {
      addToast('Please enter a job description to analyze.', 'warning');
      return;
    }
    setLoading(true);
    try {
      const result = await analyzeJob(jobText);
      navigate('/result', { state: { result } });
    } catch (err) {
      console.error(err);
      addToast('Error analyzing job posting. Please try again.', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleFileSubmit = async (e) => {
    e.preventDefault();
    if (!file) {
      addToast('Please select a file to upload.', 'warning');
      return;
    }
    setLoading(true);
    try {
      const result = await analyzeJobFile(file, fileType);
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
