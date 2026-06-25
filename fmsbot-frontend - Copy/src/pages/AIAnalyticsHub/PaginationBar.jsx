import React from "react";

export default function PaginationBar() {
  return (
    <div
      style={{
        padding: "10px 16px",
        borderTop: "1px solid #1e293b",
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        background: "#0b1528"
      }}
    >
      <div style={{ fontSize: "11px", color: "#64748b" }}>
        Showing 1 to 50 of 5,915 records
      </div>
      <div style={{ display: "flex", gap: "5px", alignItems: "center" }}>
        <button style={btnStyle}>&laquo;</button>
        <button style={{ ...btnStyle, background: "#38bdf8", color: "#0f172a", fontWeight: "bold" }}>1</button>
        <button style={btnStyle}>2</button>
        <button style={btnStyle}>3</button>
        <button style={btnStyle}>4</button>
        <button style={btnStyle}>5</button>
        <span style={{ color: "#475569", fontSize: "11px" }}>...</span>
        <button style={btnStyle}>119</button>
        <button style={btnStyle}>&raquo;</button>
      </div>
    </div>
  );
}

const btnStyle = {
  background: "#0f172a",
  border: "1px solid #334155",
  borderRadius: "4px",
  color: "#94a3b8",
  padding: "4px 8px",
  fontSize: "11px",
  cursor: "pointer"
};