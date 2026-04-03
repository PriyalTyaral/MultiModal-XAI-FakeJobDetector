import React from 'react';
import '../../styles/Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-brand-section">
            <div className="footer-brand-name">JobSatark</div>
            <p className="footer-description">Detect Fake Jobs. Protect Your Career.</p>
          </div>
          
          <div className="footer-socials">
            <a href="#twitter" className="footer-social-link" aria-label="Twitter">𝕏</a>
            <a href="https://github.com/avireddi08/MultiModal-XAI-FakeJobDetector" className="footer-social-link" aria-label="GitHub">GH</a>
            <a href="#linkedin" className="footer-social-link" aria-label="LinkedIn">in</a>
          </div>

          <p className="footer-copyright">&copy; {new Date().getFullYear()} JobSatark. All rights reserved.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
