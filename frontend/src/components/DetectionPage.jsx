import React, { useState } from "react";
import "./DetectionPage.css";

function DetectionPage() {
  const [jobText, setJobText] = useState("");
  const [file, setFile] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);

  const handleSubmitText = (e) => {
    e.preventDefault();
    setAnalyzing(true);
    setTimeout(() => {
      alert("Job text analyzed successfully!");
      setAnalyzing(false);
    }, 1500);
  };

  const handleFileChange = (e) => {
    setFile(e.target.files[0]);
  };

  const handleFileSubmit = (e) => {
    e.preventDefault();
    if (!file) {
      alert("Please select a file first!");
      return;
    }
    setAnalyzing(true);
    setTimeout(() => {
      alert("File analyzed successfully!");
      setAnalyzing(false);
    }, 1500);
  };

  return (
    <div className="detection-container">
      <h2>Fake Job Detection</h2>
      <p>Upload a job posting or paste its text to analyze authenticity.</p>

      {/* Text Input Form */}
      <form onSubmit={handleSubmitText} className="text-form">
        <textarea
          placeholder="Paste your job posting text here..."
          value={jobText}
          onChange={(e) => setJobText(e.target.value)}
        ></textarea>
        <button type="submit" disabled={analyzing}>
          {analyzing ? "Analyzing..." : "Submit Text"}
        </button>
      </form>

      <div className="divider">or</div>

      {/* File Upload Form */}
      <form onSubmit={handleFileSubmit} className="file-form">
        <input type="file" onChange={handleFileChange} />
        <div className="file-options">
          <label>
            <input type="radio" name="fileType" value="image" /> Image
          </label>
          <label>
            <input type="radio" name="fileType" value="document" /> Document
          </label>
        </div>
        <button type="submit" disabled={analyzing}>
          {analyzing ? "Analyzing..." : "Submit File"}
        </button>
      </form>
    </div>
  );
}

export default DetectionPage;
