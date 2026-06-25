import React, { useState, useEffect } from 'react';
import { createPortal } from 'react-dom'; 
import { useDragScroll } from '../../hooks/useDragScroll';

export const ImageZoomPreview = ({ zoomImage, closeZoom, sourceRect, zoomPosition }) => {
  const [zoomScale, setZoomScale] = useState(150);
  const {
    dragRef,
    isDragging,
    handleMouseDown,
    handleMouseMove,
    handleMouseUpOrLeave
  } = useDragScroll();

  useEffect(() => {
    if (zoomImage) {
      setZoomScale(150);
    }
  }, [zoomImage]);

  // Handle pencegahan event penutupan liar dari luar saat berinteraksi di dalam bingkai
  useEffect(() => {
    if (!zoomImage || !sourceRect) return;

    // Handler global khusus untuk mode floating agar klik di dalam bingkai tidak dianggap "Click Outside" oleh App.jsx
    const handleOutsidePreventer = (e) => {
      const floatingFrame = document.getElementById('floating-ai-zoom-frame');
      if (floatingFrame && floatingFrame.contains(e.target)) {
        // Jika yang diklik adalah bagian dalam bingkai, amankan event-nya
        e.stopPropagation();
      }
    };

    document.addEventListener('mousedown', handleOutsidePreventer, true);
    return () => {
      document.removeEventListener('mousedown', handleOutsidePreventer, true);
    };
  }, [zoomImage, sourceRect]);

  if (!zoomImage) return null;

  const handleZoomIn = (e) => {
    e.preventDefault();
    e.stopPropagation(); 
    setZoomScale((prev) => Math.min(prev + 25, 400));
  };

  const handleZoomOut = (e) => {
    e.preventDefault();
    e.stopPropagation(); 
    setZoomScale((prev) => Math.max(prev - 25, 75));
  };

  const handleBackdropClick = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (e.target === e.currentTarget) {
      closeZoom();
    }
  };

  // =========================================================================
  // 💡 MODE A: BINGKAI MELAYANG DI SEBELAH KIRI (KHUSUS TABEL AI WORKBENCH)
  // =========================================================================
  if (sourceRect && sourceRect.top !== undefined) {
    const spaceOffset = 20; // Jarak horizontal dari bingkai foto asli ke bingkai preview
    const floatingTop = sourceRect.top + window.scrollY;
    
    // RUMUS PINDAH KIRI: Koordinat kiri foto asli dikurangi lebar bingkai preview dikurangi offset jarak
    const frameWidth = 380; 
    const floatingLeft = sourceRect.left - frameWidth - spaceOffset;

    return (
      <div
        id="floating-ai-zoom-frame"
        className="animated-fade-in"
        onClick={(e) => {
          e.preventDefault();
          e.stopPropagation(); // Kunci klik area bingkai agar tidak tembus ke tabel bawah
        }}
        onMouseUp={(e) => {
          e.preventDefault();
          e.stopPropagation();
        }}
        style={{
          position: 'absolute',
          top: `${floatingTop}px`,
          left: `${floatingLeft > 0 ? floatingLeft : 10}px`, // Penahan fallback jika layar terlalu mepet ke kiri
          backgroundColor: '#0f172a',
          padding: '12px',
          borderRadius: '12px',
          border: '2px solid #c084fc', // Warna ungu penanda AI Workbench
          boxShadow: '0 20px 40px rgba(0, 0, 0, 0.6)',
          display: 'flex',
          flexDirection: 'column',
          gap: '8px',
          width: `${frameWidth}px`, 
          zIndex: 9999,
          cursor: 'default'
        }}
      >
        {/* FLOATING TOOLBAR */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', width: '100%', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '6px' }}>
          <span style={{ color: '#c084fc', fontSize: '11px', fontWeight: '700', letterSpacing: '0.5px' }}>
            AI LIVE WORKBENCH VIEW
          </span>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }} onClick={(e) => e.stopPropagation()}>
            <button 
              onClick={handleZoomOut} 
              onMouseDown={(e) => e.stopPropagation()}
              style={{ padding: '2px 8px', backgroundColor: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', borderRadius: '4px', cursor: 'pointer', fontSize: '11px', fontWeight: 'bold' }}
            >
              -
            </button>
            <span style={{ color: '#22d3ee', fontSize: '11px', fontWeight: '700', minWidth: '32px', textAlign: 'center', fontFamily: 'monospace' }}>{zoomScale}%</span>
            <button 
              onClick={handleZoomIn} 
              onMouseDown={(e) => e.stopPropagation()}
              style={{ padding: '2px 8px', backgroundColor: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', borderRadius: '4px', cursor: 'pointer', fontSize: '11px', fontWeight: 'bold' }}
            >
              +
            </button>
          </div>
          <button 
            onClick={(e) => {
              e.preventDefault();
              e.stopPropagation();
              closeZoom();
            }} 
            style={{ backgroundColor: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '11px', fontWeight: '700' }}
          >
            ✕
          </button>
        </div>

        {/* CONTAINER VIEWPORT IMAGE */}
        <div
          ref={dragRef}
          onMouseDown={(e) => {
            handleMouseDown(e);
            e.stopPropagation(); // Amankan drag event agar tidak bocor mousedown-nya
          }}
          onMouseMove={(e) => {
            handleMouseMove(e);
            e.stopPropagation();
          }}
          onMouseUp={(e) => {
            handleMouseUpOrLeave(e);
            e.stopPropagation();
          }}
          onMouseLeave={handleMouseUpOrLeave}
          style={{
            width: '100%',
            height: '240px', 
            overflow: 'auto',
            backgroundColor: '#020617',
            borderRadius: '6px',
            cursor: isDragging ? 'grabbing' : 'grab',
            position: 'relative'
          }}
          className="custom-scroll"
        >
          <img
            src={zoomImage}
            alt="Zoomed Preview"
            draggable={false}
            style={{
              width: `${zoomScale}%`,
              height: 'auto',
              minHeight: '100%',
              objectFit: 'contain',
              display: 'block',
              margin: 'auto',
              pointerEvents: 'none'
            }}
          />
        </div>
      </div>
    );
  }

  // =========================================================================
  // 💡 MODE B: FALLBACK PORTAL FULLSCREEN (KHUSUS DI DALAM MODAL VALIDATION - SUDAH OK)
  // =========================================================================
  return createPortal(
    <div
      onClick={handleBackdropClick}
      onMouseUp={(e) => { e.preventDefault(); e.stopPropagation(); }}
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: 'rgba(3, 7, 18, 0.9)',
        zIndex: 99999, 
        cursor: 'zoom-out',
        backdropFilter: 'blur(6px)',
        display: 'flex',
        justifyContent: 'center',
        alignItems: 'center'
      }}
    >
      <div
        onClick={(e) => { e.preventDefault(); e.stopPropagation(); }}
        onMouseUp={(e) => { e.preventDefault(); e.stopPropagation(); }}
        style={{
          backgroundColor: '#0f172a',
          padding: '20px',
          borderRadius: '16px',
          border: '1px solid rgba(255, 255, 255, 0.1)',
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.8)',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: '12px',
          width: '640px',
          maxWidth: '95vw',
          cursor: 'default'
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px', width: '100%', justifyContent: 'space-between', borderBottom: '1px solid rgba(255,255,255,0.06)', paddingBottom: '12px' }}>
          <span style={{ color: '#94a3b8', fontSize: '13px', fontWeight: '600', letterSpacing: '0.3px' }}>
            Interactive Image Validation
          </span>
          
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button onClick={handleZoomOut} style={{ padding: '6px 14px', backgroundColor: '#1e293b', border: '1px solid rgba(255,255,255,0.1)', color: '#fff', borderRadius: '6px', cursor: 'pointer', fontWeight: '700' }}>-</button>
            <span style={{ color: '#22d3ee', fontSize: '13px', fontWeight: '700', minWidth: '45px', textAlign: 'center', fontFamily: 'monospace' }}>{zoomScale}%</span>
            <button onClick={handleZoomIn} style={{ padding: '6px 14px', backgroundColor: '#1e293b', border: '1px solid rgba(255, 255, 255, 0.1)', color: '#fff', borderRadius: '6px', cursor: 'pointer', fontWeight: '700' }}>+</button>
          </div>

          <button onClick={closeZoom} style={{ backgroundColor: 'transparent', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '12px', fontWeight: '700' }}>CLOSE</button>
        </div>

        <div
          ref={dragRef}
          onMouseDown={(e) => { handleMouseDown(e); e.stopPropagation(); }}
          onMouseMove={(e) => { handleMouseMove(e); e.stopPropagation(); }}
          onMouseUp={(e) => { handleMouseUpOrLeave(e); e.stopPropagation(); }}
          onMouseLeave={handleMouseUpOrLeave}
          style={{
            width: '100%',
            height: '460px',
            overflow: 'auto',
            backgroundColor: '#020617',
            borderRadius: '8px',
            cursor: isDragging ? 'grabbing' : 'grab',
            position: 'relative',
            border: '1px solid rgba(255, 255, 255, 0.03)'
          }}
          className="custom-scroll"
        >
          <img
            src={zoomImage}
            alt="Zoomed Preview"
            draggable={false}
            style={{
              width: `${zoomScale}%`,
              height: 'auto',
              minHeight: '100%',
              objectFit: 'contain',
              display: 'block',
              margin: 'auto',
              pointerEvents: 'none'
            }}
          />
        </div>
      </div>
    </div>,
    document.body
  );
};

export default ImageZoomPreview;