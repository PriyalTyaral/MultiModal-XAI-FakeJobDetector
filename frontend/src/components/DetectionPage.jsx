import React, { useState, useEffect} from "react";
import { useNavigate } from "react-router-dom";
import Navbar from "./Navbar";
import "./DetectionPage.css";

function DetectionPage() {
  const [jobText, setJobText] = useState("");
  const [file, setFile] = useState(null);
  const [analyzing, setAnalyzing] = useState(false);
  const [history, setHistory] = useState([]);

  const navigate = useNavigate();
useEffect(() => {
  const user = localStorage.getItem("user");
  if (!user) navigate("/login");
}, []);

  const handleSubmitText = (e) => {
    e.preventDefault();
    if (!jobText.trim()) return alert("Enter job text first!");
    setAnalyzing(true);
    setTimeout(() => {
      const newEntry = { type: "text", content: jobText, result: "Analyzed ✅" };
      setHistory([newEntry, ...history]);
      setJobText("");
      setAnalyzing(false);
    }, 1000);
  };

  const handleFileChange = (e) => setFile(e.target.files[0]);

  const handleFileSubmit = (e) => {
    e.preventDefault();
    if (!file) return alert("Please select a file first!");
    setAnalyzing(true);
    setTimeout(() => {
      const newEntry = { type: "file", content: file.name, result: "Analyzed ✅" };
      setHistory([newEntry, ...history]);
      setFile(null);
      setAnalyzing(false);
    }, 1000);
  };

  return (
    <>
      <Navbar />
      <div className="detection-wrapper">
        {/* History Sidebar */}
        <aside className="history-panel">
          <h3>History</h3>
          {history.length === 0 ? (
            <p className="empty-history">No history yet</p>
          ) : (
            history.map((item, index) => (
              <div key={index} className="history-item">
                <strong>{item.type === "file" ? "📄 File" : "📝 Text"}</strong>
                <p>{item.content}</p>
                <span>{item.result}</span>
              </div>
            ))
          )}
        </aside>

        {/* Main Detection Section */}
        <div className="detection-container">
          <h2>Fake Job Detection</h2>
          <p>Upload a job posting or paste text to analyze authenticity.</p>

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

          <form onSubmit={handleFileSubmit} className="file-form">
            <input type="file" onChange={handleFileChange} />
            <button type="submit" disabled={analyzing}>
              {analyzing ? "Analyzing..." : "Submit File"}
            </button>
          </form>
        </div>
      </div>
    </>
  );
}

export default DetectionPage;
