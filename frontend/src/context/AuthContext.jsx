import React, { createContext, useState, useEffect, useContext } from 'react';

// Create Context
const AuthContext = createContext();

// Custom hook to use AuthContext
export const useAuth = () => useContext(AuthContext);

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(true);

  // On mount, check if user exists in LocalStorage
  useEffect(() => {
    const storedUser = localStorage.getItem('shieldai_user');
    if (storedUser) {
      try {
        const parsedUser = JSON.parse(storedUser);
        if (!parsedUser.userId && !parsedUser.id && !parsedUser._id) {
          // Legacy mock user detected, log them out
          console.warn("Legacy session detected. Clearing storage.");
          localStorage.removeItem('shieldai_user');
          setUser(null);
        } else {
          setUser(parsedUser);
        }
      } catch (e) {
        localStorage.removeItem('shieldai_user');
        setUser(null);
      }
    }
    setLoading(false);
  }, []);

  // Login function
  const login = (userData) => {
    setUser(userData);
    localStorage.setItem('shieldai_user', JSON.stringify(userData));
  };

  // Logout function
  const logout = () => {
    setUser(null);
    localStorage.removeItem('shieldai_user');
  };

  return (
    <AuthContext.Provider value={{ user, login, logout, loading }}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
