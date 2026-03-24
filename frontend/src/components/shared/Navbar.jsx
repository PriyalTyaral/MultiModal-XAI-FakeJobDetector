import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useToast } from './Toast';
import '../../styles/Navbar.css';

const Navbar = () => {
  const [isScrolled, setIsScrolled] = useState(false);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();
  const { user, logout } = useAuth();
  const { addToast } = useToast();

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 0) {
        setIsScrolled(true);
      } else {
        setIsScrolled(false);
      }
    };

    window.addEventListener('scroll', handleScroll);
    return () => {
      window.removeEventListener('scroll', handleScroll);
    };
  }, []);

  const navLinks = [
    { path: '/', name: 'Home' },
    { path: '/analyze', name: 'Analyze' },
    { path: '/report', name: 'Report' },
  ];

  const handleLogout = () => {
    logout();
    addToast('Logged out successfully', 'success');
    navigate('/');
    setMobileMenuOpen(false);
  };

  return (
    <>
      <nav className={`navbar ${isScrolled ? 'scrolled' : ''}`}>
        <Link to="/" className="navbar-brand">
          <div className="navbar-logo">JS</div>
          <div className="navbar-title">JobSatark</div>
        </Link>

        {/* Desktop Navigation */}
        <div className="navbar-links">
          {navLinks.map((link) => (
            <Link
              key={link.path}
              to={link.path}
              className={`navbar-link ${location.pathname === link.path ? 'active' : ''}`}
            >
              {link.name}
            </Link>
          ))}

          {user && (
            <Link to="/dashboard" className={`navbar-link ${location.pathname === '/dashboard' ? 'active' : ''}`}>
              Dashboard
            </Link>
          )}
        </div>

        {/* Desktop Auth Buttons */}
        <div className="navbar-actions">
          {user ? (
            <>
              <span style={{ color: 'var(--text-secondary)', marginRight: 'var(--space-4)', fontWeight: 'var(--font-weight-medium)' }}>
                Hi, {user.name.split(' ')[0]}
              </span>
              <button className="btn btn-outline" onClick={handleLogout}>
                Logout
              </button>
            </>
          ) : (
            <>
              <Link to="/signin" className="btn btn-outline">Sign In</Link>
              <Link to="/signup" className="btn btn-primary">Get Started</Link>
            </>
          )}
        </div>

        {/* Mobile menu button */}
        <button
          className={`navbar-hamburger ${mobileMenuOpen ? 'open' : ''}`}
          onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
          aria-label="Toggle menu"
        >
          <span></span>
          <span></span>
          <span></span>
        </button>
      </nav>

      <div className={`navbar-mobile-menu ${mobileMenuOpen ? 'open' : ''}`}>
        <div className="navbar-mobile-links">
          {navLinks.map(link => (
            <Link
              key={link.path}
              to={link.path}
              className={`navbar-link ${location.pathname === link.path ? 'active' : ''}`}
              onClick={() => setMobileMenuOpen(false)}
            >
              {link.name}
            </Link>
          ))}
          {user && (
            <Link
              to="/dashboard"
              className={`navbar-link ${location.pathname === '/dashboard' ? 'active' : ''}`}
              onClick={() => setMobileMenuOpen(false)}
            >
              Dashboard
            </Link>
          )}

          <div className="navbar-actions">
            {user ? (
              <>
                <div style={{ padding: '0 var(--space-4) var(--space-4)', color: 'var(--text-secondary)' }}>
                  Signed in as {user.name}
                </div>
                <button className="btn btn-outline btn-full" onClick={handleLogout}>Logout</button>
              </>
            ) : (
              <>
                <Link to="/signin" className="btn btn-outline btn-full" onClick={() => setMobileMenuOpen(false)}>Sign In</Link>
                <Link to="/signup" className="btn btn-primary btn-full" onClick={() => setMobileMenuOpen(false)}>Get Started</Link>
              </>
            )}
          </div>
        </div>
      </div>
    </>
  );
};

export default Navbar;
