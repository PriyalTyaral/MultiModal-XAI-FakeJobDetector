import React from 'react';
import { Link } from 'react-router-dom';
import '../../styles/Footer.css';

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-grid">
          <div className="footer-brand">
            <div className="footer-brand-logo">
              <div className="navbar-logo">S</div>
              <span>ShieldAI</span>
            </div>
            <p>
              Protecting job seekers from fraudulent postings using advanced AI analysis across text, audio, video, and documents.
            </p>
          </div>

          <div className="footer-column">
            <h4>Product</h4>
            <Link to="/analyze">Job Analysis</Link>
            <Link to="/dashboard">Dashboard</Link>
            <Link to="/report">Report Job</Link>
          </div>

          <div className="footer-column">
            <h4>Company</h4>
            <a href="#about">About</a>
            <a href="#blog">Blog</a>
            <a href="#careers">Careers</a>
          </div>

          <div className="footer-column">
            <h4>Support</h4>
            <a href="#help">Help Center</a>
            <a href="#privacy">Privacy Policy</a>
            <a href="#terms">Terms of Service</a>
          </div>
        </div>

        <div className="footer-bottom">
          <p>&copy; {new Date().getFullYear()} ShieldAI. All rights reserved.</p>
          <div className="footer-socials">
            <a href="#twitter" className="footer-social-link" aria-label="Twitter">𝕏</a>
            <a href="#github" className="footer-social-link" aria-label="GitHub">GH</a>
            <a href="#linkedin" className="footer-social-link" aria-label="LinkedIn">in</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
