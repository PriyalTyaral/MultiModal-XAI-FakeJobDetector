import React, { useState } from 'react';
import styles from '../../styles/components.css';

/**
 * CompanyNameValidator Component
 * 
 * Provides front-end validation for company name input with real-time feedback.
 * - Mandatory field validation
 * - Format validation (length, special characters)
 * - Real-time error messages
 * 
 * Usage:
 * <CompanyNameValidator 
 *   value={companyName}
 *   onChange={handleCompanyNameChange}
 *   error={errors.companyName}
 * />
 */

const CompanyNameValidator = ({ 
  value = '', 
  onChange, 
  error = null, 
  touched = false,
  disabled = false 
}) => {
  const [isFocused, setIsFocused] = useState(false);

  // ────────────────────────────────────────────────────────
  // VALIDATION RULES
  // ────────────────────────────────────────────────────────
  const validateCompanyName = (name) => {
    // Rule 1: Required field
    if (!name || name.trim() === '') {
      return 'Company name is required';
    }

    // Rule 2: Minimum length
    if (name.trim().length < 2) {
      return 'Company name must be at least 2 characters';
    }

    // Rule 3: Maximum length
    if (name.length > 100) {
      return 'Company name must not exceed 100 characters';
    }

    // Rule 4: Check for valid characters
    // Allow: letters, numbers, spaces, hyphens, ampersands, dots, apostrophes, parentheses
    const validPattern = /^[a-zA-Z0-9\s\-&'.(),]*$/;
    if (!validPattern.test(name)) {
      return 'Company name contains invalid characters';
    }

    return null;
  };

  // ────────────────────────────────────────────────────────
  // HANDLERS
  // ────────────────────────────────────────────────────────
  const handleChange = (e) => {
    const newValue = e.target.value;
    onChange(newValue);
  };

  const handleBlur = () => {
    setIsFocused(false);
  };

  const handleFocus = () => {
    setIsFocused(true);
  };

  // ────────────────────────────────────────────────────────
  // VALIDATION STATE
  // ────────────────────────────────────────────────────────
  const validationError = error || validateCompanyName(value);
  const isInvalid = touched && validationError;
  const isValid = touched && value && !validationError;

  // ────────────────────────────────────────────────────────
  // RENDER
  // ────────────────────────────────────────────────────────
  return (
    <div className="input-group">
      <label htmlFor="companyName" className="input-label">
        Company Name <span className="required">*</span>
      </label>

      <div className="input-wrapper">
        <input
          id="companyName"
          type="text"
          className={`input-field ${isInvalid ? 'input-error' : ''} ${isValid ? 'input-success' : ''}`}
          placeholder="e.g., Microsoft, Apple Inc, Tech Startup Inc"
          value={value}
          onChange={handleChange}
          onFocus={handleFocus}
          onBlur={handleBlur}
          disabled={disabled}
          maxLength="100"
          required
          autoCapitalize="words"
          spellCheck="true"
        />

        {/* Character Count */}
        <div className="input-meta">
          <span className={`char-count ${value.length > 80 ? 'warning' : ''}`}>
            {value.length}/100
          </span>
        </div>
      </div>

      {/* Error Message */}
      {isInvalid && (
        <div className="input-error-message" role="alert">
          <span className="error-icon">❌</span>
          {validationError}
        </div>
      )}

      {/* Success Message */}
      {isValid && (
        <div className="input-success-message" role="status">
          <span className="success-icon">✓</span>
          Company name looks good
        </div>
      )}

      {/* Help Text */}
      {isFocused && (
        <div className="input-helper-text">
          <ul>
            <li>Use the official company name</li>
            <li>Include legal entities (e.g., "Inc", "LLC", "Ltd")</li>
            <li>Example: "Google Inc", "Amazon.com", "Microsoft Corporation"</li>
          </ul>
        </div>
      )}
    </div>
  );
};

export default CompanyNameValidator;
