import React from 'react';

export const LoadingSpinner = ({ size = 48, text = 'Analyzing...' }) => {
  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      gap: 'var(--space-6)',
      padding: 'var(--space-12) 0',
    }}>
      <div style={{
        width: size,
        height: size,
        position: 'relative',
      }}>
        <div style={{
          position: 'absolute',
          inset: 0,
          border: '3px solid var(--border-subtle)',
          borderTopColor: 'var(--accent-primary)',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }} />
        <div style={{
          position: 'absolute',
          inset: '6px',
          border: '3px solid var(--border-subtle)',
          borderBottomColor: 'var(--accent-secondary)',
          borderRadius: '50%',
          animation: 'spin 1.2s linear infinite reverse',
        }} />
      </div>
      {text && (
        <p style={{
          color: 'var(--text-secondary)',
          fontSize: 'var(--font-size-sm)',
          fontWeight: 'var(--font-weight-medium)',
          animation: 'pulse 1.5s ease-in-out infinite',
        }}>
          {text}
        </p>
      )}
    </div>
  );
};

export const Skeleton = ({ width = '100%', height = '20px', radius = 'var(--radius-md)' }) => {
  return (
    <div
      className="skeleton"
      style={{ width, height, borderRadius: radius }}
    />
  );
};
