import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useToast } from './shared/Toast';
import { useAuth } from '../context/AuthContext';
import { signinUser } from '../services/api';
import '../styles/Auth.css';

const SignIn = () => {
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [formData, setFormData] = useState({ email: '', password: '' });
  const [showPassword, setShowPassword] = useState(false);
  const [remember, setRemember] = useState(false);
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!formData.email || !formData.password) {
      addToast('Please enter email and password.', 'warning');
      return;
    }

    setLoading(true);
    try {
      const user = await signinUser(formData.email, formData.password);
      login(user);
      addToast('Signed in successfully!', 'success');
      navigate('/dashboard');
    } catch (err) {
      addToast('Invalid email or password.', 'danger');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="auth-container">
        {/* Left Panel */}
        <div className="auth-panel">
          <div className="auth-panel-content">
            <h2>Welcome Back to JobSatark</h2>
            <p>Continue protecting yourself from fraudulent job postings with our AI-powered detection system.</p>
            <div className="auth-panel-features">
              <div className="auth-panel-feature">
                <span>🛡️</span>
                <span>AI-powered fake job detection</span>
              </div>
              <div className="auth-panel-feature">
                <span>📊</span>
                <span>Track your analysis history</span>
              </div>
              <div className="auth-panel-feature">
                <span>⚡</span>
                <span>Instant multi-modal analysis</span>
              </div>
            </div>
          </div>
        </div>

        {/* Form Panel */}
        <div className="auth-form-panel">
          <div className="auth-form-header">
            <h2>Sign In</h2>
            <p>Enter your credentials to access your account</p>
          </div>

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="input-group">
              <label className="input-label" htmlFor="signin-email">Email</label>
              <input
                id="signin-email"
                className="input-field"
                type="email"
                name="email"
                placeholder="you@example.com"
                value={formData.email}
                onChange={handleChange}
                autoComplete="email"
              />
            </div>

            <div className="input-group">
              <label className="input-label" htmlFor="signin-password">Password</label>
              <div className="password-wrapper">
                <input
                  id="signin-password"
                  className="input-field"
                  type={showPassword ? 'text' : 'password'}
                  name="password"
                  placeholder="Enter your password"
                  value={formData.password}
                  onChange={handleChange}
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? 'Hide' : 'Show'}
                </button>
              </div>
            </div>

            <div className="auth-options">
              <label className="auth-remember">
                <input
                  type="checkbox"
                  checked={remember}
                  onChange={(e) => setRemember(e.target.checked)}
                />
                Remember me
              </label>
              <button type="button" className="auth-forgot">Forgot password?</button>
            </div>

            <button type="submit" className="btn btn-primary btn-lg" style={{ width: '100%' }}>
              Sign In
            </button>
          </form>

          <div className="auth-divider">
            <span>or continue with</span>
          </div>

          <div className="auth-social-buttons">
            <button type="button" className="auth-social-btn">
              <span>G</span> Google
            </button>
            <button type="button" className="auth-social-btn">
              <span>GH</span> GitHub
            </button>
          </div>

          <div className="auth-footer">
            Don't have an account? <Link to="/signup">Sign Up</Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default SignIn;
