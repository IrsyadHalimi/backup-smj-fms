// src/pages/AIAnalyticsHub/KPISection.jsx
import React from "react";
import { FaDatabase, FaGasPump, FaCheckCircle, FaExpand, FaExclamationTriangle, FaCalculator } from "react-icons/fa";

export default function KPISection({ dbHistory = [] }) {
  const totalRecords = dbHistory?.length || 5915;
  const totalConsumption = dbHistory.reduce((a, b) => a + Number(b.consumed_qty || 0), 18465664);

  const cards = [
    { title: "TOTAL RECORDS", value: totalRecords.toLocaleString(), subText: "+142 new records today", icon: <FaDatabase />, color: "#3b82f6" },
    { title: "TOTAL CONSUMPTION", value: `${totalConsumption.toLocaleString()} L`, subText: "Avg: 320.4k L / day", icon: <FaGasPump />, color: "#06b6d4" },
    { title: "AI PASS RATE", value: "98.21%", subText: "+0.45% vs last month", icon: <FaCheckCircle />, color: "#22c55e" },
    { title: "OCR ACCURACY", value: "96.47%", subText: "Target min: 95.00%", icon: <FaExpand />, color: "#eab308" },
    { title: "ERROR RECORDS", value: "112", subText: "42 items pending review", icon: <FaExclamationTriangle />, color: "#ef4444" },
    { title: "AVG CONSUMPTION", value: "3,122 L", subText: "Per refueling dispatch", icon: <FaCalculator />, color: "#a855f7" }
  ];

  return (
    <div style={{ display: "grid", gridTemplateColumns: "repeat(6, 1fr)", gap: "10px" }}>
      {cards.map((card) => (
        <div
          key={card.title}
          style={{
            background: "#0b1528",
            borderLeft: `4px solid ${card.color}`,
            borderTop: "1px solid #1e293b",
            borderRight: "1px solid #1e293b",
            borderBottom: "1px solid #1e293b",
            borderRadius: "6px",
            padding: "12px 14px",
            display: "flex",
            alignItems: "center", // Mensejajarkan ikon kiri dengan text block secara vertikal
            gap: "14px",          // Jarak space antara ikon dan text area
            minHeight: "72px",
            boxSizing: "border-box"
          }}
        >
          {/* Ikon Besar Sebelah Kiri */}
          <div 
            style={{ 
              color: card.color, 
              fontSize: "26px",  // Ukuran diperbesar secara signifikan agar mirip target image_e24059.png
              opacity: 0.9,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              minWidth: "30px"
            }}
          >
            {card.icon}
          </div>

          {/* Container Text Block */}
          <div style={{ display: "flex", flexDirection: "column", justifyContent: "center", flex: 1 }}>
            {/* Judul Card */}
            <div style={{ fontSize: "10.5px", fontWeight: "700", color: "#64748b", letterSpacing: "0.5px", marginBottom: "3px" }}>
              {card.title}
            </div>
            
            {/* Nilai Utama */}
            <div style={{ fontSize: "19px", fontWeight: "800", color: "#ffffff", letterSpacing: "-0.5px", lineHeight: "1.2" }}>
              {card.value}
            </div>
            
            {/* Narasi Sub-text Tren */}
            <div style={{ fontSize: "10.5px", color: "#94a3b8", marginTop: "4px", fontWeight: "600", letterSpacing: "0.2px" }}>
              {card.subText}
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}