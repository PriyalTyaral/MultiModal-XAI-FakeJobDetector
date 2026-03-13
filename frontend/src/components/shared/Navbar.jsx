import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import '../../styles/Navbar.css';

const Navbar = () => {
  const [mobileOpen, setMobileOpen] = useState(false);
  const location = useLocation();

  const isActive = (path) => location.pathname === path ? 'active' : '';

  const navLinks = [
    { to: '/', label: 'Home' },
    { to: '/analyze', label: 'Analyze' },
    { to: '/dashboard', label: 'Dashboard' },
    { to: '/report', label: 'Report' },
  ];

  return (
    <>
      <nav className="navbar">
        <Link to="/" className="navbar-brand">
          <div className="navbar-logo">S</div>
          <div className="navbar-title">Shield<span>AI</span></div>
        </Link>

        <div className="navbar-links">
          {navLinks.map(link => (
            <Link
              key={link.to}
              to={link.to}
              className={`navbar-link ${isActive(link.to)}`}
            >
              {link.label}
            </Link>
          ))}
        </div>

        <div className="navbar-actions">
          <Link to="/signin" className="btn btn-ghost">Sign In</Link>
          <Link to="/signup" className="btn btn-primary">Get Started</Link>
        </div>

        <button
          className={`navbar-hamburger ${mobileOpen ? 'open' : ''}`}
          onClick={() => setMobileOpen(!mobileOpen)}
          aria-label="Toggle menu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </nav>

      <div className={`navbar-mobile-menu ${mobileOpen ? 'open' : ''}`}>
        {navLinks.map(link => (
          <Link
            key={link.to}
            to={link.to}
            className={`navbar-link ${isActive(link.to)}`}
            onClick={() => setMobileOpen(false)}
          >
            {link.label}
          </Link>
        ))}
        <div className="navbar-actions">
          <Link to="/signin" className="btn btn-ghost" onClick={() => setMobileOpen(false)}>Sign In</Link>
          <Link to="/signup" className="btn btn-primary" onClick={() => setMobileOpen(false)}>Get Started</Link>
        </div>
      </div>
    </>
  );
};

export default Navbar;
