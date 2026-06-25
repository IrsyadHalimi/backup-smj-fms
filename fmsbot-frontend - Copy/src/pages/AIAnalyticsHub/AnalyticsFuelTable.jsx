// src/pages/AIAnalyticsHub/AnalyticsFuelTable.jsx
import React, { useState } from "react";
import { FaRegImage } from "react-icons/fa";
import { HiOutlineSquares2X2, HiOutlineBars4 } from "react-icons/hi2";
import PaginationBar from "./PaginationBar";

export default function AnalyticsFuelTable() {
  const [viewMode, setViewMode] = useState("table"); // 'table' atau 'grid'
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [hoverBtn, setHoverBtn] = useState(null);

  const dummyRows = [
    { sapId: "DT2027", techId: "DT13-WD", storage: "FT2802", datetime: "16/06/2026 20:30", hmDev: "0", hmAi: "12450", hmFv: "12450", flowDev: "0", flowAi: "2478550", flowFv: "2478550", qty: "12", status: "PASS" },
    { sapId: "EX5009", techId: "PC1250-09", storage: "FT2802", datetime: "16/06/2026 20:17", hmDev: "1", hmAi: "38204", hmFv: "38203", flowDev: "-5", flowAi: "2478538", flowFv: "2478543", qty: "839", status: "PASS" },
    { sapId: "DZ2013", techId: "DZ8-08", storage: "FT2802", datetime: "16/06/2026 19:59", hmDev: "0", hmAi: "6570", hmFv: "6570", flowDev: "0", flowAi: "2477699", flowFv: "2477699", qty: "347", status: "PASS" },
    { sapId: "DZ2005", techId: "DZ8T-01", storage: "FT2802", datetime: "16/06/2026 19:52", hmDev: "0", hmAi: "18702", hmFv: "18702", flowDev: "2", flowAi: "2477352", flowFv: "2477350", qty: "349", status: "PASS" }
  ];

  const thStyle = { background: "#0f172a", color: "#94a3b8", fontSize: "10px", fontWeight: "700", padding: "6px 4px", borderBottom: "1px solid #1e293b", borderRight: "1px solid #1e293b", textAlign: "center" };
  const tdStyle = { padding: "6px 6px", fontSize: "11px", color: "#e2e8f0", borderBottom: "1px solid #1e293b", borderRight: "1px solid #1e293b", textAlign: "center" };

  const viewBtnStyle = (modeName) => ({
    background: viewMode === modeName ? "#1e293b" : (hoverBtn === modeName ? "rgba(30, 41, 59, 0.6)" : "transparent"),
    border: "none",
    color: viewMode === modeName ? "#38bdf8" : (hoverBtn === modeName ? "#cbd5e1" : "#64748b"),
    padding: "5px",
    borderRadius: "3px",
    cursor: "pointer",
    display: "flex",
    alignItems: "center",
    transition: "all 0.2s ease"
  });

  return (
    <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0, position: "relative" }}>
      
      {/* TOOLBAR KONTROL (Rows per page & View Switcher)
        Mengubah nilai 'top' dari -34px ke -39px agar posisi vertikalnya naik 
        dan sejajar sempurna di tengah-tengah teks judul "AI ANALYTICS DATA"
      */}
      <div style={{ position: "absolute", top: "-39px", right: "12px", display: "flex", alignItems: "center", gap: "12px", zIndex: 10, height: "26px" }}>
        
        {/* Dropdown Rows Per Page */}
        <div style={{ display: "flex", alignItems: "center", gap: "6px", height: "100%" }}>
          <span style={{ color: "#64748b", fontSize: "11px", fontWeight: "600", lineHeight: "1" }}>Rows per page:</span>
          <select
            value={rowsPerPage}
            onChange={(e) => setRowsPerPage(Number(e.target.value))}
            style={{ background: "#0f172a", border: "1px solid #334155", borderRadius: "4px", color: "#ffffff", padding: "2px 6px", fontSize: "11px", outline: "none", height: "24px", display: "flex", alignItems: "center" }}
          >
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
          </select>
        </div>

        {/* Toggle View Mode Button */}
        <div style={{ display: "flex", gap: "4px", background: "#0f172a", padding: "2px", borderRadius: "4px", border: "1px solid #1e293b", alignItems: "center", height: "24px", boxSizing: "border-box" }}>
          <button 
            onClick={() => setViewMode("table")} 
            style={viewBtnStyle("table")}
            onMouseEnter={() => setHoverBtn("table")}
            onMouseLeave={() => setHoverBtn(null)}
            title="List View"
          >
            <HiOutlineBars4 size={14} />
          </button>
          <button 
            onClick={() => setViewMode("grid")} 
            style={viewBtnStyle("grid")}
            onMouseEnter={() => setHoverBtn("grid")}
            onMouseLeave={() => setHoverBtn(null)}
            title="Thumbnail View"
          >
            <HiOutlineSquares2X2 size={14} />
          </button>
        </div>

      </div>

      {/* Area Render Content Viewport */}
      <div style={{ flex: 1, overflowY: "auto", minHeight: 0, padding: viewMode === "grid" ? "12px" : "0" }}>
        {viewMode === "table" ? (
          <table style={{ width: "100%", borderCollapse: "collapse" }}>
            <thead>
              <tr>
                <th rowSpan={2} style={thStyle}>SAP ID</th>
                <th rowSpan={2} style={thStyle}>Tech ID</th>
                <th rowSpan={2} style={thStyle}>Storage</th>
                <th rowSpan={2} style={thStyle}>Date/Time</th>
                <th colSpan={4} style={thStyle}>HM/KM UNIT</th>
                <th colSpan={4} style={thStyle}>FLOW METER</th>
                <th rowSpan={2} style={thStyle}>Consumed (L)</th>
                <th rowSpan={2} style={thStyle}>Status</th>
              </tr>
              <tr>
                <th style={thStyle}>FOTO</th><th style={thStyle}>DEV</th><th style={thStyle}>AI</th><th style={thStyle}>FV</th>
                <th style={thStyle}>FOTO</th><th style={thStyle}>DEV</th><th style={thStyle}>AI</th><th style={thStyle}>FV</th>
              </tr>
            </thead>
            <tbody>
              {dummyRows.map((row, idx) => (
                <tr key={idx} style={{ background: idx % 2 === 0 ? "transparent" : "rgba(15, 23, 42, 0.3)" }}>
                  <td style={tdStyle}>{row.sapId}</td>
                  <td style={{ ...tdStyle, fontWeight: "600" }}>{row.techId}</td>
                  <td style={{ ...tdStyle, color: "#38bdf8" }}>{row.storage}</td>
                  <td style={tdStyle}>{row.datetime}</td>
                  <td style={tdStyle}><FaRegImage style={{ color: "#38bdf8", cursor: "pointer" }} /></td>
                  <td style={{ ...tdStyle, color: row.hmDev !== "0" ? "#ef4444" : "#e2e8f0" }}>{row.hmDev}</td>
                  <td style={tdStyle}>{row.hmAi}</td>
                  <td style={tdStyle}>{row.hmFv}</td>
                  <td style={tdStyle}><FaRegImage style={{ color: "#38bdf8", cursor: "pointer" }} /></td>
                  <td style={{ ...tdStyle, color: row.flowDev !== "0" ? "#ef4444" : "#e2e8f0" }}>{row.flowDev}</td>
                  <td style={tdStyle}>{row.flowAi}</td>
                  <td style={tdStyle}>{row.flowFv}</td>
                  <td style={{ ...tdStyle, color: "#22c55e", fontWeight: "700" }}>{row.qty}</td>
                  <td style={tdStyle}>
                    <span style={{ background: "#14532d", color: "#4ade80", padding: "2px 6px", borderRadius: "4px", fontSize: "10px", fontWeight: "600" }}>
                      {row.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        ) : (
          /* THUMBNAIL GRID VIEW */
          <div style={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: "12px" }}>
            {dummyRows.map((row, idx) => (
              <div key={idx} style={{ background: "#0b1528", border: "1px solid #1e293b", borderRadius: "6px", padding: "12px", display: "flex", flexDirection: "column", gap: "8px" }}>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", borderBottom: "1px solid #1e293b", paddingBottom: "6px" }}>
                  <span style={{ fontSize: "13px", fontWeight: "800", color: "#ffffff" }}>{row.techId}</span>
                  <span style={{ background: "#1e293b", color: "#38bdf8", padding: "2px 6px", borderRadius: "4px", fontSize: "10px", fontWeight: "700" }}>{row.storage}</span>
                </div>
                <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: "6px" }}>
                  <div style={{ background: "#020617", height: "65px", borderRadius: "4px", border: "1px solid #334155", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "4px" }}>
                    <span style={{ fontSize: "8px", color: "#64748b", fontWeight: "700" }}>HM/KM METER</span>
                    <FaRegImage style={{ color: "#64748b" }} size={16} />
                    <span style={{ fontSize: "10px", color: "#cbd5e1" }}>AI: {row.hmAi}</span>
                  </div>
                  <div style={{ background: "#020617", height: "65px", borderRadius: "4px", border: "1px solid #334155", display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: "4px" }}>
                    <span style={{ fontSize: "8px", color: "#64748b", fontWeight: "700" }}>FLOW METER</span>
                    <FaRegImage style={{ color: "#64748b" }} size={16} />
                    <span style={{ fontSize: "10px", color: "#cbd5e1" }}>AI: {row.flowAi}</span>
                  </div>
                </div>
                <div style={{ fontSize: "11px", color: "#94a3b8", display: "flex", flexDirection: "column", gap: "2px" }}>
                  <div>SAP ID: <span style={{ color: "#ffffff" }}>{row.sapId}</span></div>
                  <div>Time: <span style={{ color: "#ffffff" }}>{row.datetime}</span></div>
                </div>
                <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center", marginTop: "4px", paddingTop: "6px", borderTop: "1px solid #1e293b" }}>
                  <span>Vol: <strong style={{ color: "#22c55e", fontSize: "13px" }}>{row.qty} L</strong></span>
                  <span style={{ background: "#14532d", color: "#4ade80", padding: "2px 6px", borderRadius: "4px", fontSize: "9px", fontWeight: "700" }}>{row.status}</span>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
      <PaginationBar />
    </div>
  );
}