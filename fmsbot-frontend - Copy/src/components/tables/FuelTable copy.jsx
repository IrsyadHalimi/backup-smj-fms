import {
  useEffect,
  useMemo,
  useRef,
  useState
} from "react";

import SortIcon from "./SortIcon";
import OCRStatusLabel from "./OCRStatusLabel";

import {
  thStyle,
  tdStyle,
  statusBadge
} from "./tableStyles";

const FuelTable = ({ 
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
};

export default FuelTable;