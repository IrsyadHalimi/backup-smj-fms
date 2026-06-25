// src/pages/AIAnalyticsHub/FilterPanel.jsx
import React, { useState } from "react";
import { FiRefreshCw, FiTrash2 } from "react-icons/fi";

export default function FilterPanel() {
  const [hoverBtn, setHoverBtn] = useState(null);
  
  // State untuk data pencarian terfilter (Mock Data List)
  const [storageInput, setStorageInput] = useState("");
  const [techInput, setTechInput] = useState("");
  const [sapInput, setSapInput] = useState(""); // State baru untuk SAP ID
  const [shiftInput, setShiftInput] = useState("All Shifts"); // State baru untuk Shift
  const [showStorageList, setShowStorageList] = useState(false);
  const [showTechList, setShowTechList] = useState(false);

  const storageOptions = ["FT2801", "FT2802", "FT2601", "FT2503", "FT1001"];
  const techOptions = ["DT13-WD", "PC1250-09", "DZ8-08", "DZ8T-01", "EC210-04"];

  const baseInputStyle = {
    background: "#0f172a",
    border: "1px solid #334155",
    borderRadius: "4px",
    color: "#ffffff",
    padding: "6px 10px",
    fontSize: "11px",
    outline: "none",
    width: "100%",
    boxSizing: "border-box",
    height: "28px"
  };

  const miniBtnStyle = (btnKey, activeColor, activeBg) => ({
    display: "flex",
    alignItems: "center",
    gap: "4px",
    background: hoverBtn === btnKey ? activeBg : "transparent",
    border: `1px solid ${hoverBtn === btnKey ? activeBg : "#334155"}`,
    color: hoverBtn === btnKey ? activeColor : "#94a3b8",
    padding: "0 10px",
    borderRadius: "4px",
    fontSize: "11px",
    fontWeight: "600",
    cursor: "pointer",
    height: "28px",
    transition: "all 0.2s ease",
    alignSelf: "end"
  });

  return (
    <div style={{ background: "#0b1528", border: "1px solid #1e293b", borderRadius: "6px", padding: "10px 12px" }}>
      {/* gridTemplateColumns diubah menjadi 9 kolom agar muat dalam 1 row secara proporsional */}
      <div style={{ display: "grid", gridTemplateColumns: "1.1fr 1.1fr 1.1fr 1fr 0.9fr 0.9fr 0.8fr auto auto", gap: "8px", alignItems: "center" }}>
        
        {/* Date Picker Range Box */}
        <div>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>DATE PERIOD</label>
          <div style={{ display: "flex", gap: "3px", alignItems: "center" }}>
            <input type="date" defaultValue="2026-06-01" style={{ ...baseInputStyle, padding: "4px" }} />
            <span style={{ color: "#475569", fontSize: "10px" }}>to</span>
            <input type="date" defaultValue="2026-06-20" style={{ ...baseInputStyle, padding: "4px" }} />
          </div>
        </div>

        {/* Searchable Storage Autocomplete */}
        <div style={{ position: "relative" }}>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>SEARCH STORAGE</label>
          <input
            type="text"
            placeholder="Type Storage ID..."
            value={storageInput}
            onChange={(e) => { setStorageInput(e.target.value); setShowStorageList(true); }}
            onFocus={() => setShowStorageList(true)}
            onBlur={() => setTimeout(() => setShowStorageList(false), 200)}
            style={baseInputStyle}
          />
          {showStorageList && (
            <div style={{ position: "absolute", top: "42px", left: 0, right: 0, background: "#0f172a", border: "1px solid #334155", zIndex: 10, borderRadius: "4px", maxHeight: "120px", overflowY: "auto" }}>
              {storageOptions.filter(o => o.toLowerCase().includes(storageInput.toLowerCase())).map(o => (
                <div key={o} onClick={() => setStorageInput(o)} style={{ padding: "6px 10px", fontSize: "11px", color: "#cbd5e1", cursor: "pointer" }} onMouseDown={() => setStorageInput(o)}>
                  {o}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Searchable Tech ID Autocomplete */}
        <div style={{ position: "relative" }}>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>SEARCH TECH ID</label>
          <input
            type="text"
            placeholder="Type Unit Code..."
            value={techInput}
            onChange={(e) => { setTechInput(e.target.value); setShowTechList(true); }}
            onFocus={() => setShowTechList(true)}
            onBlur={() => setTimeout(() => setShowTechList(false), 200)}
            style={baseInputStyle}
          />
          {showTechList && (
            <div style={{ position: "absolute", top: "42px", left: 0, right: 0, background: "#0f172a", border: "1px solid #334155", zIndex: 10, borderRadius: "4px", maxHeight: "120px", overflowY: "auto" }}>
              {techOptions.filter(o => o.toLowerCase().includes(techInput.toLowerCase())).map(o => (
                <div key={o} onClick={() => setTechInput(o)} style={{ padding: "6px 10px", fontSize: "11px", color: "#cbd5e1", cursor: "pointer" }} onMouseDown={() => setTechInput(o)}>
                  {o}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* New Field: Search SAP ID */}
        <div>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>SEARCH SAP ID</label>
          <input
            type="text"
            placeholder="Search SAP ID..."
            value={sapInput}
            onChange={(e) => setSapInput(e.target.value)}
            style={baseInputStyle}
          />
        </div>

        {/* Status Dropdowns */}
        <div>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>AI STATUS</label>
          <select style={baseInputStyle}><option>All Statuses</option><option>PASS</option><option>ERROR</option></select>
        </div>

        <div>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>OCR STATUS</label>
          <select style={baseInputStyle}><option>All Statuses</option><option>SUCCESS</option><option>FAILED</option></select>
        </div>

        {/* New Field: Shift Dropdown */}
        <div>
          <label style={{ display: "block", fontSize: "9px", color: "#64748b", fontWeight: "700", marginBottom: "3px" }}>SHIFT</label>
          <select 
            style={baseInputStyle}
            value={shiftInput}
            onChange={(e) => setShiftInput(e.target.value)}
          >
            <option>All Shifts</option>
            <option>Day</option>
            <option>Night</option>
          </select>
        </div>

        {/* Minified Buttons with Hover Effects */}
        <button
          style={miniBtnStyle("search", "#0f172a", "#38bdf8")}
          onMouseEnter={() => setHoverBtn("search")}
          onMouseLeave={() => setHoverBtn(null)}
        >
          <FiRefreshCw size={11} /> Search
        </button>

        <button
          style={miniBtnStyle("reset", "#ffffff", "#ef4444")}
          onMouseEnter={() => setHoverBtn("reset")}
          onMouseLeave={() => setHoverBtn(null)}
        >
          <FiTrash2 size={11} /> Reset
        </button>

      </div>
    </div>
  );
}