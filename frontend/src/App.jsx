import React from "react";
import { Outlet } from "react-router-dom";
import "./App.css";

function App() {
  return (
    <div className="app">
      <div className="content">
        {/* Renders HomePage, Login, or Register */}
        <Outlet />
      </div>
    </div>
  );
}

export default App;
