import { useState } from "react";
import "./DetectionPage.css";

export default function DetectionPage() {
  const [text, setText] = useState("");
  const [loading, setLoading] = useState(false);
  const [result, setResult] = useState(null);

  const handleAnalyze = async () => {
    setLoading(true);

    // TEMP (will replace with backend API)
    setTimeout(() => {
      setResult({
        label: "FAKE",
        confidence: 0.93,
        reasons: [
          "Urgent language detected",
          "No interview mentioned",
          "Suspicious keywords detected"
        ]
      });
      setLoading(false);
    }, 1200);
  };

  return (
    <div className="detection-wrapper">
      <aside className="history-panel">
        <h3>History</h3>
        <div className="history-item">Fake Job Example</div>
        <div className="history-item">Internship Scam</div>
      </aside>

      <main className="detection-container">
        <h2>Analyze Job Description</h2>

        <textarea
          rows="6"
          placeholder="Paste job description here..."
          value={text}
          onChange={(e) => setText(e.target.value)}
        />

        <button onClick={handleAnalyze} disabled={loading}>
          {loading ? "Analyzing..." : "Analyze"}
        </button>

        {result && (
          <div className={`result-card ${result.label.toLowerCase()}`}>
            <h3>{result.label}</h3>
            <p>Confidence: {(result.confidence * 100).toFixed(1)}%</p>
            <ul>
              {result.reasons.map((r, i) => (
                <li key={i}>⚠️ {r}</li>
              ))}
            </ul>
          </div>
        )}
      </main>
    </div>
  );
}
