// src/pages/AIAnalyticsHub/AIAnalyticsHub.jsx
import React, { useState } from "react";
import KPISection from "./KPISection";
import FilterPanel from "./FilterPanel";
import InsightPanel from "./InsightPanel";
import AnalyticsFuelTable from "./AnalyticsFuelTable";
import { FiRefreshCw, FiDownload } from "react-icons/fi";

export default function AIAnalyticsHub({
  dbHistory = [],
  activeTheme = { bg: "#0b1329", card: "#111c44", border: "#1b2a4a", text: "#ffffff" },
  isDarkMode,
  getImageUrl,
  handleImageZoom,
  zoomImage,
  sourceRect,
  zoomPosition
}) {
  const [filters, setFilters] = useState({
    search: "",
    dateRange: "",
    storage: "All Selected (8)",
    techId: "Select Tech ID",
    aiStatus: "All Statuses",
    ocrStatus: "All Statuses"
  });

  // State Utama untuk Kontrol Sidebar (Buka/Tutup)
  const [isSidebarCollapsed, setIsSidebarCollapsed] = useState(false);

  return (
    <div
      style={{
        height: "100vh",
        background: "#080d1e",
        color: "#ffffff",
        display: "flex",
        flexDirection: "column",
        gap: "14px",
        padding: "16px",
        boxSizing: "border-box",
        overflow: "hidden",
        fontFamily: "'Segoe UI', Roboto, Helvetica, Arial, sans-serif"
      }}
    >
      {/* ================================================= */}
      {/* HEADER ZONE */}
      {/* ================================================= */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          height: "48px",
          minHeight: "48px"
        }}
      >
        <div>
          <div style={{ fontSize: "20px", fontWeight: "800", color: "#38bdf8", letterSpacing: "0.5px" }}>
            AI Analytics Hub <span style={{ fontSize: "12px", color: "#64748b", fontWeight: "normal" }}>(Read Only)</span>
          </div>
          <div style={{ fontSize: "11px", color: "#94a3b8", marginTop: "2px" }}>
            Read Only Analytics Platform
          </div>
        </div>

        {/* Action Buttons Right Side */}
        <div style={{ display: "flex", gap: "10px" }}>
          <button
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              background: "#1e293b",
              border: "1px solid #334155",
              color: "#94a3b8",
              padding: "6px 12px",
              borderRadius: "6px",
              fontSize: "12px",
              cursor: "pointer",
              fontWeight: "500"
            }}
          >
            <FiRefreshCw size={13} /> Refresh Data
          </button>
          <button
            style={{
              display: "flex",
              alignItems: "center",
              gap: "6px",
              background: "#15803d",
              border: "1px solid #166534",
              color: "#ffffff",
              padding: "6px 12px",
              borderRadius: "6px",
              fontSize: "12px",
              cursor: "pointer",
              fontWeight: "500"
            }}
          >
            <FiDownload size={13} /> Export
          </button>
        </div>
      </div>

      {/* ================================================= */}
      {/* KPI CARDS COMPONENT */}
      {/* ================================================= */}
      <KPISection dbHistory={dbHistory} activeTheme={activeTheme} />

      {/* ================================================= */}
      {/* FILTER CONTROL PANEL */}
      {/* ================================================= */}
      <FilterPanel filters={filters} setFilters={setFilters} activeTheme={activeTheme} />

      {/* ================================================= */}
      {/* MAIN DATA GRID (RESPONSIVE SEJAJAR PANEL SIDEBAR) */}
      {/* ================================================= */}
      <div
        style={{
          flex: 1,
          display: "grid",
          // Lebar kolom kanan berubah dinamis: 280px (buka) atau 50px (collapse)
          gridTemplateColumns: isSidebarCollapsed ? "1fr 50px" : "1fr 280px", 
          gap: "14px",
          minHeight: 0,
          transition: "grid-template-columns 0.3s cubic-bezier(0.4, 0, 0.2, 1)"
        }}
      >
        {/* LEFT WORKSPACE: DATA TABLE (Otomatis melebar jika sidebar collapse) */}
        <div
          style={{
            background: "#0b1528",
            border: "1px solid #1e293b",
            borderRadius: "8px",
            display: "flex",
            flexDirection: "column",
            minHeight: 0,
            overflow: "hidden"
          }}
        >
          <div
            style={{
              padding: "12px 16px",
              borderBottom: "1px solid #1e293b",
              fontSize: "12px",
              fontWeight: "800",
              color: "#38bdf8",
              letterSpacing: "0.5px"
            }}
          >
            AI ANALYTICS DATA
          </div>

          <div style={{ flex: 1, display: "flex", flexDirection: "column", minHeight: 0 }}>
            <AnalyticsFuelTable
              data={dbHistory}
              activeTheme={activeTheme}
              isDarkMode={isDarkMode}
              getImageUrl={getImageUrl}
              handleImageZoom={handleImageZoom}
              zoomImage={zoomImage}
              sourceRect={sourceRect}
              zoomPosition={zoomPosition}
            />
          </div>
        </div>

        {/* RIGHT WORKSPACE: SIDEBAR COMPONENT */}
        <div style={{ minHeight: 0, display: "flex", flexDirection: "column" }}>
          <InsightPanel 
            dbHistory={dbHistory} 
            activeTheme={activeTheme} 
            isCollapsed={isSidebarCollapsed}
            setIsCollapsed={setIsSidebarCollapsed}
          />
        </div>
      </div>
    </div>
  );
}