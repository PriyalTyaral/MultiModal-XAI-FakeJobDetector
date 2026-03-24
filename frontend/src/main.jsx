import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";

// Global Styles
// These styles cascade down throughout the entire application.
// variable.css holds CSS custom properties for theming centrally.
import "./styles/variables.css";
import "./styles/global.css";
import "./styles/components.css";

/**
 * Entry point of the React application.
 * Wraps the App component with BrowserRouter to enable client-side routing.
 */
ReactDOM.createRoot(document.getElementById("root")).render(
  <BrowserRouter>
    <App />
  </BrowserRouter>
);
