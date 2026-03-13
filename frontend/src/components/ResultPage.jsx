import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import '../styles/ResultPage.css';

const ResultPage = () => {
  const { state } = useLocation();
  const navigate = useNavigate();

  if (!state || !state.result) {
    return (
      <div className="result-page">
        <div className="container">
          <div className="result-empty">
            <div className="result-empty-icon">🔍</div>
            <h2>No Analysis Results</h2>
            <p>Submit a job posting to see the analysis results here.</p>
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/analyze')}>
              Analyze a Job Posting
            </button>
          </div>
        </div>
      </div>
    );
  }

  const { label, probability_fake, explanation } = state.result;

  // Normalize probability
  let fakeProb = probability_fake;
  if (fakeProb > 1) fakeProb = fakeProb / 100;
  fakeProb = Math.min(Math.max(fakeProb, 0), 1);

  const realProb = 1 - fakeProb;
  const isFake = fakeProb >= 0.5;
  const fakePercent = (fakeProb * 100).toFixed(1);
  const realPercent = (realProb * 100).toFixed(1);

  // Risk level
  let riskLevel, riskClass;
  if (fakeProb < 0.25) { riskLevel = 'Low Risk'; riskClass = 'low'; }
  else if (fakeProb < 0.5) { riskLevel = 'Medium Risk'; riskClass = 'medium'; }
  else if (fakeProb < 0.75) { riskLevel = 'High Risk'; riskClass = 'high'; }
  else { riskLevel = 'Critical Risk'; riskClass = 'critical'; }

  // Parse LIME explanation
  let explanationList = [];
  try {
    explanationList = explanation ? JSON.parse(explanation) : [];
  } catch (err) {
    console.error('Explanation parse error:', err);
  }

  // SVG Gauge
  const gaugeRadius = 80;
  const gaugeCircumference = 2 * Math.PI * gaugeRadius;
  const gaugeOffset = gaugeCircumference - (fakeProb * gaugeCircumference);

  return (
    <div className="result-page">
      <div className="container">
        <div className="result-header">
          <h1>Analysis <span className="text-gradient">Results</span></h1>
        </div>

        {/* Verdict Card */}
        <div className="result-verdict card-elevated">
          <div className="result-gauge-container">
            <svg width="200" height="200" viewBox="0 0 200 200">
              {/* Background circle */}
              <circle
                cx="100" cy="100" r={gaugeRadius}
                fill="none"
                stroke="var(--bg-secondary)"
                strokeWidth="12"
              />
              {/* Progress arc */}
              <circle
                cx="100" cy="100" r={gaugeRadius}
                fill="none"
                stroke={isFake ? 'var(--color-danger)' : 'var(--color-success)'}
                strokeWidth="12"
                strokeLinecap="round"
                strokeDasharray={gaugeCircumference}
                strokeDashoffset={gaugeOffset}
                transform="rotate(-90 100 100)"
                style={{ transition: 'stroke-dashoffset 1.2s cubic-bezier(0.34, 1.56, 0.64, 1)' }}
              />
              {/* Center text */}
              <text x="100" y="92" textAnchor="middle" fill="var(--text-primary)" fontSize="32" fontWeight="800">
                {fakePercent}%
              </text>
              <text x="100" y="115" textAnchor="middle" fill="var(--text-muted)" fontSize="12">
                Fake Probability
              </text>
            </svg>
          </div>

          <div className={`result-prediction ${isFake ? 'fake' : 'real'}`}>
            {isFake ? '⚠️ FAKE JOB DETECTED' : '✅ LEGITIMATE JOB'}
          </div>

          {/* Risk Gauge */}
          <div className="risk-gauge">
            <div className="risk-gauge-label">
              <span>Safe</span>
              <span>Critical</span>
            </div>
            <div className="risk-gauge-bar">
              <div
                className={`risk-gauge-fill ${riskClass}`}
                style={{ width: `${fakeProb * 100}%` }}
              />
            </div>
            <div className="risk-level-badge">
              <span className={`badge badge-${riskClass === 'low' ? 'success' : riskClass === 'medium' ? 'warning' : 'danger'}`}>
                {riskLevel}
              </span>
            </div>
          </div>
        </div>

        {/* Confidence Breakdown */}
        <div className="result-breakdown card-elevated">
          <h3>📊 Confidence Breakdown</h3>
          <div className="breakdown-bars">
            <div className="breakdown-item">
              <span className="breakdown-label">Fake</span>
              <div className="breakdown-bar">
                <div className="progress-bar-track">
                  <div
                    className="progress-bar-fill"
                    style={{
                      width: `${fakePercent}%`,
                      background: 'var(--gradient-danger)',
                    }}
                  />
                </div>
              </div>
              <span className="breakdown-value" style={{ color: 'var(--color-danger)' }}>
                {fakePercent}%
              </span>
            </div>
            <div className="breakdown-item">
              <span className="breakdown-label">Real</span>
              <div className="breakdown-bar">
                <div className="progress-bar-track">
                  <div
                    className="progress-bar-fill"
                    style={{
                      width: `${realPercent}%`,
                      background: 'var(--gradient-success)',
                    }}
                  />
                </div>
              </div>
              <span className="breakdown-value" style={{ color: 'var(--color-success)' }}>
                {realPercent}%
              </span>
            </div>
          </div>
        </div>

        {/* LIME Explanation */}
        {explanationList.length > 0 && (
          <div className="result-explanation card-elevated">
            <h3>🔑 Suspicious Keywords</h3>
            <div className="result-keywords">
              {explanationList.map((item, index) => (
                <span
                  key={index}
                  className={`keyword-pill ${item.weight > 0 ? 'suspicious' : 'safe'}`}
                >
                  {item.word}
                  <span className="keyword-weight">({item.weight > 0 ? '+' : ''}{item.weight.toFixed(3)})</span>
                </span>
              ))}
            </div>
          </div>
        )}

        {/* Actions */}
        <div className="result-actions">
          <button className="btn btn-primary btn-lg" onClick={() => navigate('/analyze')}>
            Analyze Another Job
          </button>
          <button className="btn btn-secondary btn-lg" onClick={() => navigate('/report')}>
            Report This Job
          </button>
          <button className="btn btn-ghost btn-lg" onClick={() => navigate('/dashboard')}>
            View Dashboard
          </button>
        </div>
      </div>
    </div>
  );
};

export default ResultPage;