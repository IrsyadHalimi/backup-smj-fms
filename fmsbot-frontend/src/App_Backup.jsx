import React, { useState, useEffect, useMemo, useRef } from 'react';
import useWebSocket from 'react-use-websocket';
import * as XLSX from 'xlsx'
import { Rnd } from 'react-rnd';

// Helper Format Tanggal
const formatIndoDate = (dateString) => {
  if (!dateString || dateString === "-") return "-";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return dateString;
    return new Intl.DateTimeFormat('id-ID', { day: 'numeric', month: 'long', year: 'numeric' }).format(date);
  } catch (e) { return dateString; }
};

// Sub-Komponen Card Statis
const StatCard = ({ title, value, color, activeTheme, subValue }) => (
  <div style={{
    backgroundColor: activeTheme.card,
    padding: '12px 16px',
    borderRadius: '10px',
    // Perbaikan: Jangan campur 'border' dengan 'borderLeft'
    borderStyle: 'solid',
    borderWidth: '1px 1px 1px 4px', // Atas, Kanan, Bawah, Kiri (Kiri lebih tebal)
    borderColor: `${activeTheme.border} ${activeTheme.border} ${activeTheme.border} ${color}`,
    flex: 1,
    display: 'flex',
    flexDirection: 'column',
    justifyContent: 'center'
  }}>
    <div style={{ fontSize: '12px', color: activeTheme.subText, fontWeight: 'bold', textTransform: 'uppercase', marginBottom: '4px' }}>{title}</div>
    <div style={{ fontSize: '22px', fontWeight: '800', color: activeTheme.text, lineHeight: '1' }}>{value}</div>
    {subValue && <div style={{ fontSize: '10px', color: color, marginTop: '4px', fontWeight: 'bold' }}>{subValue}</div>}
  </div>
);

const OCRStatusLabel = ({ status }) => {
  const styles = { 
    fontSize: '10px', 
    fontWeight: '700', 
    padding: '4px 8px', 
    borderRadius: '4px', 
    display: 'inline-block',
    textAlign: 'center',
    minWidth: '60px'
  };

  // 1. Menangani status 'Processing' / 'Queue' dari Celery (Animasi)
  if (status === 'Processing' || status === 'Queue') {
    return (
      <div style={{ width: '100%' }}>
        <div style={{ ...styles, color: '#06b6d4' }}>⚡ Screening</div>
        <div style={{ 
          width: '100%', 
          height: '4px', 
          backgroundColor: '#334155', 
          borderRadius: '2px', 
          marginTop: '2px', 
          overflow: 'hidden' 
        }}>
          <div style={{ 
            width: '60%', 
            height: '100%', 
            backgroundColor: '#06b6d4', 
            animation: 'blink 1s infinite' 
          }} />
        </div>
      </div>
    );
  }

  // 2. Status Sukses (Matched)
  if (status === 'Verified' || status === 'Matched') {
    return (
      <span style={{ 
        ...styles, 
        backgroundColor: 'rgba(16, 185, 129, 0.15)', 
        color: '#10b981',
        border: '1px solid rgba(16, 185, 129, 0.3)'
      }}>
        Matched
      </span>
    );
  }

  // 3. Status Error / Failed (Kini Merah Tegas)
  // Kita cek apakah status mengandung kata 'Error', 'Failed', atau 'Mismatch'
  if (
    status === 'Mismatch' || 
    status === 'Mismatched' || 
    status === 'Failed' || 
    status?.includes('Error')
  ) {
    // Tentukan label teks: Jika Failed/Error munculkan "Error AI", jika Mismatch biarkan "Mismatch"
    const isAiError = status === 'Failed' || status?.includes('Error');
    const labelText = isAiError ? 'Error AI' : 'Mismatch';

    return (
      <span style={{ 
        ...styles, 
        backgroundColor: 'rgba(239, 68, 68, 0.15)', 
        color: '#ef4444',
        border: '1px solid rgba(239, 68, 68, 0.3)'
      }}>
        {labelText}
      </span>
    );
  }

  // 4. Default / Blank (untuk Initial Flow atau data kosong)
  return <span style={{ color: '#94a3b8', fontSize: '10px' }}>-</span>;
};

const menuButtonStyle = {
  background: 'none',
  border: 'none',
  color: 'white',
  display: 'flex',
  alignItems: 'center',
  gap: '8px',
  fontSize: '12px',
  cursor: 'pointer',
  fontWeight: '600',
  padding: '4px 8px'
};

//Style Helper (Konstanta Style)
const activeModeStyle = {
  backgroundColor: '#22d3ee',
  color: '#0f172a',
  border: 'none',
  padding: '6px 15px',
  borderRadius: '4px',
  fontSize: '10px',
  fontWeight: 'bold',
  cursor: 'pointer'
};

const inactiveModeStyle = {
  backgroundColor: 'rgba(255,255,255,0.05)',
  color: '#64748b',
  border: 'none',
  padding: '6px 15px',
  borderRadius: '4px',
  fontSize: '10px',
  fontWeight: 'bold',
  cursor: 'pointer'
};

// FUNGSI EXPORT & LAYOUT PELAPORAN
// 1. Definisikan Key untuk LocalStorage
const EXPORT_LAYOUT_KEY = 'fms_export_layout_pref';
// 2. Daftar Field Lengkap dari models.py
const AVAILABLE_FIELDS = [
  'id', 'no_unit_sap', 'tech_id', 'entry_type', 'hm_km_unit', 
  'ai_hm_read', 'flow_meter_value', 'ai_flow_read', 
  'screening_status', 'date_screening', 'created_at'
];

// --- BACKEND CONNECTION CONFIGURATION ---
const isSecure = window.location.protocol === 'https:';
const hostName = window.location.hostname;
const isLocal = hostName === 'localhost' || hostName === '127.0.0.1';
const backendHost = isLocal ? `${hostName}:8000` : hostName;
const BASE_URL = `${window.location.protocol}//${backendHost}`;
const API_URL = `${BASE_URL}/api/fuel-transactions/`;
const WS_URL = `${isSecure ? 'wss' : 'ws'}://${backendHost}/ws/fuel-sync/`;

// Debug untuk memastikan URL benar di Console
console.log("Koneksi API ke:", API_URL);
console.log("Koneksi WS ke:", WS_URL);

 const getImageUrl = (path) => {
  if (!path) return null;
  if (String(path).startsWith('http')) return path;
  const cleanPath = String(path).startsWith('/') ? path : `/${path}`;
  return `${BASE_URL}${cleanPath}`;
};

// --- HELPER COMPONENTS & STYLES ---
const SortIcon = ({ config, col }) => {
  if (config.key !== col) return <i className="fas fa-sort" style={{ marginLeft: '5px', opacity: 0.2 }}></i>;
  return <i className={`fas fa-sort-amount-${config.direction === 'asc' ? 'up' : 'down'}`} style={{ marginLeft: '5px', color: '#ef4444' }}></i>;
};

const thStyle = (theme) => ({ padding: '10px 5px', border: `1px solid ${theme.border}44`, cursor: 'pointer', whiteSpace: 'nowrap' });
const tdStyle = (theme) => ({ padding: '8px', borderRight: `1px solid ${theme.border}22` });
const statusBadge = (s) => ({
  fontSize: '9px', padding: '2px 6px', borderRadius: '4px', fontWeight: 'bold',
  backgroundColor: s === 'Verified' ? 'rgba(16,185,129,0.1)' : 'rgba(245,158,11,0.1)',
  color: s === 'Verified' ? '#10b981' : '#f59e0b',
  border: `1px solid ${s === 'Verified' ? '#10b98144' : '#f59e0b44'}`
});

