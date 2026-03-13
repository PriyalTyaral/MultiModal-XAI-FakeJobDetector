import React from "react";
import { Routes, Route } from "react-router-dom";
import Layout from "./components/shared/Layout";

// Page Components
import LandingPage from "./components/LandingPage";
import SignIn from "./components/SignIn";
import SignUp from "./components/SignUp";
import JobAnalysis from "./components/JobAnalysis";
import ResultPage from "./components/ResultPage";
import Dashboard from "./components/Dashboard";
import ReportJob from "./components/ReportJob";
import Profile from "./components/Profile";

/**
 * App Component - The root of the React application.
 * Uses react-router-dom for navigation.
 * All defined routes are wrapped inside the <Layout> component, 
 * which provides the global Navbar and Footer.
 */
function App() {
  return (
    <Layout>
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={<LandingPage />} />
        <Route path="/signin" element={<SignIn />} />
        <Route path="/signup" element={<SignUp />} />
        
        {/* Core Feature Routes */}
        <Route path="/analyze" element={<JobAnalysis />} />
        <Route path="/upload" element={<JobAnalysis />} />
        <Route path="/result" element={<ResultPage />} />
        
        {/* User Specific Routes */}
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/report" element={<ReportJob />} />
        <Route path="/profile" element={<Profile />} />
      </Routes>
    </Layout>
  );
}

export default App;
