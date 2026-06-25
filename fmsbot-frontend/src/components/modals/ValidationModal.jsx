import React, { useState } from 'react';
import { ImageZoomPreview } from '../ui/ImageZoomPreview';

export const ValidationModal = ({ 
  isOpen, 
  record, 
  onClose, 
  activeTheme, 
  finalHM, 
  setFinalHM, 
  finalFlow, 
  setFinalFlow, 
  onSubmit,
  getImageUrl,
  setPanImage 
}) => {
  // 🛡️ POSISI HOOKS UTAMA (Wajib diletakkan paling atas sebelum kondisi IF manapun)
  const [modalPanImage, setModalPanImage] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // 🛡️ Proteksi Awal Render (Aman diletakkan setelah pendeklarasian Hook)
  if (!isOpen || !record) return null;

  const isHMMatch = record.screening_status === 'HM Match';
  const isFlowMatch = record.screening_status === 'Flow Match';

  return (
    <>
      <div style={modalOverlayStyle} onClick={onClose}>
        <div 
          className="modal-content custom-scroll" 
          onClick={(e) => e.stopPropagation()} 
          style={{ 
            ...modalContainerStyle, 
            backgroundColor: activeTheme.card, 
            border: `1px solid ${activeTheme.border}`,
            animation: 'modalSlideUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)',
            position: 'relative'
          }}
        >
          
          {/* HEADER SECTION */}
          <div style={{ marginBottom: '24px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h2 style={{ margin: 0, color: activeTheme.tableTitle || activeTheme.accent, fontSize: '22px', fontWeight: '700', letterSpacing: '-0.5px' }}>
              Validation: {record.no_unit_sap || "-"} / {record.tech_id || "-"}
            </h2>
            <div style={{ display: 'flex', gap: '12px', alignItems: 'center' }}>
               <div style={{ 
                 fontSize: '11px', 
                 fontWeight: '700', 
                 color: '#fff', 
                 backgroundColor: record.screening_status?.includes('Match') ? '#10b981' : 'rgba(255,255,255,0.05)', 
                 padding: '6px 16px', 
                 borderRadius: '20px',
                 border: record.screening_status?.includes('Match') ? '1px solid #10b981' : `1px solid ${activeTheme.border}`
               }}>
                 Status: {record.screening_status}
               </div>
               <div style={{ fontSize: '12px', color: activeTheme.subText, fontWeight: '500', fontFamily: 'monospace' }}>ID: #{record.id}</div>
            </div>
          </div>

          {/* SINGLE ELEGANT IMAGE BOX GRID */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '24px' }}>
            
            {/* HM BOX */}
            <PhotoSection 
              title="HM / KM SOURCE IMAGE" 
              isMatch={isHMMatch} 
              imgSrc={record.hm_foto_full} 
              activeTheme={activeTheme}
              getImageUrl={getImageUrl}
              onZoom={setModalPanImage}
            />
            
            {/* FLOW METER BOX */}
            <PhotoSection 
              title="FLOW METER SOURCE IMAGE" 
              isMatch={isFlowMatch} 
              imgSrc={record.flow_meter_foto_full} 
              activeTheme={activeTheme}
              getImageUrl={getImageUrl}
              onZoom={setModalPanImage}
            />
          </div>

          {/* INTERACTIVE INPUT FIELD LAYER */}
          <div style={{ display: 'flex', gap: '20px', marginBottom: '24px' }}>
            <ValidationInput 
              label="FINAL HM VALUE" 
              isMatch={isHMMatch} 
              deviceValue={record.hm_km_unit} 
              aiValue={record.ai_hm_read} 
              value={finalHM} 
              placeholder="Input confirmed HM..."
              onChange={setFinalHM} 
              activeTheme={activeTheme}
              btnDeviceBg="#2563eb"
              btnAiBg="#0891b2"
            />
            <ValidationInput 
              label="FINAL FLOW METER" 
              isMatch={isFlowMatch} 
              deviceValue={record.flow_meter_value} 
              aiValue={record.ai_flow_read} 
              value={finalFlow} 
              placeholder="Input confirmed Flow..."
              onChange={setFinalFlow} 
              activeTheme={activeTheme}
              btnDeviceBg="#2563eb"
              btnAiBg="#0891b2"
            />
          </div>

          {/* FOOTER ACTION CONTROLS */}
          <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '14px', borderTop: `1px solid ${activeTheme.border}`, paddingTop: '20px' }}>
            <button 
              disabled={isSubmitting} 
              onClick={onClose} 
              className="btn-h" 
              style={cancelBtnStyle(activeTheme)}
            >
              CANCEL
            </button>
    
            <button 
              onClick={async () => {
                if (isSubmitting) return; // Kunci tombol ganda
                setIsSubmitting(true);
                try {
                  // Menunggu eksekusi fungsi submit dari parent selesai
                  await onSubmit(); 
                } catch (error) {
                  console.error("Submit validation error:", error);
                } finally {
                  setIsSubmitting(false); // Buka kembali kunci
                }
              }} 
              className="btn-h" 
              disabled={isSubmitting} // Efek visual & proteksi interaksi saat loading
              style={{ 
                ...submitBtnStyle, 
                backgroundColor: isSubmitting ? activeTheme.border : activeTheme.accent,
                boxShadow: isSubmitting ? 'none' : `0 4px 14px ${activeTheme.accent}44`,
                cursor: isSubmitting ? 'not-allowed' : 'pointer',
                opacity: isSubmitting ? 0.7 : 1
              }}
            >
              {isSubmitting ? (
                <span style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <i className="fas fa-spinner fa-spin"></i> SAVING TO DATABASE...
                </span>
              ) : (
                "SUBMIT VALIDATION"
              )}
            </button>
          </div>

        </div>
      </div>

      <ImageZoomPreview 
        zoomImage={modalPanImage} 
        closeZoom={() => setModalPanImage(null)} 
      />
    </>
  );
};

