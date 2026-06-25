import React, { useEffect, useState } from 'react';

const ZoomConnector = ({ zoomImage, sourceRect, activeTheme }) => {
  const [coords, setCoords] = useState(null);

  useEffect(() => {
    if (!zoomImage || !sourceRect || sourceRect.top === undefined) {
      setCoords(null);
      return;
    }

    // 💡 PENYESUAIAN KOORDINAT UTK BINGKAI DI SEBELAH KIRI
    const frameWidth = 380;   // Harus sama dengan width di ImageZoomPreview
    const spaceOffset = 20;   // Harus sama dengan spaceOffset di ImageZoomPreview

    // 1. Titik Asal (Sisi Kiri dari Foto Asli di Tabel)
    const startX = sourceRect.left + window.scrollX;
    const startY = sourceRect.top + window.scrollY + (sourceRect.height / 2);

    // 2. Titik Target (Sisi Kanan dari Bingkai Preview Melayang)
    // Karena bingkai di kiri, maka sisi kanannya adalah (sourceRect.left - spaceOffset)
    const endX = sourceRect.left - spaceOffset;
    const endY = sourceRect.top + window.scrollY + 120; // 120px adalah titik tengah tinggi viewport preview (240px / 2)

    setCoords({ startX, startY, endX, endY });
  }, [zoomImage, sourceRect]);

  if (!coords) return null;

  const { startX, startY, endX, endY } = coords;

  // Membuat jalur kurva bezier halus (S-Curve) dari tabel ke bingkai kiri
  // Control point ditarik ke kiri dan ke kanan secara proporsional agar lengkungan luwes
  const controlOffset = Math.abs(startX - endX) * 0.5;
  const pathDefinition = `
    M ${startX} ${startY}
    C ${startX - controlOffset} ${startY}, 
      ${endX + controlOffset} ${endY}, 
      ${endX} ${endY}
  `;

  const accentColor = activeTheme?.accent || '#c084fc'; // Default ke warna ungu AI

  return (
    <svg
      style={{
        position: 'absolute',
        top: 0,
        left: 0,
        width: '100vw',
        height: '100vh',
        pointerEvents: 'none', // Supaya tidak menghalangi klik mouse pada elemen di bawahnya
        zIndex: 9998, // Tepat satu tingkat di bawah bingkai preview (9999)
      }}
    >
      <defs>
        {/* Efek Glow Neon pada Garis Konektor */}
        <filter id="glow-connector" x="-20%" y="-20%" width="140%" height="140%">
          <feGaussianBlur stdDeviation="3" result="blur" />
          <feComposite in="SourceGraphic" in2="blur" operator="over" />
        </filter>

        {/* Marker Titik Penanda di Ujung Garis Kanan (Sisi Bingkai) */}
        <marker
          id="marker-dot-end"
          markerWidth="6"
          markerHeight="6"
          refX="3"
          refY="3"
          getMarkerUnits="strokeWidth"
        >
          <circle cx="3" cy="3" r="3" fill={accentColor} />
        </marker>

        {/* Marker Titik Penanda di Ujung Garis Kiri (Sisi Tabel) */}
        <marker
          id="marker-dot-start"
          markerWidth="6"
          markerHeight="6"
          refX="3"
          refY="3"
          getMarkerUnits="strokeWidth"
        >
          <circle cx="3" cy="3" r="3" fill="#22d3ee" /> {/* Cyan penanda jangkar tabel */}
        </marker>
      </defs>

      {/* 🌟 Garis Vektor Utama (S-Curve) */}
      <path
        d={pathDefinition}
        fill="none"
        stroke={accentColor}
        strokeWidth="2"
        strokeDasharray="4 3" // Model garis putus-putus estetik khas teknologi AI screening
        filter="url(#glow-connector)"
        markerStart="url(#marker-dot-start)"
        markerEnd="url(#marker-dot-end)"
        style={{
          opacity: 0.85,
          transition: 'd 0.1s ease-out' // Animasi pergeseran luwes jika user melakukan scroll tabel
        }}
      />
    </svg>
  );
};

export default ZoomConnector;