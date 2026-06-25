// src/pages/AIAnalyticsHub/InsightPanel.jsx
import React, { useState, useRef, useEffect } from "react";
import { 
  FaDatabase, 
  FaArrowTrendUp, 
  FaTriangleExclamation, 
  FaBullseye, 
  FaGaugeHigh, 
  FaShieldHalved, 
  FaChevronRight, 
  FaChevronLeft 
} from "react-icons/fa6";
import { LuSparkles } from "react-icons/lu";
import { FiSend } from "react-icons/fi";
import { RiRobot2Line } from "react-icons/ri";

export default function InsightPanel({ isCollapsed, setIsCollapsed }) {
  const [hoverBtn, setHoverBtn] = useState(false);
  const [showTooltip, setShowTooltip] = useState(false);
  
  // State untuk Fitur Q&A AI Copilot
  const [query, setQuery] = useState("");
  const [aiResponse, setAiResponse] = useState(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  
  const textareaRef = useRef(null);

  const cards = [
    { title: "Top Storage by Consumption", main: "FT2802", sub: "8,465,220 L", note: "45.83% of total", color: "#00b2fe", icon: <FaDatabase /> },
    { title: "Highest Consumption Record", main: "DT3240", sub: "2,478,550 L", note: "16/06/2026 19:44", color: "#22c55e", icon: <FaArrowTrendUp /> },
    { title: "Most AI Failures", main: "FT2601", sub: "42 Records", note: "This period", color: "#f43f5e", icon: <FaTriangleExclamation /> },
    { title: "OCR Accuracy Rate", main: "96.47%", sub: "Very Good", note: "", color: "#a855f7", icon: <FaBullseye /> },
    { title: "Average Consumption", main: "3,122 L", sub: "Per Record", note: "", color: "#eab308", icon: <FaGaugeHigh /> },
    { title: "Data Coverage", main: "100%", sub: "Complete", note: "", color: "#10b981", icon: <FaShieldHalved /> }
  ];

  // Efek Auto-Resize Tinggi Textarea Input Box (Maksimal 3 Baris)
  useEffect(() => {
    if (textareaRef.current) {
      textareaRef.current.style.height = "24px";
      const scrollHeight = textareaRef.current.scrollHeight;
      textareaRef.current.style.height = `${Math.min(scrollHeight, 60)}px`;
    }
  }, [query]);

  // Simulasi Pengiriman Query ke Model (Gemini / OpenAI Integration Ready)
  const handleSendQuery = (e) => {
    if (e) e.preventDefault();
    if (!query.trim() || isAnalyzing) return;

    setIsAnalyzing(true);
    
    // Simulasi Response Delay 1 detik
    setTimeout(() => {
      setAiResponse(
        `Berdasarkan analisis exploratory data pada storage FT2802, lonjakan konsumsi unit DT3240 dipengaruhi oleh deviasi pembacaan flowmeter digital sebesar +0.4%. Tren menunjukkan akurasi OCR stabil di angka 96.47%, sehingga data pengisian valid untuk dilakukan sinkronisasi langsung ke modul SAP FICO/MM.`
      );
      setIsAnalyzing(false);
    }, 1000);
  };

  return (
    <div 
      style={{ 
        display: "flex", 
        flexDirection: "column", 
        height: "100%", 
        background: "#0b1424", 
        border: "1px solid #1e293b", 
        borderRadius: "8px",
        boxSizing: "border-box",
        padding: isCollapsed ? "12px 4px" : "12px",
        transition: "all 0.3s cubic-bezier(0.4, 0, 0.2, 1)",
        overflow: "hidden",
        position: "relative"
      }}
    >
      {/* Inject Style Animasi Smooth Pop-In */}
      <style>{`
        @keyframes smoothPopUp {
          from { opacity: 0; transform: translateY(8px); }
          to { opacity: 1; transform: translateY(0); }
        }
      `}</style>

      {/* ================================================= */}
      {/* HEADER BAR INSIGHT SUMMARY */}
      {/* ================================================= */}
      <div 
        style={{ 
          display: "flex", 
          justifyContent: isCollapsed ? "center" : "space-between", 
          alignItems: "center", 
          borderBottom: isCollapsed ? "none" : "1px solid #1e293b", 
          paddingBottom: isCollapsed ? "0px" : "10px", 
          marginBottom: "10px",
          height: "28px",
          flexShrink: 0,
          position: "relative"
        }}
      >
        {isCollapsed ? (
          /* Saat Collapse: Gunakan Icon Utama sebagai Pemicu Eksklusif */
          <button
            onClick={() => setIsCollapsed(false)}
            onMouseEnter={() => { setHoverBtn(true); setShowTooltip(true); }}
            onMouseLeave={() => { setHoverBtn(false); setShowTooltip(false); }}
            style={{
              background: hoverBtn ? "#1e293b" : "transparent",
              border: "none",
              color: "#38bdf8",
              width: "32px",
              height: "32px",
              borderRadius: "50%",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              transition: "all 0.2s ease"
            }}
          >
            <LuSparkles size={18} />
          </button>
        ) : (
          /* Saat Uncollapse: Judul & Tombol Panah Keluar */
          <>
            <div style={{ display: "flex", alignItems: "center", gap: "6px", fontSize: "12px", fontWeight: "800", color: "#38bdf8", letterSpacing: "0.5px" }}>
              <LuSparkles size={14} /> AI INSIGHT SUMMARY
            </div>

            <button
              onClick={() => setIsCollapsed(true)}
              onMouseEnter={() => { setHoverBtn(true); setShowTooltip(true); }}
              onMouseLeave={() => { setHoverBtn(false); setShowTooltip(false); }}
              style={{
                background: hoverBtn ? "#1e293b" : "transparent",
                border: "none",
                color: hoverBtn ? "#38bdf8" : "#64748b",
                width: "28px",
                height: "28px",
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                cursor: "pointer",
                transition: "all 0.2s ease",
                outline: "none"
              }}
            >
              <FaChevronRight size={12} />
            </button>
          </>
        )}

        {/* Floating Tooltip Custom ala Gemini */}
        {showTooltip && (
          <div 
            style={{
              position: "absolute",
              top: "34px",
              right: isCollapsed ? "-6px" : "0px",
              background: "#1e293b",
              border: "1px solid #334155",
              color: "#e2e8f0",
              padding: "4px 8px",
              borderRadius: "4px",
              fontSize: "10px",
              fontWeight: "600",
              whiteSpace: "nowrap",
              zIndex: 99,
              boxShadow: "0 4px 6px -1px rgba(0, 0, 0, 0.5)"
            }}
          >
            {isCollapsed ? "Expand AI Insights" : "Collapse Sidebar"}
          </div>
        )}
      </div>

      {/* ================================================= */}
      {/* BODY AREA: CARDS ATAU CONTAINER JAWABAN AI */}
      {/* ================================================= */}
      <div 
        style={{ 
          flex: 1, 
          display: "flex", 
          flexDirection: "column", 
          gap: "5px", 
          minHeight: 0,
          marginBottom: isCollapsed ? "0px" : "10px"
        }}
      >
        {isCollapsed ? (
          /* TAMPILAN COLLAPSE: HANYA STRIP BARIS ICON */
          <div style={{ flex: 1, display: "flex", flexDirection: "column", justifyContent: "space-around", alignItems: "center", padding: "10px 0" }}>
            {cards.map((card, i) => (
              <div 
                key={i} 
                title={`${card.title}: ${card.main}`}
                style={{ 
                  fontSize: "20px", 
                  color: card.color, 
                  background: "rgba(15, 23, 42, 0.5)",
                  width: "34px",
                  height: "34px",
                  borderRadius: "6px",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  border: "1px solid #1e293b"
                }}
              >
                {card.icon}
              </div>
            ))}
          </div>
        ) : aiResponse ? (
          /* TAMPILAN AKTIF: BOX JAWABAN AI COPILOT (MENGGANTIKAN CARD) */
          <div
            style={{
              flex: 1,
              background: "linear-gradient(135deg, #0f172a, #020617)",
              border: "1px solid #38bdf8",
              borderRadius: "6px",
              padding: "12px",
              display: "flex",
              flexDirection: "column",
              minHeight: 0,
              animation: "smoothPopUp 0.4s cubic-bezier(0.16, 1, 0.3, 1) forwards"
            }}
          >
            <div style={{ display: "flex", alignItems: "center", gap: "6px", fontSize: "11px", fontWeight: "700", color: "#38bdf8", marginBottom: "8px", flexShrink: 0 }}>
              <RiRobot2Line size={14} /> AI COPILOT RESPONSE
            </div>
            
            {/* Konten Balasan Eksplorasi */}
            <div style={{ flex: 1, overflowY: "auto", fontSize: "11.5px", lineHeight: "1.5", color: "#cbd5e1", paddingRight: "4px" }}>
              {aiResponse}
            </div>

            <button
              onClick={() => { setAiResponse(null); setQuery(""); }}
              style={{ 
                background: "#1e293b", 
                border: "1px solid #334155", 
                color: "#94a3b8", 
                fontSize: "10px", 
                padding: "4px 10px", 
                borderRadius: "4px", 
                cursor: "pointer", 
                marginTop: "8px", 
                alignSelf: "flex-end",
                fontWeight: "600",
                transition: "all 0.2s"
              }}
              onMouseEnter={(e) => e.target.style.color = "#ffffff"}
              onMouseLeave={(e) => e.target.style.color = "#94a3b8"}
            >
              Back to Summary
            </button>
          </div>
        ) : (
          /* TAMPILAN NORMAL: 6 CARDS PROPORSI SEMPURNA */
          cards.map((card, i) => (
            <div
              key={i}
              style={{
                background: "#0f172a",
                border: "1px solid #1e293b",
                borderRadius: "6px",
                padding: "8px 12px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                flex: 1, 
                minHeight: "48px", 
                boxSizing: "border-box"
              }}
            >
              <div style={{ display: "flex", flexDirection: "column", justifyContent: "center" }}>
                <div style={{ fontSize: "9.5px", color: "#64748b", fontWeight: "700", marginBottom: "2px", textTransform: "uppercase", letterSpacing: "0.3px" }}>
                  {card.title}
                </div>
                <div style={{ display: "flex", alignItems: "baseline", gap: "6px" }}>
                  <span style={{ fontSize: "15px", fontWeight: "800", color: card.color }}>
                    {card.main}
                  </span>
                  <span style={{ fontSize: "11px", color: "#cbd5e1", fontWeight: "600" }}>
                    {card.sub}
                  </span>
                </div>
                {card.note && (
                  <div style={{ fontSize: "9px", color: "#475569", marginTop: "1px", fontWeight: "500" }}>
                    {card.note}
                  </div>
                )}
              </div>
              
              <div style={{ fontSize: "22px", color: card.color, opacity: 0.75, display: "flex", alignItems: "center" }}>
                {card.icon}
              </div>
            </div>
          ))
        )}
      </div>

      {/* ================================================= */}
      {/* FOOTER AREA: Q&A INPUT BOX AI COPILOT */}
      {/* ================================================= */}
      {!isCollapsed && (
        <div 
          style={{ 
            background: "#0f172a", 
            border: "1px solid #1e293b", 
            borderRadius: "6px", 
            padding: "8px", 
            display: "flex", 
            flexDirection: "column", 
            gap: "6px",
            flexShrink: 0
          }}
        >
          <div style={{ display: "flex", alignItems: "center", gap: "5px", fontSize: "10px", fontWeight: "700", color: "#94a3b8" }}>
            <RiRobot2Line style={{ color: "#38bdf8" }} size={12} /> Ask AI Copilot (Exploratory Data)
          </div>
          
          <div 
            style={{ 
              display: "flex", 
              gap: "6px", 
              alignItems: "flex-end", 
              background: "#020617", 
              border: "1px solid #334155", 
              borderRadius: "4px", 
              padding: "4px 6px" 
            }}
          >
            <textarea
              ref={textareaRef}
              rows={1}
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              disabled={isAnalyzing}
              placeholder={isAnalyzing ? "Analyzing fuel database..." : "Tanyakan trend analisis fuel..."}
              style={{
                flex: 1,
                background: "transparent",
                border: "none",
                color: "#ffffff",
                fontSize: "11px",
                outline: "none",
                resize: "none",
                fontFamily: "inherit",
                lineHeight: "1.4",
                minHeight: "24px",
                maxHeight: "60px",
                overflowY: "auto",
                padding: "2px 0"
              }}
              onKeyDown={(e) => {
                if (e.key === "Enter" && !e.shiftKey) {
                  e.preventDefault();
                  handleSendQuery();
                }
              }}
            />
            <button
              onClick={handleSendQuery}
              disabled={isAnalyzing || !query.trim()}
              style={{
                background: "transparent",
                border: "none",
                color: query.trim() && !isAnalyzing ? "#38bdf8" : "#475569",
                cursor: query.trim() && !isAnalyzing ? "pointer" : "default",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                height: "24px",
                padding: "0 4px",
                transition: "color 0.2s ease"
              }}
            >
              <FiSend size={13} />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}