// =========================================================================
// REDESIGNED SUB-COMPONENTS (OPTIMIZED & CLEAN)
// =========================================================================

const PhotoSection = ({ title, isMatch, imgSrc, activeTheme, getImageUrl, onZoom }) => {
  const handlePhotoClick = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (onZoom && imgSrc) {
      onZoom(getImageUrl(imgSrc));
    }
  };

  return (
    <div 
      className="photo-card-scifi" 
      style={{ 
        backgroundColor: 'rgba(15, 23, 42, 0.6)', 
        padding: '16px', 
        borderRadius: '12px', 
        border: `1px solid ${activeTheme.border}`, 
        position: 'relative',
        display: 'flex',
        flexDirection: 'column',
        gap: '10px'
      }}
    >
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h4 style={{ margin: 0, color: activeTheme.accent, fontSize: '12px', fontWeight: '700', letterSpacing: '0.5px', fontFamily: 'monospace' }}>
          {title} {isMatch && <span style={{ color: '#10b981', marginLeft: '4px' }}>✓</span>}
        </h4>
        <span style={{ fontSize: '9px', color: 'rgba(255,255,255,0.3)', fontFamily: 'monospace' }}>PREVIEW ENGINE</span>
      </div>

      {/* Single Ultra-Wide Image Container */}
      <div 
        onClick={handlePhotoClick} 
        style={{
          position: 'relative',
          cursor: 'zoom-in',
          borderRadius: '8px',
          overflow: 'hidden',
          border: '1px solid rgba(255,255,255,0.05)',
          backgroundColor: '#020617',
          transition: 'all 0.25s ease-in-out',
        }}
        className="img-box-container"
      >
        <img 
          src={getImageUrl(imgSrc)} 
          style={{ 
            width: '100%', 
            height: '240px', 
            objectFit: 'contain',
            display: 'block'
          }} 
          alt={title} 
        />
        
        {/* Futuristic Overlay HUD Banner */}
        <div style={{
          position: 'absolute',
          bottom: 0, left: 0, right: 0,
          background: 'linear-gradient(transparent, rgba(2, 6, 23, 0.95))',
          padding: '12px',
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center'
        }}>
          <span style={{ fontSize: '10px', color: '#38bdf8', fontWeight: '600', letterSpacing: '1px', fontFamily: 'monospace', display: 'flex', alignItems: 'center', gap: '6px' }}>
            <i className="fas fa-search-plus"></i> CLICK TO INTERACTIVE ZOOM
          </span>
        </div>
      </div>

      {/* Global CSS Injector Tanpa :has() */}
      <style>{`
        .img-box-container {
          box-shadow: inset 0 0 20px rgba(0,0,0,0.6);
        }
        .img-box-container:hover {
          border-color: ${activeTheme.accent}aa !important;
          box-shadow: 0 0 15px ${activeTheme.accent}33;
        }
      `}</style>
    </div>
  );
};