// FuelTable component moved outside to preserve internal state and prevent scroll resets
const FuelTable = React.memo(({ 
  data, title, tableType, isExpanded, onToggle, 
  activeTheme, isDarkMode, getImageUrl, handleImageZoom, setZoomImage,
  handleOpenValidation, handleReScreening, formatIndoDate,
  activeMenuId, setActiveMenuId, menuButtonStyle, OCRStatusLabel
}) => {
  // 1. STATE UNTUK SORTING & GLOBAL SEARCH
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [globalSearch, setGlobalSearch] = useState("");

  // --- PENYEMPURNAAN STATE VIEW MODE (3-LEVEL) ---
  const [viewState, setViewState] = useState('half'); // 'min', 'half', 'full'

  const isAI = tableType === 'AI_WORKBENCH';

  const getTableMeta = (type) => {
    switch(type) {
      case 'VERIFIED': 
        return { 
          label: 'DISPATCHED LOGS', 
          icon: 'fa-file-export', 
          color: '#10b981' 
        };
      case 'AI_WORKBENCH': 
        return { 
          label: 'AI ANALYTICS HUB', 
          icon: 'fa-microchip', 
          color: '#c084fc' 
        };
      case 'QUEUE': 
        return { 
          label: 'PENDING VALIDATION', 
          icon: 'fa-clock-rotate-left', 
          color: '#f59e0b' 
        };
      default: 
        return { label: title, icon: 'fa-bolt', color: activeTheme.accent };
    }
  };

  const meta = getTableMeta(tableType);

  // LOGIKA TINGGI DINAMIS BERDASARKAN SIKLUS KLIK
  const dynamicHeight = useMemo(() => {
    if (!isExpanded) return '52px'; // Mode Minimize (Collapsed)
    // Jika isExpanded true, kita cek internal state untuk half/full
    return viewState === 'full' ? '100%' : '380px'; 
  }, [isExpanded, viewState]);

  const handleHeaderClick = () => {
    if (!isExpanded) {
        onToggle(); // Buka ke posisi half
        setViewState('half');
    } else if (viewState === 'half') {
        setViewState('full'); // Dari half ke full
    } else {
        onToggle(); // Dari full ke minimize (tutup)
        setViewState('half'); // reset internal ke half untuk pembukaan berikutnya
    }
  };

  const enhancedImgStyle = (isZoomed) => ({
  width: '100%',
  height: '45px',
  objectFit: 'cover',
  borderRadius: '4px',
  cursor: 'zoom-in',
  display: 'block',
  transition: 'all 0.2s ease',
  border: isZoomed ? `2px solid ${activeTheme.accent}` : `1px solid ${activeTheme.border}44`,
  boxShadow: isZoomed ? '0 0 10px rgba(0,0,0,0.5)' : 'none'
});

  // 2. LOGIKA SORTING
  const handleSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  const [activeZoomId, setActiveZoomId] = useState(null);

  useEffect(() => {
  const handleClickOutside = (event) => {
    if (!event.target.classList.contains('zoomable-image')) {
      setZoomImage(null);
      setActiveZoomId(null);
    }
  };

  document.addEventListener('mousedown', handleClickOutside);
  return () => {
    document.removeEventListener('mousedown', handleClickOutside);
  };
}, [setZoomImage]);

  const tableRef = useRef(null);  

const processedData = useMemo(() => {
  let filtered = data.filter(r => {
    const status = r.screening_status;
    if (tableType === 'QUEUE') {
      return !status || status === '' || status === 'Pending';
    }
    if (tableType === 'AI_WORKBENCH') {
      const aiStatuses = [
        'Mismatch', 'Flow Match', 'HM Match', 
        'Failed', 'Error AI', 'Processing', 'Screening'
      ];
      return aiStatuses.includes(status);
    }
    if (tableType === 'VERIFIED') {
      return status === 'Verified';
    }
    return true;
  });

  if (globalSearch) {
    const query = globalSearch.toLowerCase();
    filtered = filtered.filter(item => 
      String(item.pit_location_id || '').toLowerCase().includes(query)
    );
  }

  if (sortConfig.key) {
    filtered.sort((a, b) => {
      const valA = a[sortConfig.key] || '';
      const valB = b[sortConfig.key] || '';
      if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
      if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
      return 0;
    });
  }

  return tableType === 'QUEUE' ? filtered.slice(0, 10) : filtered;

}, [data, tableType, globalSearch, sortConfig]); 

  const totalRecords = processedData.length;
  const totalVolume = useMemo(() => {
    return processedData.reduce((acc, curr) => {
      const val = parseFloat(curr.consumed_qty);
      return acc + (isNaN(val) ? 0 : Math.abs(val));
    }, 0);
  }, [processedData]);

  const triggerZoom = (e, imgUrl, rowId) => {
    e.stopPropagation();
    const row = e.currentTarget.closest('tr');
    const container = tableRef.current;
    if (row && container) {
      const rowBottom = row.offsetTop + row.offsetHeight;
      const containerViewBottom = container.scrollTop + container.offsetHeight;
      
      if (rowBottom > containerViewBottom - 50) {
        container.scrollTo({
          top: row.offsetTop - 50,
          behavior: 'smooth'
        });
      }
    }
    handleImageZoom(e, imgUrl);
  };

  return (
    <div style={{ 
      display: 'flex', flexDirection: 'column', flex: 1, 
      height: dynamicHeight, 
      minHeight: 0, overflow: 'hidden', 
      backgroundColor: activeTheme.tableHead, 
      borderRadius: '8px', border: `1px solid ${activeTheme.border}`,
      transition: 'height 0.4s cubic-bezier(0.4, 0, 0.2, 1)'
    }}>
      
      {/* HEADER DENGAN INFO STATS */}
      <div style={{ 
        padding: '10px 14px', cursor: 'pointer', display: 'flex', 
        justifyContent: 'space-between', alignItems: 'center', minHeight: '50px',
        borderBottom: isExpanded ? `1px solid ${activeTheme.border}44` : 'none'
      }} onClick={handleHeaderClick}>
        
        {/* Sisi Kiri: Judul Profesional & Stats */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <i className={`fas ${meta.icon}`} style={{ color: meta.color, fontSize: '13px' }}></i>
            <span style={{ fontSize: '12px', fontWeight: '800', letterSpacing: '0.5px', color: activeTheme.tableTitle }}>
              {meta.label}
            </span>
          </div>
          
          {isExpanded && (
            <div style={{ display: 'flex', gap: '12px', marginLeft: '23px', opacity: 0.8 }}>
               <span style={{ fontSize: '9px', color: activeTheme.subText, display: 'flex', alignItems: 'center', gap: '4px' }}>
                 <i className="fas fa-database" style={{ fontSize: '8px' }}></i>
                 {totalRecords} Records
               </span>
               <span style={{ fontSize: '9px', color: activeTheme.subText, display: 'flex', alignItems: 'center', gap: '4px' }}>
                 <i className="fas fa-gas-pump" style={{ fontSize: '8px' }}></i>
                 {totalVolume.toLocaleString('id-ID')} Litres
               </span>
            </div>
          )}
        </div>

        {/* Sisi Kanan: Area Filter & Siklus Icon */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
          {!isExpanded && (
            <div style={{ 
              display: 'flex', gap: '8px', padding: '4px 10px', 
              backgroundColor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)',
              borderRadius: '12px', marginRight: '4px'
            }}>
              <span style={{ fontSize: '10px', fontWeight: 'bold', color: meta.color }}>
                {totalRecords} <small style={{ fontWeight: 'normal', opacity: 0.7 }}>Recs</small>
              </span>
              <div style={{ width: '1px', backgroundColor: activeTheme.border, height: '12px', alignSelf: 'center' }} />
              <span style={{ fontSize: '10px', fontWeight: 'bold', color: activeTheme.text }}>
                {totalVolume.toLocaleString('id-ID')} <small style={{ fontWeight: 'normal', opacity: 0.7 }}>L</small>
              </span>
            </div>
          )}

          {isExpanded && (  
            <div style={{ position: 'relative' }} onClick={(e) => e.stopPropagation()}>
              <i className="fas fa-filter" style={{ position: 'absolute', left: '8px', top: '50%', transform: 'translateY(-50%)', fontSize: '10px', color: activeTheme.accent }}></i>
              <input 
                type="text" placeholder="Filter Storage..." value={globalSearch}
                onChange={(e) => setGlobalSearch(e.target.value)}
                style={{
                  padding: '4px 8px 4px 24px', fontSize: '10px', borderRadius: '15px',
                  border: `1px solid ${activeTheme.border}`, background: isDarkMode ? 'rgba(0,0,0,0.2)' : '#fff',
                  color: activeTheme.text, width: '120px', outline: 'none'
                }}
              />
            </div>
          )}
          
          {/* ICON BERUBAH BERDASARKAN SIKLUS */}
          <i className={`fas ${
                !isExpanded ? 'fa-caret-down' : 
                viewState === 'half' ? 'fa-expand-arrows-alt' : 'fa-compress-alt'
            }`} 
            style={{ color: '#ef4444', fontSize: '14px', transition: 'transform 0.3s' }}>
          </i>
        </div>
      </div>

      {/* BODY TABEL */}
      <div 
        ref={tableRef} 
        className="custom-scroll" 
        style={{ 
          overflow: 'auto', flex: 1, 
          display: isExpanded ? 'block' : 'none',
          backgroundColor: isDarkMode ? 'rgba(0,0,0,0.2)' : '#ffffff' 
        }}
      >
        <table style={{ width: 'max-content', minWidth: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ position: 'sticky', top: 0, zIndex: 10, backgroundColor: activeTheme.tableHead }}>
            {isAI ? (
              <>
                <tr style={{ fontSize: '10px', color: activeTheme.subText }}>
                  <th style={thStyle(activeTheme)} rowSpan={2} onClick={() => handleSort('no_unit_sap')}>SAP ID <SortIcon config={sortConfig} col="no_unit_sap"/></th>
                  <th style={thStyle(activeTheme)} rowSpan={2} onClick={() => handleSort('tech_id')}>Tech. ID <SortIcon config={sortConfig} col="tech_id"/></th>
                  <th style={thStyle(activeTheme)} rowSpan={2} onClick={() => handleSort('pit_location_id')}>Storage <SortIcon config={sortConfig} col="pit_location_id"/></th>
                  <th style={thStyle(activeTheme)} rowSpan={2}>Time</th>
                  <th colSpan={5} style={thStyle(activeTheme)}>HM/KM Unit</th>
                  <th colSpan={5} style={thStyle(activeTheme)}>Flow Meter</th>
                  <th style={thStyle(activeTheme)} rowSpan={2}>Consumed Qty</th>
                  <th rowSpan={2} style={thStyle(activeTheme)}>Action</th>
                </tr>
                <tr style={{ fontSize: '10px', color: activeTheme.subText }}>
                  {['Foto', 'Dev', 'AI', 'FV', 'OCR'].map((h, i) => <th key={i} style={thStyle(activeTheme)}>{h}</th>)}
                  {['Foto', 'Dev', 'AI', 'FV', 'OCR'].map((h, i) => <th key={i+5} style={thStyle(activeTheme)}>{h}</th>)}
                </tr>
              </>
            ) : (
              <tr style={{ fontSize: '11px', color: activeTheme.subText }}>
                <th style={thStyle(activeTheme)} onClick={() => handleSort('no_unit_sap')}>SAP ID <SortIcon config={sortConfig} col="no_unit_sap"/></th>
                <th style={thStyle(activeTheme)} onClick={() => handleSort('tech_id')}>Tech ID <SortIcon config={sortConfig} col="tech_id"/></th>
                <th style={thStyle(activeTheme)} onClick={() => handleSort('pit_location_id')}>Storage <SortIcon config={sortConfig} col="pit_location_id"/></th>
                <th style={thStyle(activeTheme)}>Date/Time</th>
                <th style={thStyle(activeTheme)}>HM/KM</th>
                <th style={thStyle(activeTheme)}>Flowmeter</th>
                <th style={thStyle(activeTheme)}>Consumed Qty</th>
                <th style={thStyle(activeTheme)}>Status</th>
              </tr>
            )}
          </thead>

          <tbody style={{ fontSize: '11px', textAlign: 'center' }}>
            {processedData.map((r, i) => (
                <tr key={i} style={{ borderBottom: `1px solid ${activeTheme.border}22` }}>
                    <td style={tdStyle(activeTheme)}>{r.no_unit_sap}</td>
                    <td style={tdStyle(activeTheme)}>{r.tech_id}</td>
                    <td style={{ ...tdStyle(activeTheme), color: '#3b82f6', fontWeight: 'bold' }}>{r.pit_location_id || '-'}</td>

                    {isAI ? (
                        <>
                            <td style={tdStyle(activeTheme)}>{r.date}<br />{r.jam_isi}</td>
                            <td style={{ ...tdStyle(activeTheme), width: '80px', padding: '4px' }}>
                                <img
                                    src={getImageUrl(r.hm_foto_full)}
                                    className="zoomable-image"
                                    style={enhancedImgStyle(activeZoomId === `hm-${r.id}`)}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        triggerZoom(e, r.hm_foto_full, r.id);
                                        setActiveZoomId(`hm-${r.id}`);
                                    }}
                                />
                            </td>
                            <td style={tdStyle(activeTheme)}>{r.hm_km_unit}</td>
                            <td style={{ ...tdStyle(activeTheme), color: activeTheme.accent }}>{r.ai_hm_read || '-'}</td>
                            <td style={tdStyle(activeTheme)}>{r.final_hm_value || '-'}</td>
                            <td style={tdStyle(activeTheme)}><OCRStatusLabel status={r.screening_status} /></td>
                            <td style={{ ...tdStyle(activeTheme), width: '80px', padding: '4px' }}>
                                <img
                                    src={getImageUrl(r.flow_meter_foto_full)}
                                    className="zoomable-image"
                                    style={enhancedImgStyle(activeZoomId === `flow-${r.id}`)}
                                    onClick={(e) => {
                                        e.stopPropagation();
                                        triggerZoom(e, r.flow_meter_foto_full, r.id);
                                        setActiveZoomId(`flow-${r.id}`);
                                    }}
                                />
                            </td>
                            <td style={tdStyle(activeTheme)}>{r.flow_meter_value}</td>
                            <td style={{ ...tdStyle(activeTheme), color: activeTheme.accent }}>{r.ai_flow_read || '-'}</td>
                            <td style={tdStyle(activeTheme)}>{r.final_flow_value || '-'}</td>
                            <td style={tdStyle(activeTheme)}><OCRStatusLabel status={r.screening_status} /></td>
                            <td style={{ ...tdStyle(activeTheme), color: '#48e618', fontWeight: 'bold' }}>
                                {r.consumed_qty ? Math.abs(r.consumed_qty) : '-'}
                            </td>
                            <td style={{
                                padding: '10px',
                                position: 'relative',
                                overflow: 'visible'
                            }} className="action-menu-container">
                                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                                    <button
                                        onClick={(e) => {
                                            e.stopPropagation();
                                            setActiveMenuId(activeMenuId === r.id ? null : r.id);
                                        }}
                                        style={{
                                            background: 'none', border: 'none', color: activeTheme.text,
                                            cursor: 'pointer', padding: '8px', zIndex: 101, transition: 'all 0.3s ease'
                                        }}
                                    >
                                        <i className={`fas ${activeMenuId === r.id ? 'fa-times' : 'fa-ellipsis-v'}`}
                                            style={{ fontSize: '16px', opacity: 0.7 }}></i>
                                    </button>

                                    <div style={{
                                        position: 'absolute',
                                        right: activeMenuId === r.id ? '45px' : '-150px',
                                        top: '50%',
                                        transform: 'translateY(-50%)',
                                        opacity: activeMenuId === r.id ? 1 : 0,
                                        display: 'flex',
                                        gap: '4px',
                                        zIndex: 100,
                                        pointerEvents: activeMenuId === r.id ? 'auto' : 'none',
                                        backgroundColor: isDarkMode ? 'rgba(30, 41, 59, 0.9)' : 'rgba(255, 255, 255, 0.9)',
                                        backdropFilter: 'blur(10px)',
                                        padding: '4px 12px',
                                        borderRadius: '30px',
                                        border: `1px solid ${activeTheme.border}`,
                                        transition: 'all 0.5s cubic-bezier(0.175, 0.885, 0.32, 1.275)',
                                        boxShadow: '0 4px 12px rgba(0,0,0,0.2)',
                                        whiteSpace: 'nowrap'
                                    }}>
                                        <button
                                            className="btn-h"
                                            onClick={() => handleOpenValidation(r)}
                                            style={{
                                                background: 'none', border: 'none', padding: '6px 10px',
                                                color: '#10b981', cursor: 'pointer', fontSize: '11px',
                                                fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '6px'
                                            }}
                                        >
                                            <i className="fas fa-check-double"></i> Verify
                                        </button>
                                        <div style={{ width: '1px', backgroundColor: activeTheme.border, height: '14px', alignSelf: 'center' }} />
                                        <button
                                            className="btn-h"
                                            onClick={() => handleReScreening(r)}
                                            style={{
                                                background: 'none', border: 'none', padding: '6px 10px',
                                                color: activeTheme.accent, cursor: 'pointer', fontSize: '11px',
                                                fontWeight: 'bold', display: 'flex', alignItems: 'center', gap: '6px'
                                            }}
                                        >
                                            <i className="fas fa-robot"></i> AI Scan
                                        </button>
                                    </div>
                                </div>
                            </td>
                        </>
                    ) : (
                        <>
                            <td style={tdStyle(activeTheme)}>{r.date}<br /><small>{r.jam_isi}</small></td>
                            <td style={tdStyle(activeTheme)}>{r.hm_km_unit}</td>
                            <td style={tdStyle(activeTheme)}>{r.flow_meter_value}</td>
                            <td style={{ ...tdStyle(activeTheme), color: '#48e618', fontWeight: 'bold' }}>
                                {r.consumed_qty ? Math.abs(r.consumed_qty) : '-'}
                            </td>
                            <td style={tdStyle(activeTheme)}>
                                <div style={statusBadge(tableType === 'QUEUE' ? 'Queue' : (r.screening_status || 'Unknown'))}>
                                    {tableType === 'QUEUE' ? 'QUEUE' : (r.screening_status || '-')}
                                </div>
                            </td>
                        </>
                    )}
                </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
});

function App() {
  const [records, setRecords] = useState([]);
  const [dbHistory, setDbHistory] = useState([]);
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [isNewDataIn, setIsNewDataIn] = useState(false);
  const [leftPanelOpen, setLeftPanelOpen] = useState(true);
  const [rightPanelOpen, setRightPanelOpen] = useState(true);
  const [selectedRecord, setSelectedRecord] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [fvInput, setFvInput] = useState("");
  const [actionMenuId, setActionMenuId] = useState(null);
  const [activeMenuId, setActiveMenuId] = useState(null);
  const [zoomImage, setZoomImage] = useState(null);
  const [zoomPosition, setZoomPosition] = useState({ x: 0, y: 0 });
  const [panImage, setPanImage] = useState(null); 
  const [sourceRect, setSourceRect] = useState(null);

  const [isDragging, setIsDragging] = useState(false);
  const [startX, setStartX] = useState(0);
  const [startY, setStartY] = useState(0);
  const [scrollLeft, setScrollLeft] = useState(0);
  const [scrollTop, setScrollTop] = useState(0);
  const dragRef = useRef(null);

  // --- STATE UNTUK EXPORT SPREADSHEET ---
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [viewMode, setViewMode] = useState('spreadsheet');
  const [tableLayout, setTableLayout] = useState('split'); 
  const [isTopCollapsed, setIsTopCollapsed] = useState(false);
  const [isBottomCollapsed, setIsBottomCollapsed] = useState(false); 

  // Load & Save Layout dari LocalStorage
  const [selectedFields, setSelectedFields] = useState(() => {
    const saved = localStorage.getItem(EXPORT_LAYOUT_KEY);
    return saved ? JSON.parse(saved) : ['no_unit_sap', 'tech_id', 'hm_km_unit', 'flow_meter_value', 'screening_status'];
  });

  useEffect(() => {
    localStorage.setItem(EXPORT_LAYOUT_KEY, JSON.stringify(selectedFields));
  }, [selectedFields]);

  // Logika Lock Button: Aktif jika semua record statusnya 'Verified'
  const isReadyToExport = dbHistory.some(r => {
    const status = r.screening_status || r.validation_status;
    return status === 'Verified';
  });

  // State Modal & Drag Over
  const [isMaximized, setIsMaximized] = useState(false);
  const [draggedFieldIdx, setDraggedFieldIdx] = useState(null); 
  const [dragOverIdx, setDragOverIdx] = useState(null); 

 // Object untuk melacak status masing-masing tabel
const [expandedTable, setExpandedTable] = useState({
  live: true,      // Default terbuka
  verified: true,  // Default terbuka
  ai: true         // Default terbuka
});

// Fungsi handleToggle yang baru (mengubah status tabel yang dipilih saja)
const handleToggle = (target) => {
  setExpandedTable(prev => ({
    ...prev,
    [target]: !prev[target] // Membalikkan status: jika true jadi false, dst.
  }));
};

    // --- FUNGSI DOWNLOAD TO EXCEL  ---
    const handleDownloadExcel = async () => {
    // 1. Ambil data yang sedang aktif di tabel (hasil filter/search)
    const currentTableData = records.length > 0 ? records : dbHistory;

    // 2. Filter: Hanya ambil yang statusnya 'Verified' dari data yang tampil di tabel tersebut
    const dataToProcess = currentTableData.filter(r => {
      const status = (r.screening_status || r.validation_status || "").toLowerCase();
      return status === 'verified';
    });

    if (dataToProcess.length === 0) {
      if (typeof showToast === 'function') showToast("Tidak ada data 'Verified' pada hasil filter saat ini", "warning");
      return;
    }

    try {
      // 3. Mapping Data (Urutan kolom sesuai selectedFields/Capture)
      const dataToExport = dataToProcess.map(record => {
        let row = {};
        selectedFields.forEach(field => {
          const cleanHeader = field.replace(/_/g, ' ').toUpperCase();
          row[cleanHeader] = record[field] || "-";
        });
        return row;
      });

      // 4. Generate Workbook
      const worksheet = XLSX.utils.json_to_sheet(dataToExport);
      const workbook = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(workbook, worksheet, "Dispatched_Logs_Report");
      
      const excelBuffer = XLSX.write(workbook, { bookType: 'xlsx', type: 'array' });
      const blob = new Blob([excelBuffer], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });

      // 5. Nama File Dinamis (Menggunakan Timestamp agar tidak konflik)
      const dateStr = new Date().toISOString().split('T')[0];
      const fileName = `FMS_Export_Verified_${dateStr}.xlsx`;

      // 6. Eksekusi Simpan File
      if ('showSaveFilePicker' in window) {
        const handle = await window.showSaveFilePicker({
          suggestedName: fileName,
          types: [{
            description: 'Excel file',
            accept: { 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': ['.xlsx'] },
          }],
        });
        
        const writable = await handle.createWritable();
        await writable.write(blob);
        await writable.close();
      } else {
        // Fallback untuk browser lama
        XLSX.writeFile(workbook, fileName);
      }

      // 7. UPDATE STATUS GLOBAL: Ubah record yang berhasil di-export menjadi 'Downloaded'
      // Kita update dbHistory secara keseluruhan agar status tersinkron di semua tabel
      const updatedHistory = dbHistory.map(r => {
        const wasExported = dataToProcess.some(processed => processed.id === r.id);
        if (wasExported) {
          return { 
            ...r, 
            screening_status: 'Downloaded', 
            validation_status: 'Downloaded' 
          };
        }
        return r;
      });

      if (typeof setDbHistory === 'function') {
        setDbHistory(updatedHistory);
      }

      if (typeof showToast === 'function') showToast("Export Berhasil & Status Diperbarui", "success");
      setIsExportModalOpen(false);

    } catch (error) {
      if (error.name === 'AbortError') {
        console.log("User membatalkan dialog simpan file.");
      } else {
        console.error("Download Error:", error);
        if (typeof showToast === 'function') showToast("Gagal memproses export", "error");
      }
    }
  };

  // --- MOUSE DRAG HANDLERS ---
  const handleMouseDown = (e) => {
    if (!dragRef.current) return;
    setIsDragging(true);
    setStartX(e.pageX - dragRef.current.offsetLeft);
    setStartY(e.pageY - dragRef.current.offsetTop);
    setScrollLeft(dragRef.current.scrollLeft);
    setScrollTop(dragRef.current.scrollTop);
  };

  const handleMouseMove = (e) => {
    if (!isDragging || !dragRef.current) return;
    e.preventDefault();
    const x = e.pageX - dragRef.current.offsetLeft;
    const y = e.pageY - dragRef.current.offsetTop;
    const walkX = (x - startX) * 1.5; 
    const walkY = (y - startY) * 1.5;
    dragRef.current.scrollLeft = scrollLeft - walkX;
    dragRef.current.scrollTop = scrollTop - walkY;
  };

  const handleMouseUpOrLeave = () => {
    setIsDragging(false);
  };

  const websocketHook = typeof useWebSocket === 'function' ? useWebSocket : (useWebSocket.default || useWebSocket.useWebSocket);
  const { lastJsonMessage, readyState } = websocketHook(WS_URL, { shouldReconnect: () => true, reconnectInterval: 3000 });

  // Kalkulasi Stats (Sinkron dengan DB History)
  const stats = useMemo(() => {
    const totalLiters = dbHistory.reduce((acc, curr) => acc + Number(curr.consumed_qty || 0), 0);
    const uniqueUnits = new Set(dbHistory.map(r => r.no_unit_sap)).size;
    const anomalies = dbHistory.filter(r => r.screening_status === 'Mismatch' || r.screening_status?.includes('Error')).length;
    const stations = new Set(dbHistory.map(r => r.gas_station)).size;
    return { totalLiters, uniqueUnits, anomalies, stations };
  }, [dbHistory]);

  // Fungsi Fetch Detail Transaksi
  const fetchSingleRecord = async (id) => {
    try {
      const response = await fetch(`${API_URL}${id}/`);
      if (response.ok) return await response.json();
    } catch (err) { console.error("Error fetch detail:", err); }
    return null;
  };

  // Fungsi Load Seluruh History
  const fetchHistory = async () => {
    try {
      const response = await fetch(API_URL); 
      const data = await response.json();
      setDbHistory(data);
    } catch (err) {
      console.error("Gagal load history:", err);
    }
  };

  // --- 1. Deklarasi Tanggal (Pastikan ini di paling atas sebelum Filter) ---
  const todayStr = new Date().toISOString().split('T')[0];

  // --- 2. Filter Tabel Atas (LIVE MONITOR) ---
  const filteredLive = useMemo(() => {
    return [...dbHistory]
      .sort((a, b) => b.id - a.id)
      .slice(0, 10);
  }, [dbHistory]);

  // --- 3. Filter Tabel Bawah (AI SCREENING) ---
  const filteredHistory = useMemo(() => {
    return dbHistory.filter(r => {
      const status = r.screening_status;
      if (status === 'Mismatch' || status === 'Processing' || status === 'Pending' || status === 'Mismatched') {
        return true;
      }
      const screeningDate = r.date_screening || r.date; 
      return screeningDate === todayStr;
    });
  }, [dbHistory, todayStr]);

  // --- 4. Load Data Awal ---
  useEffect(() => { 
    fetchHistory(); 
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (activeMenuId !== null && !event.target.closest('.action-menu-container')) {
        setActiveMenuId(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [activeMenuId]);

console.log("Connecting to:", WS_URL);

  // WebSocket Integration
  const websocketHook = typeof useWebSocket === 'function' ? useWebSocket : (useWebSocket.default || useWebSocket.useWebSocket);
  const { lastJsonMessage, readyState } = websocketHook(WS_URL, { shouldReconnect: () => true, reconnectInterval: 3000 });

  useEffect(() => {
    if (lastJsonMessage && lastJsonMessage.data) {
      const socketData = lastJsonMessage.data;

      // Ambil data lengkap dari API (untuk memastikan foto & field AI terbaru terbawa)
      fetchSingleRecord(socketData.id).then(fullData => {
        const dataToDisplay = fullData || { ...socketData };

        setDbHistory((prev) => {
          // 1. Cek apakah record dengan ID ini sudah ada di tabel?
          const isExist = prev.find(item => item.id === dataToDisplay.id);

          if (isExist) {
            // JIKA SUDAH ADA: Update data yang lama (hasil screening AI masuk ke sini)
            return prev.map(item => 
              item.id === dataToDisplay.id ? { ...item, ...dataToDisplay } : item
            );
          } else {
            // JIKA BELUM ADA: Berarti ini transaksi baru masuk dari HP
            setIsNewDataIn(true);
            setTimeout(() => setIsNewDataIn(false), 3000);
            return [dataToDisplay, ...prev]; // Tambah ke baris paling atas
          }
        });

        // Sinkronkan juga ke state records (untuk Live Monitor)
        setRecords((prev) => {
          const isExist = prev.find(item => item.id === dataToDisplay.id);
          if (isExist) {
            return prev.map(item => item.id === dataToDisplay.id ? { ...item, ...dataToDisplay } : item);
          }
          return [dataToDisplay, ...prev.slice(0, 9)];
        });
      });
    }
  }, [lastJsonMessage]);

  const isConnected = readyState === 1;

  const themes = {
    dark: { bg: '#0f172a', card: '#1e293b', text: '#f1f5f9', subText: '#94a3b8', accent: '#06b6d4', border: '#334155', tableHead: '#0f172a', tableTitle: '#22d3ee' },
    standard: { bg: '#6ca7ff', card: '#f8fafc', text: '#0f172a', subText: '#334155', accent: '#2563eb', border: '#94a3b8', tableHead: '#98beec', tableTitle: '#1d4ed8' }
  };
  const activeTheme = isDarkMode ? themes.dark : themes.standard;

  const handleImageZoom = (e, imagePath, isLiveMode = false) => {
    if (!imagePath) return;
    const rect = e.target.getBoundingClientRect();
    setSourceRect(rect);
    const zoomWidth = 350;
    const gap = 15;
    let leftPos;
    if (isLiveMode) {
      leftPos = rect.right + gap;
      if (leftPos + zoomWidth > window.innerWidth) leftPos = window.innerWidth - zoomWidth - 20;
    } else {
      leftPos = rect.left - zoomWidth - gap;
      if (leftPos < 20) leftPos = 20;
    }
    let topPos = rect.top - 50; 
    if (topPos < 20) topPos = 20;
    if (topPos + 250 > window.innerHeight) topPos = window.innerHeight - 270;
    setZoomPosition({ x: leftPos, y: topPos });
    setZoomImage(getImageUrl(imagePath));
  };

  const handleOpenValidation = (r) => {
  // 1. Proteksi jika sudah Verified
  if (r.screening_status === 'Verified') {
    showToast(`Data unit ${r.no_unit_sap} sudah Verified by System.`, "shield-check");
    return;
  }

  // 2. Logic Isi Otomatis jika Match (agar user tidak perlu ngetik lagi)
  const isHMMatch = Number(r.hm_km_unit) === Number(r.ai_hm_read);
  const isFlowMatch = Number(r.flow_meter_value) === Number(r.ai_flow_read);

  setSelectedRecord(r);
  setFinalHM(isHMMatch ? r.ai_hm_read : ''); // Isi otomatis jika Match
  setFinalFlow(isFlowMatch ? r.ai_flow_read : ''); // Isi otomatis jika Match
  setIsModalOpen(true);
  setActiveMenuId(null);
};

  // Fungsi Update Modal Validation
  const [finalHM, setFinalHM] = useState('');
  const [finalFlow, setFinalFlow] = useState('');
  const [notification, setNotification] = useState({ show: false, message: '' });

  const handleSubmitValidation = async () => {
  if (!selectedRecord) return;

  // Pastikan nilai yang dikirim adalah string atau angka yang valid
  const hmToSubmit = finalHM || selectedRecord.ai_hm_read || selectedRecord.hm_km_unit;
  const flowToSubmit = finalFlow || selectedRecord.ai_flow_read || selectedRecord.flow_meter_value;

  try {
    // Gunakan URL search params atau pastikan format URL bersih
    const requestUrl = `${BASE_URL}/api/fuel-transactions/${selectedRecord.id}/validate/`.replace(/([^:]\/)\/+/g, "$1");
    
    const response = await fetch(requestUrl, {
      method: 'PATCH',
      headers: { 
        'Content-Type': 'application/json',
        'Accept': 'application/json' 
      },
      body: JSON.stringify({
        final_hm_value: String(hmToSubmit), // Backend terkadang minta string
        final_flow_value: String(flowToSubmit),
        screening_status: 'Verified'
      }),
    });

    if (response.ok) {
      const updatedData = await response.json();
      
      const syncUpdate = (prev) => 
        prev.map(item => (item.id === selectedRecord.id ? { ...item, ...updatedData } : item));

      setDbHistory(syncUpdate);
      setRecords(syncUpdate);

      // RESET & CLOSE
      setIsModalOpen(false); 
      setSelectedRecord(null);
      setFinalHM('');
      setFinalFlow('');
  
      showToast(`Data unit ${selectedRecord.no_unit_sap} berhasil divalidasi`, "check-circle");

      // Refresh data dari server untuk sinkronisasi total
      if (typeof fetchAllData === 'function') {
        setTimeout(() => fetchAllData(), 500); 
      }
    } else {
      const errorData = await response.json();
      showToast(`Gagal simpan: ${errorData.message || 'Cek input'}`, "exclamation-triangle");
    }
  } catch (error) {
    console.error("Error Validation:", error);
    showToast("Gagal menghubungi server", "wifi");
  }
};

// Fungsi Re-Screening
const showToast = (msg, icon = "info-circle") => {
  setNotification({ show: true, message: msg, icon: icon });
  // Toast otomatis hilang setelah 2.5 detik
  setTimeout(() => {
    setNotification(prev => ({ ...prev, show: false }));
  }, 2500);
};

const handleReScreening = async (r) => {
  if (!r?.id) return;

  showToast(`Memulai AI Re-Screening: ${r.no_unit_sap}...`, "robot");
  setActiveMenuId(null);

  try {
    const targetUrl = new URL(`${r.id}/re_screening/`, API_URL).toString();
    
    console.log("🔥 URL YANG DIKIRIM:", targetUrl);

    const response = await fetch(targetUrl, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      }
    });

    if (response.ok) {
      const result = await response.json();
      const sync = prev => prev.map(item => item.id === r.id ? { ...item, ...result } : item);
      setRecords(sync);
      setDbHistory(sync);
      showToast("AI sedang bekerja", "cog");
    } else {
      showToast("Server menolak request", "exclamation-triangle");
    }
  } catch (error) {
    console.error("Network Error:", error);
    showToast("Koneksi gagal", "wifi");
  }
};

  return (
    
    <div style={{ backgroundColor: activeTheme.bg, color: activeTheme.text, height: '100vh', width: '100vw', overflow: 'hidden', display: 'flex', flexDirection: 'column', padding: '10px', boxSizing: 'border-box' }}>
      <style>{`
  .btn-h:hover {
    filter: brightness(1.2);
    transform: translateY(-1px);
  }

  .custom-scroll::-webkit-scrollbar {
    width: 4px;
    height: 4px;
  }

  .custom-scroll::-webkit-scrollbar-thumb {
    background: ${activeTheme.border};
    border-radius: 4px;
  }

  @keyframes blink {
    50% {
      opacity: 0.3;
    }
  }

  .row-highlight {
    animation: pulse 2s infinite;
  }

  @keyframes pulse {
    0% {
      background-color: transparent;
    }
    50% {
      background-color: rgba(6, 182, 212, 0.15);
    }
    100% {
      background-color: transparent;
    }
  }

  .panel-transition {
    transition:
      flex-basis 0.4s cubic-bezier(0.4, 0, 0.2, 1),
      min-width 0.4s,
      max-width 0.4s,
      opacity 0.3s;
  }

  @keyframes progressIndeterminate {
    0% { transform: translateX(-100%); }
    100% { transform: translateX(100%); }
  }

  .progress-loading-bar {
    animation: progressIndeterminate 1.5s infinite linear;
    background: linear-gradient(90deg, transparent, #00e5ff, transparent);
    width: 50% !important; /* Buat bar lebih pendek agar terlihat bergerak */
  }

  /* ===== MODAL ANIMATION ===== */

  @keyframes modalFadeIn {
    from {
      opacity: 0;
      backdrop-filter: blur(0px);
    }
    to {
      opacity: 1;
      backdrop-filter: blur(4px);
    }
  }

  @keyframes slideMenuIn {
    from {
      opacity: 0;
      transform: translateY(-8px) scale(0.96);
    }
    to {
      opacity: 1;
      transform: translateY(0px) scale(1);
    }
  }

  @keyframes modalSlideUp {
    from {
      opacity: 0;
      transform: translateY(40px) scale(0.96);
    }
    to {
      opacity: 1;
      transform: translateY(0px) scale(1);
    }
  }

  .final-input {
  width: 100%;
  padding: 12px;
  background: rgba(15, 23, 42, 0.8);
  border: 1px solid #1e293b;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  outline: none;
  transition: border 0.3s;
}

.final-input:focus {
  border-color: #00e5ff; /* Warna aksen biru neon */
}

.btn-val {
  flex: 1;
  padding: 10px;
  background: #2563eb;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 600;
}

.btn-ai-val {
  flex: 1;
  padding: 10px;
  background: #0891b2;
  border: none;
  border-radius: 6px;
  color: white;
  font-weight: 600;
}

.btn-submit {
  flex: 1;
  padding: 15px;
  background: #10b981;
  border: none;
  border-radius: 8px;
  color: white;
  font-weight: bold;
  cursor: pointer;
}

@keyframes slideIn {
    from { transform: translateX(100%); opacity: 0; }
    to { transform: translateX(0); opacity: 1; }
  }


@keyframes dash {
  to {
    stroke-dashoffset: 0;
  }
}

.connector-line {
  stroke-dasharray: 5;
  animation: dash 0.5s linear infinite;
  pointer-events: none;
  z-index: 9999;
}

@keyframes flow {
  from {
    stroke-dashoffset: 20;
  }
  to {
    stroke-dashoffset: 0;
  }
}

@keyframes pulse-opacity {
  0% { opacity: 0.3; stroke-width: 1.5; }
  50% { opacity: 0.8; stroke-width: 2.5; }
  100% { opacity: 0.3; stroke-width: 1.5; }
}

.connector-line-animated {
  stroke-dasharray: 5, 5;
  animation: 
    flow 1s linear infinite, 
    pulse-opacity 2s ease-in-out infinite;
}

`}</style>

{/* HEADER SECTION */}
<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '15px', flexShrink: 0 }}>
  <div style={{ textAlign: 'left' }}>
    <h1 style={{ margin: 0, fontSize: '22px', fontWeight: '600', color: isDarkMode ? activeTheme.accent : activeTheme.text }}>FMS — Fuel Management System</h1>
    <p style={{ margin: 0, fontSize: '14px', color: activeTheme.subText }}>Real-time Transaction Hub & AI Screening</p>
  </div>
  <div style={{ display: 'flex', gap: '8px' }}>
    <button className="btn-h" onClick={() => setIsDarkMode(!isDarkMode)} style={{ padding: '8px 12px', borderRadius: '5px', border: 'none', backgroundColor: activeTheme.accent, color: '#fff', fontSize: '10px', fontWeight: 'bold', cursor: 'pointer' }}>{isDarkMode ? 'STANDARD' : 'DARK'}</button>
    <button 
      className="btn-h" 
      disabled={!isReadyToExport}
      onClick={() => {
        // Set default fields sesuai capture image_2ded8a.png saat modal dibuka
        setSelectedFields([
          'no_unit_sap', 'gas_station', 'date', 'jam_isi', 'hm_km_unit', 
          'consumed_qty', 'fluid_type', 'measuring_position', 
          'pit_location_id', 'header_text', 'flow_meter_value'
        ]);
        setIsExportModalOpen(true);
      }}
      style={{ 
        padding: '8px 16px', 
        borderRadius: '5px', 
        border: 'none', 
        backgroundColor: isReadyToExport ? '#10b981' : '#1e293b',
        color: isReadyToExport ? '#fff' : '#64748b', 
        fontSize: '10px', 
        fontWeight: 'bold', 
        cursor: isReadyToExport ? 'pointer' : 'not-allowed',
        opacity: isReadyToExport ? 1 : 0.6,
        boxShadow: isReadyToExport ? '0 0 15px rgba(16, 185, 129, 0.4)' : 'none',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        transition: 'all 0.3s ease'
      }}
    >
      <i className={`fas ${isReadyToExport ? 'fa-file-excel' : 'fa-lock'}`}></i>
      {isReadyToExport ? 'EXPORT SPREADSHEET' : 'VALIDATION INCOMPLETE'}
    </button>
  </div>
</div>

      {/* DASHBOARD CARDS */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '10px', marginBottom: '12px', flexShrink: 0 }}>
        <StatCard title="Total Fuel Liters" value={`${stats.totalLiters.toLocaleString()} L`} color="#06b6d4" activeTheme={activeTheme} subValue="Actual Consumption" />
        <StatCard title="Active Units" value={stats.uniqueUnits} color="#10b981" activeTheme={activeTheme} subValue="Last 24 Hours" />
        <StatCard title="AI Anomaly Flags" value={stats.anomalies} color="#ef4444" activeTheme={activeTheme} subValue="Check Required" />
        <StatCard title="Gas Stations" value={stats.stations} color="#f59e0b" activeTheme={activeTheme} subValue="Operational" />
      </div>

      {/* MAIN CONTENT AREA */}
      <div style={{ display: 'flex', flex: 1, gap: '10px', minHeight: 0, position: 'relative' }}>
        
        {/* PANEL KIRI (System Monitor) */}
        <div className="panel-transition" style={{ flexBasis: leftPanelOpen ? '240px' : '0px', minWidth: leftPanelOpen ? '240px' : '0px', maxWidth: leftPanelOpen ? '240px' : '0px', opacity: leftPanelOpen ? 1 : 0, overflow: 'hidden', backgroundColor: activeTheme.card, borderRadius: '8px', border: `1px solid ${activeTheme.border}`, padding: leftPanelOpen ? '15px' : '0px', display: 'flex', flexDirection: 'column' }}>
          <h4 style={{ margin: '0 0 10px 0', color: activeTheme.accent, fontSize: '13px' }}>SYSTEM MONITOR</h4>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '11px', color: isConnected ? '#10b981' : '#ef4444', marginBottom: '10px' }}>
            <div style={{ width: '8px', height: '8px', borderRadius: '50%', backgroundColor: isConnected ? '#10b981' : '#ef4444' }} />
            {isConnected ? 'CONNECTED' : 'DISCONNECTED'}
          </div>
          <div style={{ backgroundColor: '#000', borderRadius: '6px', padding: '10px', fontFamily: 'monospace', fontSize: '11px', color: '#10b981', flex: 1, overflowY: 'auto' }}>
            {`> Socket: Active`}<br/>{`> Archive: ${dbHistory.length}`}<br/>
            {isNewDataIn && <span style={{ color: '#fff' }}>{`> RECV_DATA_SYNC...`}</span>}
          </div>
        </div>

        {/* PANEL TENGAH (Tabel) */}
        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '12px', minHeight: 0, position: 'relative' }}>
          {/* Tombol Slider */}
          <button onClick={()=>setLeftPanelOpen(!leftPanelOpen)} style={{ position: 'absolute', left: '-10px', top: '50%', zIndex: 20, width: '20px', height: '40px', borderRadius: '10px', border: 'none', backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff' }}>{leftPanelOpen ? '◀' : '▶'}</button>
          <button onClick={()=>setRightPanelOpen(!rightPanelOpen)} style={{ position: 'absolute', right: '-10px', top: '50%', zIndex: 20, width: '20px', height: '40px', borderRadius: '10px', border: 'none', backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff' }}>{rightPanelOpen ? '▶' : '◀'}</button>

          {/* PANEL TENGAH (Tabel) */}
<div style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: '12px', minHeight: 0, position: 'relative' }}>
  
  {/* Tombol Slider (Tetap biarkan ada) */}
  <button onClick={()=>setLeftPanelOpen(!leftPanelOpen)} style={{ position: 'absolute', left: '-10px', top: '50%', zIndex: 20, width: '20px', height: '40px', borderRadius: '10px', border: 'none', backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff' }}>{leftPanelOpen ? '◀' : '▶'}</button>
  <button onClick={()=>setRightPanelOpen(!rightPanelOpen)} style={{ position: 'absolute', right: '-10px', top: '50%', zIndex: 20, width: '20px', height: '40px', borderRadius: '10px', border: 'none', backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff' }}>{rightPanelOpen ? '▶' : '◀'}</button>
  
{/* CONTAINER KHUSUS TABEL - DINAMIS & RESPONSIF */}
<div style={{ 
  display: 'grid',
  gridTemplateColumns: '1fr 1fr', 
  // gridTemplateRows menggunakan 'auto' agar saat tabel minimize (44px), 
  // baris tersebut langsung mengecil dan memberikan ruang sisanya ke baris lainnya (1fr).
  gridTemplateRows: 'auto 1fr', 
  gap: '16px',
  flex: 1,
  minHeight: 0,
  padding: '10px 0',
  overflow: 'hidden'
}}>
  
  {/* TABEL LIVE QUEUE (Kiri Atas) */}
  <div style={{ gridColumn: '1 / 2', display: 'flex', minHeight: 0 }}>
    <FuelTable 
      title="LIVE QUEUE"
      tableType="QUEUE"
      data={dbHistory}
      activeTheme={activeTheme}
      isDarkMode={isDarkMode}
      // Menggunakan state objek: expandedTable.live
      isExpanded={expandedTable.live}
      onToggle={() => handleToggle('live')} 
      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}
      setZoomImage={setZoomImage} // Tambahkan ini untuk fitur auto-hide zoom
      formatIndoDate={formatIndoDate}
    />
  </div>

  {/* TABEL VERIFIED (Kanan Atas) */}
  <div style={{ gridColumn: '2 / 3', display: 'flex', minHeight: 0 }}>
    <FuelTable 
      title="READY TO EXPORT"
      tableType="VERIFIED"
      data={dbHistory}
      activeTheme={activeTheme}
      isDarkMode={isDarkMode}
      isExpanded={expandedTable.verified}
      onToggle={() => handleToggle('verified')}
      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}
      setZoomImage={setZoomImage}
      formatIndoDate={formatIndoDate}
    />
  </div>

  {/* TABEL AI WORKBENCH (Bawah - Full Width) */}
  <div style={{ gridColumn: '1 / 3', display: 'flex', minHeight: 0 }}> 
    <FuelTable 
      title="AI SCREENING WORKBENCH"
      tableType="AI_WORKBENCH"
      data={dbHistory}
      activeTheme={activeTheme}
      isDarkMode={isDarkMode}
      isExpanded={expandedTable.ai}
      onToggle={() => handleToggle('ai')}
      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}
      setZoomImage={setZoomImage}
      handleOpenValidation={handleOpenValidation}
      handleReScreening={handleReScreening}
      formatIndoDate={formatIndoDate}
      activeMenuId={activeMenuId}
      setActiveMenuId={setActiveMenuId}
      menuButtonStyle={menuButtonStyle}
      OCRStatusLabel={OCRStatusLabel}
    />
  </div>
</div>

</div>
        </div>

        {/* PANEL KANAN (Analytics) */}
        <div className="panel-transition" style={{ flexBasis: rightPanelOpen ? '240px' : '0px', minWidth: rightPanelOpen ? '240px' : '0px', maxWidth: rightPanelOpen ? '240px' : '0px', opacity: rightPanelOpen ? 1 : 0, overflow: 'hidden', backgroundColor: activeTheme.card, borderRadius: '8px', border: `1px solid ${activeTheme.border}`, padding: rightPanelOpen ? '15px' : '0px', display: 'flex', flexDirection: 'column' }}>
          <h4 style={{ margin: '0 0 15px 0', color: activeTheme.accent, fontSize: '13px' }}>AI ANALYTICS</h4>
          <div style={{ backgroundColor: isDarkMode ? 'rgba(0,0,0,0.2)' : '#fff', padding: '15px', borderRadius: '8px', textAlign: 'center', border: `1px solid ${activeTheme.border}` }}>
            <span style={{ fontSize: '28px', fontWeight: '700', color: activeTheme.accent }}>{dbHistory.length}</span>
            <p style={{ margin: 0, fontSize: '10px', color: activeTheme.subText }}>TOTAL RECORDS</p>
          </div>
          <div style={{ marginTop: '20px' }}>
            <div style={{ fontSize: '11px', color: activeTheme.subText, marginBottom: '10px' }}>Consumption Trend</div>
            <div style={{ display: 'flex', alignItems: 'flex-end', height: '100px', gap: '4px' }}>
              {dbHistory.slice(0, 10).reverse().map((r, i) => (
                <div key={i} style={{ flex: 1, backgroundColor: activeTheme.accent, height: `${Math.min((Number(r.consumed_qty)/1000)*100, 100)}%`, opacity: 0.6, borderRadius: '2px 2px 0 0' }} />
              ))}
            </div>
          </div>
        </div>
      </div>

      {zoomImage && (
  <div
    style={{
      position: 'fixed',
      top: `${zoomPosition.y}px`,
      left: `${zoomPosition.x}px`,
      zIndex: 999,
      backgroundColor: '#111827',
      padding: '10px',
      borderRadius: '10px',
      border: '1px solid rgba(255,255,255,0.12)',
      boxShadow: '0 10px 30px rgba(0,0,0,0.5)'
    }}
  >
    <img
      src={zoomImage}
      alt="Zoom Preview"
      style={{
        width: '320px',
        maxHeight: '420px',
        objectFit: 'contain',
        borderRadius: '8px'
      }}
    />
  </div>
)}

{/* MODAL VALIDATION */}
{isModalOpen && selectedRecord && (
  <div
    style={{
      position: 'fixed',
      inset: 0,
      backgroundColor: 'rgba(0,0,0,0.85)',
      zIndex: 100,
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      animation: 'modalFadeIn 0.35s ease',
      backdropFilter: 'blur(5px)'
    }}
  >
    <div
      style={{
        width: '1180px',
        maxHeight: '90vh',
        overflowY: 'auto',
        backgroundColor: activeTheme.card,
        borderRadius: '14px',
        padding: '24px',
        border: `1px solid ${activeTheme.border}`,
        boxShadow: '0 20px 60px rgba(0,0,0,0.6)',
        animation: 'modalSlideUp 0.4s cubic-bezier(0.22, 1, 0.36, 1)',
      }}
    >
      {/* HEADER */}
      <div style={{ marginBottom: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h2 style={{ margin: 0, color: activeTheme.accent, fontSize: '24px', fontWeight: '700' }}>
          Validation: {selectedRecord.no_unit_sap || "-"} / {selectedRecord.tech_id || "-"}
        </h2>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
           <div style={{ 
             fontSize: '11px', 
             fontWeight: 'bold', 
             color: '#fff', 
             backgroundColor: selectedRecord.screening_status.includes('Match') ? '#10b981' : 'rgba(255,255,255,0.05)', 
             padding: '4px 15px', 
             borderRadius: '20px',
             border: selectedRecord.screening_status.includes('Match') ? '1px solid #10b981' : '1px solid transparent'
           }}>
             Status: {selectedRecord.screening_status}
           </div>
           <div style={{ fontSize: '12px', color: activeTheme.subText }}>ID: #{selectedRecord.id}</div>
        </div>
      </div>

      {/* PHOTO SECTION */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '20px' }}>
        {/* HM SECTION */}
        <div style={{ backgroundColor: 'rgba(0,0,0,0.2)', padding: '15px', borderRadius: '10px', border: `1px solid ${activeTheme.border}` }}>
          <h4 style={{ marginBottom: '12px', color: '#22d3ee', marginTop: 0 }}>
            HM / KM Validation {selectedRecord.screening_status === 'HM Match' && '✅'}
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <div style={{ fontSize: '10px', color: activeTheme.subText, marginBottom: '6px' }}>FULL SIZE (CLICK TO ZOOM)</div>
              <img 
                src={getImageUrl(selectedRecord.hm_foto_full)} 
                onClick={() => setPanImage(getImageUrl(selectedRecord.hm_foto_full))}
                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                style={{ width: '100%', height: '220px', objectFit: 'contain', backgroundColor: '#000', borderRadius: '6px', cursor: 'zoom-in', transition: 'transform 0.3s ease' }} 
                alt="HM Full" 
              />
            </div>
            <div>
              <div style={{ fontSize: '10px', color: activeTheme.subText, marginBottom: '6px' }}>AI CROP (ROI)</div>
              <img src={getImageUrl(selectedRecord.hm_foto)} style={{ width: '100%', height: '220px', objectFit: 'contain', backgroundColor: '#000', borderRadius: '6px' }} alt="HM Crop" />
            </div>
          </div>
        </div>

        {/* FLOW SECTION */}
        <div style={{ backgroundColor: 'rgba(0,0,0,0.2)', padding: '15px', borderRadius: '10px', border: `1px solid ${activeTheme.border}` }}>
          <h4 style={{ marginBottom: '12px', color: '#22d3ee', marginTop: 0 }}>
            FLOW METER Validation {selectedRecord.screening_status === 'Flow Match' && '✅'}
          </h4>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <div style={{ fontSize: '10px', color: activeTheme.subText, marginBottom: '6px' }}>FULL SIZE (CLICK TO ZOOM)</div>
              <img 
                src={getImageUrl(selectedRecord.flow_meter_foto_full)} 
                onClick={() => setPanImage(getImageUrl(selectedRecord.flow_meter_foto_full))}
                onMouseEnter={(e) => e.target.style.transform = 'scale(1.05)'}
                onMouseLeave={(e) => e.target.style.transform = 'scale(1)'}
                style={{ width: '100%', height: '220px', objectFit: 'contain', backgroundColor: '#000', borderRadius: '6px', cursor: 'zoom-in', transition: 'transform 0.3s ease' }} 
                alt="Flow Full" 
              />
            </div>
            <div>
              <div style={{ fontSize: '10px', color: activeTheme.subText, marginBottom: '6px' }}>AI CROP (ROI)</div>
              <img src={getImageUrl(selectedRecord.flow_meter_foto)} style={{ width: '100%', height: '220px', objectFit: 'contain', backgroundColor: '#000', borderRadius: '6px' }} alt="Flow Crop" />
            </div>
          </div>
        </div>
      </div>

      {/* FINAL VALUE INPUT SECTION */}
      <div style={{ display: 'flex', gap: '20px', marginBottom: '25px' }}>
        {/* HM COLUMN */}
        <div style={{ flex: 1, opacity: selectedRecord.screening_status === 'HM Match' ? 0.6 : 1 }}>
          <label style={{ fontSize: '12px', fontWeight: 'bold', color: activeTheme.subText, display: 'block', marginBottom: '8px' }}>
            FINAL HM VALUE {selectedRecord.screening_status === 'HM Match' && '(VERIFIED BY AI)'}
          </label>
          <div style={{ display: 'flex', gap: '8px', marginBottom: '10px' }}>
            <button 
              className="btn-val" 
              disabled={selectedRecord.screening_status === 'HM Match'}
              onClick={() => setFinalHM(selectedRecord.hm_km_unit)} 
              style={{ fontSize: '11px', flex: 1, cursor: selectedRecord.screening_status === 'HM Match' ? 'not-allowed' : 'pointer' }}
            >
              📱 DEVICE: {selectedRecord.hm_km_unit || 0}
            </button>
            <button 
              className="btn-ai-val" 
              disabled={selectedRecord.screening_status === 'HM Match'}
              onClick={() => setFinalHM(selectedRecord.ai_hm_read)} 
              style={{ fontSize: '11px', flex: 1, cursor: selectedRecord.screening_status === 'HM Match' ? 'not-allowed' : 'pointer' }}
            >
              🤖 AI: {selectedRecord.ai_hm_read || 0}
            </button>
          </div>
          <input 
            type="number" 
            placeholder={selectedRecord.screening_status === 'HM Match' ? "Data locked" : "Input confirmed HM..."}
            value={finalHM} 
            disabled={selectedRecord.screening_status === 'HM Match'}
            onChange={(e) => setFinalHM(e.target.value)} 
            className="final-input"
            style={{ 
              border: finalHM ? `1px solid ${activeTheme.accent}` : `1px solid ${activeTheme.border}`,
              backgroundColor: selectedRecord.screening_status === 'HM Match' ? 'rgba(0,0,0,0.1)' : 'transparent',
              color: selectedRecord.screening_status === 'HM Match' ? activeTheme.subText : activeTheme.text
            }}
          />
        </div>

        {/* FLOW COLUMN */}
        <div style={{ flex: 1, opacity: selectedRecord.screening_status === 'Flow Match' ? 0.6 : 1 }}>
          <label style={{ fontSize: '12px', fontWeight: 'bold', color: activeTheme.subText, display: 'block', marginBottom: '8px' }}>
            FINAL FLOW METER {selectedRecord.screening_status === 'Flow Match' && '(VERIFIED BY AI)'}
          </label>
          <div style={{ display: 'flex', gap: '8px', marginBottom: '10px' }}>
            <button 
              className="btn-val" 
              disabled={selectedRecord.screening_status === 'Flow Match'}
              onClick={() => setFinalFlow(selectedRecord.flow_meter_value)} 
              style={{ fontSize: '11px', flex: 1, cursor: selectedRecord.screening_status === 'Flow Match' ? 'not-allowed' : 'pointer' }}
            >
              📱 DEVICE: {selectedRecord.flow_meter_value || 0}
            </button>
            <button 
              className="btn-ai-val" 
              disabled={selectedRecord.screening_status === 'Flow Match'}
              onClick={() => setFinalFlow(selectedRecord.ai_flow_read)} 
              style={{ fontSize: '11px', flex: 1, cursor: selectedRecord.screening_status === 'Flow Match' ? 'not-allowed' : 'pointer' }}
            >
              🤖 AI: {selectedRecord.ai_flow_read || 0}
            </button>
          </div>
          <input 
            type="number" 
            placeholder={selectedRecord.screening_status === 'Flow Match' ? "Data locked" : "Input confirmed Flow..."}
            value={finalFlow} 
            disabled={selectedRecord.screening_status === 'Flow Match'}
            onChange={(e) => setFinalFlow(e.target.value)} 
            className="final-input"
            style={{ 
              border: finalFlow ? `1px solid ${activeTheme.accent}` : `1px solid ${activeTheme.border}`,
              backgroundColor: selectedRecord.screening_status === 'Flow Match' ? 'rgba(0,0,0,0.1)' : 'transparent',
              color: selectedRecord.screening_status === 'Flow Match' ? activeTheme.subText : activeTheme.text
            }}
          />
        </div>
      </div>

      {/* FOOTER ACTIONS */}
      <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '12px', borderTop: `1px solid ${activeTheme.border}`, paddingTop: '20px' }}>
        <button 
          onClick={() => { setIsModalOpen(false); setFinalHM(''); setFinalFlow(''); }} 
          style={{ padding: '12px 24px', borderRadius: '8px', border: `1px solid ${activeTheme.border}`, backgroundColor: 'transparent', color: activeTheme.text, cursor: 'pointer', fontWeight: '600' }}
          onMouseEnter={(e) => e.target.style.backgroundColor = 'rgba(255,255,255,0.05)'}
          onMouseLeave={(e) => e.target.style.backgroundColor = 'transparent'}
        >
          CANCEL
        </button>
        <button 
          onClick={handleSubmitValidation} 
          className="btn-submit"
          onMouseEnter={(e) => { e.target.style.transform = 'scale(1.02)'; e.target.style.backgroundColor = '#0ea5e9'; }}
          onMouseLeave={(e) => { e.target.style.transform = 'scale(1)'; e.target.style.backgroundColor = activeTheme.accent; }}
          style={{ 
            padding: '12px 40px', 
            minWidth: '200px', 
            backgroundColor: activeTheme.accent, 
            color: '#fff', 
            border: 'none', 
            borderRadius: '8px', 
            fontWeight: 'bold', 
            cursor: 'pointer',
            transition: 'all 0.3s ease'
          }}
        >
          SUBMIT VALIDATION
        </button>
      </div>
    </div>
  </div>
)}

{/* MODAL EXPORT LAYOUT STUDIO */}
{isExportModalOpen && (
  <Rnd
    size={isMaximized ? { width: '100vw', height: '100vh' } : { width: 1100, height: 750 }}
    position={isMaximized ? { x: 0, y: 0 } : undefined}
    disableDragging={isMaximized}
    dragHandleClassName="modal-drag-handle"
    style={{ zIndex: 1000, position: 'fixed' }}
  >
    <div style={{ 
      width: '100%', height: '100%', backgroundColor: '#0f172a', borderRadius: isMaximized ? '0' : '15px', 
      border: '1px solid rgba(255,255,255,0.1)', display: 'flex', flexDirection: 'column', 
      boxShadow: '0 0 50px rgba(0,0,0,0.5)', overflow: 'hidden' 
    }}>
      
      {/* HEADER */}
      <div className="modal-drag-handle" style={{ padding: '15px 25px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', background: 'rgba(255,255,255,0.03)', borderBottom: '1px solid rgba(255,255,255,0.05)', cursor: 'move' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
          <h2 style={{ margin: 0, color: '#22d3ee', fontSize: '16px', fontWeight: '800', letterSpacing: '1px' }}>
            <i className="fas fa-layer-group" style={{ marginRight: '10px' }}></i> EXPORT STUDIO
          </h2>
          <div style={{ display: 'flex', gap: '5px' }}>
            <button onClick={() => setViewMode('spreadsheet')} style={viewMode === 'spreadsheet' ? activeModeStyle : inactiveModeStyle}>SPREADSHEET</button>
            <button onClick={() => setViewMode('table')} style={viewMode === 'table' ? activeModeStyle : inactiveModeStyle}>DYNAMIC TABLE</button>
          </div>
        </div>
        <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
          <button onClick={() => setIsMaximized(!isMaximized)} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' }}>
            <i className={`fas ${isMaximized ? 'fa-compress' : 'fa-expand'}`}></i>
          </button>
          <button onClick={() => setIsExportModalOpen(false)} style={{ background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '24px' }}>&times;</button>
        </div>
      </div>

      <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
        
        {/* PANEL KIRI: SELECTOR & REORDER */}
        <div style={{ width: '300px', padding: '20px', borderRight: '1px solid rgba(255,255,255,0.05)', backgroundColor: 'rgba(0,0,0,0.2)', overflowY: 'auto' }} className="custom-scroll">
          <span style={{ fontSize: '10px', color: '#38bdf8', fontWeight: 'bold', display: 'block', marginBottom: '15px' }}>DRAG TO REORDER COLUMNS</span>
          
          {selectedFields.map((f, index) => (
            <div 
              key={f}
              draggable
              onDragStart={() => setDraggedFieldIdx(index)}
              onDragOver={(e) => { e.preventDefault(); setDragOverIdx(index); }}
              onDragLeave={() => setDragOverIdx(null)}
              onDrop={() => {
                const updated = [...selectedFields];
                const movedItem = updated.splice(draggedFieldIdx, 1)[0];
                updated.splice(index, 0, movedItem);
                setSelectedFields(updated);
                setDragOverIdx(null);
              }}
              style={{
                padding: '10px 15px', marginBottom: '6px', borderRadius: '6px', cursor: 'grab', fontSize: '11px',
                backgroundColor: 'rgba(34, 211, 238, 0.05)', 
                border: '1px solid rgba(34, 211, 238, 0.3)',
                color: '#fff', display: 'flex', justifyContent: 'space-between',
                borderTop: dragOverIdx === index ? '3px solid #22d3ee' : '1px solid rgba(34, 211, 238, 0.3)',
                transition: 'all 0.1s ease'
              }}
            >
              <span><i className="fas fa-grip-vertical" style={{ marginRight: '10px', color: '#64748b' }}></i> {f.replace(/_/g, ' ').toUpperCase()}</span>
            </div>
          ))}
          
          <hr style={{ border: 'none', borderTop: '1px solid rgba(255,255,255,0.05)', margin: '20px 0' }} />
          <span style={{ fontSize: '10px', color: '#64748b', fontWeight: 'bold', display: 'block', marginBottom: '10px' }}>ADD MORE FIELDS</span>
          {AVAILABLE_FIELDS.filter(f => !selectedFields.includes(f)).map(f => (
            <div 
              key={f}
              onClick={() => setSelectedFields([...selectedFields, f])}
              style={{ padding: '8px 12px', marginBottom: '5px', borderRadius: '6px', cursor: 'pointer', fontSize: '10px', backgroundColor: 'rgba(255,255,255,0.02)', color: '#64748b', border: '1px solid transparent' }}
            >
              + {f.replace(/_/g, ' ').toUpperCase()}
            </div>
          ))}
        </div>

        {/* PANEL KANAN: PREVIEW AREA */}
        <div style={{ flex: 1, padding: '20px', backgroundColor: viewMode === 'spreadsheet' ? '#fff' : '#000', overflow: 'hidden', display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '10px' }}>
            <span style={{ fontSize: '10px', color: viewMode === 'spreadsheet' ? '#217346' : '#10b981', fontWeight: 'bold' }}>
              {viewMode === 'spreadsheet' ? 'EXCEL SPREADSHEET CANVAS' : 'DYNAMIC NEON TABLE'}
            </span>
            <span style={{ fontSize: '10px', color: '#94a3b8' }}>{selectedFields.length} COLUMNS ACTIVE</span>
          </div>

          <div className="custom-scroll" style={{ flex: 1, overflow: 'auto', border: viewMode === 'spreadsheet' ? '1px solid #ccc' : '1px solid rgba(255,255,255,0.05)' }}>
            
            {viewMode === 'spreadsheet' ? (
              /* --- SPREADSHEET VIEW --- */
              <table style={{ borderCollapse: 'separate', borderSpacing: 0, width: 'max-content', tableLayout: 'fixed', fontFamily: 'Calibri, sans-serif', backgroundColor: '#fff' }}>
                <colgroup>
                  <col style={{ width: '50px' }} />
                  {selectedFields.map((f, i) => (
                    <col key={`col-${i}`} style={{ width: '150px' }} />
                  ))}
                </colgroup>
                <thead>
                  <tr style={{ backgroundColor: '#f3f3f3' }}>
                    <th style={{ border: '1px solid #ccc', backgroundColor: '#e2e8f0', position: 'sticky', top: 0 }}></th>
                    {selectedFields.map((f, i) => (
                      <th 
                        key={f} 
                        draggable
                        onDragStart={() => setDraggedFieldIdx(i)}
                        onDragOver={(e) => { e.preventDefault(); setDragOverIdx(i); }}
                        onDragLeave={() => setDragOverIdx(null)}
                        onDrop={() => {
                          const updated = [...selectedFields];
                          const moved = updated.splice(draggedFieldIdx, 1)[0];
                          updated.splice(i, 0, moved);
                          setSelectedFields(updated);
                          setDragOverIdx(null);
                        }}
                        style={{ 
                          border: '1px solid #ccc',
                          borderLeft: dragOverIdx === i ? '4px solid #22d3ee' : '1px solid #ccc',
                          padding: '8px', backgroundColor: '#f8fafc', cursor: 'grab', textAlign: 'center',
                          resize: 'horizontal', overflow: 'auto', position: 'sticky', top: 0, zIndex: 10
                        }}
                      >
                        <div style={{ color: '#94a3b8', fontSize: '9px', fontWeight: 'normal' }}>{String.fromCharCode(65 + i)}</div>
                        <div style={{ color: '#334155', fontSize: '11px', fontWeight: '800', textTransform: 'uppercase' }}>{f.replace(/_/g, ' ')}</div>
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {dbHistory.length > 0 ? dbHistory.map((r, idx) => (
                    <tr key={idx}>
                      <td style={{ border: '1px solid #ccc', backgroundColor: '#f3f3f3', textAlign: 'center', fontSize: '11px', color: '#333' }}>{idx + 1}</td>
                      {selectedFields.map((f) => (
                        <td key={f} style={{ border: '1px solid #eee', padding: '10px', fontSize: '12px', color: '#000', textAlign: 'center', whiteSpace: 'normal', wordBreak: 'break-word' }}>
                          {r[f] || '-'}
                        </td>
                      ))}
                    </tr>
                  )) : (
                    <tr><td colSpan={selectedFields.length + 1} style={{ textAlign: 'center', padding: '20px', color: '#999' }}>No data available</td></tr>
                  )}
                </tbody>
              </table>
            ) : (
              /* --- DYNAMIC NEON TABLE --- */
              <div style={{ minWidth: 'max-content' }}>
                <div style={{ display: 'flex', backgroundColor: 'rgba(34, 211, 238, 0.1)', borderBottom: '2px solid #22d3ee' }}>
                  {selectedFields.map((f, i) => (
                    <div 
                      key={f} draggable
                      onDragStart={() => setDraggedFieldIdx(i)}
                      onDragOver={(e) => { e.preventDefault(); setDragOverIdx(i); }}
                      onDrop={() => {
                        const updated = [...selectedFields];
                        const moved = updated.splice(draggedFieldIdx, 1)[0];
                        updated.splice(i, 0, moved);
                        setSelectedFields(updated);
                        setDragOverIdx(null);
                      }}
                      style={{ width: '180px', padding: '15px', color: '#22d3ee', fontWeight: 'bold', fontSize: '10px', cursor: 'grab', textAlign: 'center', borderLeft: dragOverIdx === i ? '4px solid #22d3ee' : 'none' }}
                    >
                      {f.replace(/_/g, ' ').toUpperCase()}
                    </div>
                  ))}
                </div>
                {dbHistory.map((row, rIdx) => (
                  <div key={rIdx} style={{ display: 'flex', borderBottom: '1px solid rgba(255,255,255,0.05)', backgroundColor: rIdx % 2 === 0 ? 'rgba(255,255,255,0.01)' : 'transparent' }}>
                    {selectedFields.map(f => (
                      <div key={f} style={{ width: '180px', padding: '12px 15px', color: '#94a3b8', fontSize: '11px', textAlign: 'center', whiteSpace: 'normal', wordBreak: 'break-word' }}>
                        {row[f] || '-'}
                      </div>
                    ))}
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      {/* FOOTER */}
      <div style={{ padding: '15px 25px', display: 'flex', justifyContent: 'flex-end', gap: '12px', background: 'rgba(0,0,0,0.3)', borderTop: '1px solid rgba(255,255,255,0.05)' }}>
         <button 
            onClick={() => setSelectedFields([
              'no_unit_sap', 'gas_station', 'date', 'jam_isi', 'hm_km_unit', 
              'consumed_qty', 'fluid_type', 'measuring_position', 
              'pit_location_id', 'header_text', 'flow_meter_value'
            ])}
            style={{ background: 'none', border: '1px solid #334155', color: '#94a3b8', padding: '8px 20px', borderRadius: '5px', fontSize: '10px', cursor: 'pointer' }}
        >
          RESET TO TEMPLATE
        </button>
         
         <button 
           onClick={handleDownloadExcel}
           style={{ background: '#217346', color: '#fff', border: 'none', padding: '8px 30px', borderRadius: '5px', fontSize: '10px', fontWeight: 'bold', cursor: 'pointer', boxShadow: '0 0 15px rgba(33, 115, 70, 0.3)' }}
         >
           <i className="fas fa-file-download" style={{ marginRight: '8px' }}></i> EXPORT SPREADSHEET
         </button>
      </div>
    </div>
  </Rnd>
)}

{/* PAN-VIEWER OVERLAY (Bingkai Minimalis Tengah) */}
{panImage && (
  <div style={{
    position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.95)', 
    zIndex: 200, display: 'flex', justifyContent: 'center', alignItems: 'center'
  }}>
    <div style={{
      width: '850px', height: '650px', backgroundColor: '#111', borderRadius: '15px',
      position: 'relative', overflow: 'hidden', border: '1px solid rgba(255,255,255,0.2)',
    }}>
      {/* Header */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, padding: '15px 20px', background: 'linear-gradient(to bottom, rgba(0,0,0,0.8), transparent)', zIndex: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '12px', color: '#38bdf8', fontWeight: 'bold' }}>DRAG PHOTO TO INSPECT WATERMARK</span>
        <button onClick={() => setPanImage(null)} style={{ background: '#ef4444', border: 'none', color: '#fff', width: '30px', height: '30px', borderRadius: '50%', cursor: 'pointer' }}>
          <i className="fas fa-times"></i>
        </button>
      </div>

      {/* DRAG AREA */}
      <div 
        ref={dragRef}
        onMouseDown={handleMouseDown}
        onMouseLeave={() => setIsDragging(false)}
        onMouseUp={() => setIsDragging(false)}
        onMouseMove={handleMouseMove}
        className="no-scrollbar"
        style={{ 
          width: '100%', height: '100%', overflow: 'hidden', // Hidden agar scrollbar hilang
          cursor: isDragging ? 'grabbing' : 'grab' 
        }}
      >
        <img 
          src={panImage} 
          draggable="false" // Agar drag bawaan browser tidak ganggu
          style={{ 
            width: '200%', // Perbesar agar bisa di-drag
            display: 'block',
            userSelect: 'none'
          }} 
        />
      </div>
    </div>
  </div>
)}

{/* Render di luar FuelTable, sejajar dengan Modal/Box Zoom */}
{zoomImage && sourceRect && (
  <svg 
    style={{
      position: 'fixed',
      top: 0,
      left: 0,
      width: '100vw',
      height: '100vh',
      pointerEvents: 'none',
      zIndex: 9998, // Pastikan di bawah box zoom (9999)
    }}
  >
    <defs>
      <filter id="glow">
        <feGaussianBlur stdDeviation="2" result="coloredBlur" />
        <feMerge>
          <feMergeNode in="coloredBlur" />
          <feMergeNode in="SourceGraphic" />
        </feMerge>  
      </filter>
    </defs> 
    
    <path
      className="connector-line-animated"
      d={`M ${sourceRect.left + (sourceRect.width / 2)} ${sourceRect.top + (sourceRect.height / 2)} 
          L ${zoomPosition.x + (350 / 2)} ${zoomPosition.y + 125}`}
      stroke={activeTheme.accent}
      fill="none"
      filter="url(#glow)"
      strokeLinecap="round"
    />
  </svg>
)}

{/* Elegant Mini Toast */}
{notification.show && (
  <div style={{
    position: 'fixed',
    top: '30px',
    right: '30px',
    backgroundColor: 'rgba(20, 20, 25, 0.85)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    padding: '12px 20px',
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    color: '#fff',
    fontSize: '14px',
    fontWeight: '500',
    boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
    zIndex: 9999,
    animation: 'slideInRight 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28)'
  }}>
    <i className={`fas fa-${notification.icon || 'info-circle'}`} style={{ color: '#c084fc' }}></i>
    <span>{notification.message}</span>
  </div>
)}
</div>
  );
}

export default App;