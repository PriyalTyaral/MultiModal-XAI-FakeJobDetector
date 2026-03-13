import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/LandingPage.css';

const LandingPage = () => {
  const navigate = useNavigate();

  const features = [
    {
      icon: '📝',
      type: 'text',
      title: 'Text Analysis',
      description: 'Paste any job description and our AI will analyze language patterns, suspicious phrasing, and red flags instantly.',
    },
    {
      icon: '🎙️',
      type: 'audio',
      title: 'Audio Detection',
      description: 'Upload audio recordings of job calls to detect scripted pitches, urgency tactics, and voice manipulation.',
    },
    {
      icon: '🎬',
      type: 'video',
      title: 'Video Analysis',
      description: 'Submit video files for deep analysis of visual cues, presentation patterns, and content authenticity.',
    },
    {
      icon: '📄',
      type: 'document',
      title: 'Document Scanning',
      description: 'Upload screenshots, PDFs, or documents to extract and analyze job posting content with OCR technology.',
    },
  ];

  const stats = [
    { number: '99.2%', label: 'Detection Accuracy' },
    { number: '50K+', label: 'Jobs Analyzed' },
    { number: '< 3s', label: 'Average Response' },
    { number: '12K+', label: 'Fake Jobs Caught' },
  ];

  const steps = [
    { title: 'Upload Content', description: 'Paste job text or upload audio, video, or document files through our simple interface.' },
    { title: 'AI Analysis', description: 'Our multi-modal AI engine processes your input using NLP, LIME explanations, and pattern recognition.' },
    { title: 'Get Results', description: 'Receive a detailed report with confidence scores, risk levels, and suspicious keyword highlights.' },
  ];

  return (
    <div className="landing-page">
      {/* Hero Section */}
      <section className="landing-hero">
        <div className="container">
          <div className="landing-hero-badge">
            🛡️ AI-Powered Job Scams Detection
          </div>
          <h1>
            Detect <span className="text-gradient">Fake Jobs</span><br />
            Before You Apply
          </h1>
          <p>
            Shield yourself from fraudulent job postings using advanced multi-modal AI that analyzes text, audio, video, and documents in seconds.
          </p>
          <div className="landing-hero-actions">
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/analyze')}>
              Start Analyzing →
            </button>
            <button className="btn btn-secondary btn-lg" onClick={() => navigate('/signup')}>
              Create Free Account
            </button>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="container">
        <div className="landing-stats stagger-children">
          {stats.map((stat, i) => (
            <div key={i} className="landing-stat">
              <div className="landing-stat-number">{stat.number}</div>
              <div className="landing-stat-label">{stat.label}</div>
            </div>
          ))}
        </div>
      </section>

      {/* Features */}
      <section className="landing-features">
        <div className="container">
          <div className="landing-features-header">
            <h2>Multi-Modal Detection</h2>
            <p>Analyze job postings across every medium with our comprehensive AI toolkit.</p>
          </div>
          <div className="landing-features-grid stagger-children">
            {features.map((feature, i) => (
              <div key={i} className="feature-card">
                <div className={`feature-icon ${feature.type}`}>{feature.icon}</div>
                <h3>{feature.title}</h3>
                <p>{feature.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* How It Works */}
      <section className="landing-how">
        <div className="container">
          <div className="landing-how-header">
            <h2>How It Works</h2>
            <p>Three simple steps to verify any job posting</p>
          </div>
          <div className="landing-how-steps stagger-children">
            {steps.map((step, i) => (
              <div key={i} className="how-step">
                <div className="how-step-number">{i + 1}</div>
                <h3>{step.title}</h3>
                <p>{step.description}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="landing-cta">
        <div className="container">
          <div className="landing-cta-card">
            <h2>Ready to Stay Safe?</h2>
            <p>Start analyzing job postings for free. No credit card required.</p>
            <button className="btn btn-primary btn-lg" onClick={() => navigate('/analyze')}>
              Analyze a Job Posting →
            </button>
          </div>
        </div>
      </section>
    </div>
  );
};

export default LandingPage;
