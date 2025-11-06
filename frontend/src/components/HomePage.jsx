import React from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import "./HomePage.css";

function HomePage() {
  const navigate = useNavigate();

  return (
    <>
      <Navbar />
      <div className="home-container">
        <div className="home-content">
          <h1>Welcome to Fake Job Detection System</h1>
          <p>
            Our AI-powered platform helps you verify job postings and detect
            potential fake listings instantly. Stay safe and informed while
            applying for your dream job.
          </p>
          <button
            className="home-detect-btn"
            onClick={() => navigate("/detection")}
          >
            Start Detection
          </button>
        </div>
      </div>
    </>
  );
}

export default HomePage;