const ValidationInput = ({ label, isMatch, deviceValue, aiValue, value, placeholder, onChange, activeTheme, btnDeviceBg, btnAiBg }) => (
  <div style={{ flex: 1, opacity: isMatch ? 0.6 : 1, transition: 'all 0.25s ease' }}>
    <label style={{ fontSize: '11px', fontWeight: '700', color: activeTheme.subText, display: 'block', marginBottom: '8px', letterSpacing: '0.5px', fontFamily: 'monospace' }}>
      {label} {isMatch && '(AUTO-VERIFIED)'}
    </label>
    
    <div style={{ display: 'flex', gap: '8px', marginBottom: '10px' }}>
      <button 
        type="button"
        className="btn-h" 
        disabled={isMatch} 
        onClick={() => onChange(deviceValue)}
        style={{ 
          flex: 1, padding: '10px', backgroundColor: btnDeviceBg, border: 'none', borderRadius: '6px',
          color: 'white', fontWeight: '600', fontSize: '12px', cursor: isMatch ? 'not-allowed' : 'pointer', opacity: isMatch ? 0.5 : 1,
          transition: 'transform 0.1s'
        }}
      >
        📱 DEV: {deviceValue || 0}
      </button>
      <button 
        type="button"
        className="btn-h" 
        disabled={isMatch} 
        onClick={() => onChange(aiValue)}
        style={{ 
          flex: 1, padding: '10px', backgroundColor: btnAiBg, border: 'none', borderRadius: '6px',
          color: 'white', fontWeight: '600', fontSize: '12px', cursor: isMatch ? 'not-allowed' : 'pointer', opacity: isMatch ? 0.5 : 1,
          transition: 'transform 0.1s'
        }}
      >
        🤖 AI: {aiValue || 0}
      </button>
    </div>
    
    <input 
      type="number" 
      value={value} 
      placeholder={isMatch ? "" : placeholder} 
      disabled={isMatch} 
      onChange={(e) => onChange(e.target.value)} 
      style={{ 
        width: '100%', boxSizing: 'border-box', padding: '12px 14px', borderRadius: '8px', fontSize: '14px', outline: 'none', transition: 'all 0.2s',
        border: value ? `1px solid ${activeTheme.accent}` : `1px solid ${activeTheme.border}`,
        backgroundColor: isMatch ? 'rgba(30, 41, 59, 0.3)' : 'rgba(15, 23, 42, 0.7)',
        cursor: isMatch ? 'not-allowed' : 'text', color: isMatch ? activeTheme.subText : '#fff'
      }}
    />
  </div>
);

// BASE STYLES RE-OPTIMIZED
const modalOverlayStyle = { 
  position: 'fixed', inset: 0, backgroundColor: 'rgba(8, 13, 28, 0.88)', zIndex: 10000, 
  display: 'flex', justifyContent: 'center', alignItems: 'center', backdropFilter: 'blur(8px)' 
};

const modalContainerStyle = { 
  width: '1000px', maxHeight: '90vh', overflowY: 'auto', borderRadius: '16px', padding: '24px', boxShadow: '0 30px 80px rgba(0, 0, 0, 0.8)' 
};

const submitBtnStyle = { 
  padding: '12px 36px', minWidth: '180px', color: '#fff', border: 'none', borderRadius: '8px', fontWeight: '700', cursor: 'pointer', fontSize: '13px', letterSpacing: '0.5px', transition: 'all 0.2s ease'
};

const cancelBtnStyle = (theme) => ({ 
  padding: '12px 24px', borderRadius: '8px', border: `1px solid ${theme.border}`, backgroundColor: 'transparent', color: theme.text, cursor: 'pointer', fontWeight: '600', fontSize: '13px', transition: 'all 0.2s ease'
});

export default ValidationModal;