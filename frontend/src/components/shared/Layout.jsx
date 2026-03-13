import React from 'react';
import Navbar from './Navbar';
import Footer from './Footer';
import { ToastProvider } from './Toast';

const Layout = ({ children }) => {
  return (
    <ToastProvider>
      <div style={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
        <Navbar />
        <main style={{ flex: 1, paddingTop: 'var(--navbar-height)' }}>
          {children}
        </main>
        <Footer />
      </div>
    </ToastProvider>
  );
};

export default Layout;
