import React, {
  useEffect,
  useMemo,
  useState,
  useRef
} from "react";

import SortIcon from "./SortIcon";
import OCRStatusLabel from "./OCRStatusLabel";

import {
  thStyle,
  tdStyle,
  statusBadge
} from "./tableStyles";

import ScreeningStatusBadge from './ScreeningStatusBadge';

const FuelRecapTable = ({ 
  data = [], 
  title, 
  tableType, 
  isLoading = false,
  isExpanded, 
  onToggle, 
  onToggleState, 
  viewState, 
  activeTheme, 
  isDarkMode, 
  getImageUrl, 
  handleImageZoom, 
  setZoomImage,
  zoomImage, 
  handleOpenValidation, 
  handleReScreening, 
  formatIndoDate,
  activeMenuId, 
  setActiveMenuId, 
  menuButtonStyle,
  onDataFiltered,

  // Terima filter dari Parent
  searchTerm: parentSearchTerm,
  filterDate: parentFilterDate
}) => {
  
  const [sortConfig, setSortConfig] = useState({ key: null, direction: 'asc' });
  const [globalSearch, setGlobalSearch] = useState("");
  const [selectedDate, setSelectedDate] = useState(""); 
  const [showInitialFlow, setShowInitialFlow] = useState(true); 
  const [activeZoomId, setActiveZoomId] = useState(null);
  const [tableProgress, setTableProgress] = useState(0);
  const tableRef = useRef(null);

  const isAnalytics = tableType === 'AI_ANALYTICS';
  const isAI = tableType === 'AI_WORKBENCH' || tableType === 'AI_ANALYTICS';
  const isVerified = tableType === 'VERIFIED';
  
  // SINKRONISASI FILTER PARENT
  useEffect(() => {
    if (parentSearchTerm !== undefined) {
      setGlobalSearch(parentSearchTerm);
    }
  }, [parentSearchTerm]);

  useEffect(() => {
    if (parentFilterDate !== undefined) {
      setSelectedDate(parentFilterDate);
    }
  }, [parentFilterDate]);

  const meta = useMemo(() => {
    switch(tableType) {
      case 'VERIFIED': 
        return { label: 'DISPATCHED LOGS', icon: 'fa-file-export', color: '#10b981' };
      case 'AI_WORKBENCH': 
        return { label: 'FUEL RECAP', icon: 'fa-list', color: '#c084fc' };
      case 'QUEUE': 
        return { label: 'PENDING VALIDATION', icon: 'fa-clock-rotate-left', color: '#f59e0b' };
      default: 
        return { label: title, icon: 'fa-bolt', color: activeTheme.accent };
    }
  }, [tableType, title, activeTheme.accent]);

  const dynamicHeight = useMemo(() => {
    if (!isExpanded) return '52px'; 
    return '100%'; 
  }, [isExpanded]);

  const handleHeaderClick = (e) => {
    if (e.target.closest('input') || e.target.closest('button') || e.target.closest('.action-menu-container') || e.target.closest('.filter-wrapper') || e.target.closest('.toggle-container')) {
      return;
    }
    if (onToggleState) onToggleState(); 
    else if (onToggle) onToggle(); 
  };

  const handleSort = (key) => {
    let direction = 'asc';
    if (sortConfig.key === key && sortConfig.direction === 'asc') {
      direction = 'desc';
    }
    setSortConfig({ key, direction });
  };

  // SINKRONISASI CLOSE ZOOM
  useEffect(() => {
    if (!zoomImage) setActiveZoomId(null);
  }, [zoomImage]);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (!event.target.classList.contains('zoomable-image')) {
        if (setZoomImage) setZoomImage(null);
        setActiveZoomId(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [setZoomImage]);

  // DATA PROCESSING ENGINE
  const processedData = useMemo(() => {
    const safeData = data || []; 

    let filtered = safeData.filter(r => {
      const status = r.screening_status;
      
      // if (tableType === 'QUEUE') {
      //   return !status || status === '' || status === 'Pending' || status === 'QUEUE';
      // }
      // if (tableType === 'AI_WORKBENCH') {
      //   return ['Mismatch', 'Flow Match', 'HM Match', 'Failed', 'Error AI', 'Processing', 'Screening'].includes(status);
      // }
      // if (tableType === 'VERIFIED') {
        return status === 'Verified' || status === 'Downloaded';
      // }
      // return true;
    });

    if (isVerified && !showInitialFlow) {
      filtered = filtered.filter(item => {  
        const sapId = String(item.kode_unit || '').toUpperCase();
        return !sapId.includes('INITIAL_FLOW');
      });
    }

    if (selectedDate) {
      filtered = filtered.filter(item => item.date === selectedDate);
    }

    if (globalSearch) {
      const query = globalSearch.toLowerCase();
      filtered = filtered.filter(item => 
        String(item.location_id || '').toLowerCase().includes(query) ||
        String(item.tech_id || '').toLowerCase().includes(query) ||
        String(item.kode_unit || '').toLowerCase().includes(query)
      );
    }

    if (sortConfig.key) {
      filtered.sort((a, b) => {
        let valA = a[sortConfig.key] ?? '';
        let valB = b[sortConfig.key] ?? '';
        if (valA < valB) return sortConfig.direction === 'asc' ? -1 : 1;
        if (valA > valB) return sortConfig.direction === 'asc' ? 1 : -1;
        return 0;
      });
    }

    return filtered; 
  }, [data, tableType, globalSearch, selectedDate, showInitialFlow, sortConfig, isVerified]);

  useEffect(() => {
    const totalData = data?.length || 0;
    const readyData = processedData?.length || 0;

    if (totalData === 0) {
      setTableProgress(0);
      return;
    }

    const progress = Math.min(
      Math.round((readyData / totalData) * 100),
      100
    );

    setTableProgress(progress);
  }, [data, processedData]);

  const dataFingerprint = processedData.map(r => r.id || r.no).join(',');
  useEffect(() => {
    if (typeof onDataFiltered === 'function') {
      onDataFiltered(processedData);
    }
  }, [dataFingerprint, onDataFiltered]);

  const totalRecords = processedData.length;
  const totalVolume = useMemo(() => {
    return processedData.reduce((acc, curr) => {
      const val = parseFloat(curr.consumed_qty);
      return acc + (isNaN(val) ? 0 : Math.abs(val));
    }, 0);
  }, [processedData]);

  const inputBaseStyle = {
    height: '28px', padding: '4px 10px', fontSize: '11px', fontWeight: '500', borderRadius: '6px',
    border: `1px solid ${activeTheme.border}bb`, background: isDarkMode ? 'rgba(15, 23, 42, 0.6)' : '#fafafa',
    color: activeTheme.text, outline: 'none', transition: 'all 0.2s ease-in-out', boxSizing: 'border-box'
  };

  const tableHeaderThStyle = {
    ...thStyle(activeTheme),
    backgroundColor: activeTheme.tableHead,
    padding: '10px 8px',
    fontSize: '11px',
    verticalAlign: 'middle',
    border: `1px solid ${activeTheme.border}`
  };

  return (
    <div style={{ 
      display: 'flex', flexDirection: 'column', flex: 1, height: dynamicHeight, minHeight: 0, overflow: 'hidden', 
      backgroundColor: activeTheme.tableHead, borderRadius: '8px',
      border: isLoading ? `1px solid ${activeTheme.accent}` : `1px solid ${activeTheme.border}`,
      boxShadow: isLoading ? `0 0 10px ${activeTheme.accent}22` : 'none',
      transition: 'height 0.4s cubic-bezier(0.4, 0, 0.2, 1)', boxSizing: 'border-box'
    }}>
      {/* HEADER BAR */}
      <div style={{ 
        padding: '10px 14px', cursor: 'pointer', display: 'flex', justifyContent: 'space-between', alignItems: 'center', minHeight: '52px',
        borderBottom: isExpanded ? `1px solid ${activeTheme.border}44` : 'none', backgroundColor: activeTheme.tableHead, boxSizing: 'border-box'
      }} onClick={handleHeaderClick}>
        
        <div style={{ display: 'flex', flexDirection: 'column', gap: '2px', flexShrink: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <i className={`fas ${meta.icon}`} style={{ color: meta.color, fontSize: '13px' }}></i>
            <span style={{ fontSize: '12px', fontWeight: '800', letterSpacing: '0.5px', color: activeTheme.tableTitle }}>
              {meta.label}
            </span>
            {isLoading && (
              <div style={{
                display: 'flex', alignItems: 'center', gap: '6px', backgroundColor: `${activeTheme.accent}15`,
                padding: '2px 8px', borderRadius: '12px', border: `1px solid ${activeTheme.accent}44`
              }}>
                <span style={{ fontSize: '9px', fontWeight: 'bold', fontFamily: 'monospace', color: activeTheme.text }}>
                  {isAI ? "AI PROCESSING" : "LIVE SYNCING"}
                </span>
              </div>
            )}
          </div>
          {isExpanded && (
            <div style={{ display: 'flex', gap: '12px', marginLeft: '23px', opacity: 0.8 }}>
              <span style={{ fontSize: '9px', color: activeTheme.subText, display: 'flex', alignItems: 'center', gap: '4px' }}>
                <i className="fas fa-database" style={{ fontSize: '8px' }}></i> {totalRecords} Records
              </span>
              <span style={{ fontSize: '9px', color: activeTheme.subText, display: 'flex', alignItems: 'center', gap: '4px' }}>
                <i className="fas fa-gas-pump" style={{ fontSize: '8px' }}></i> {totalVolume.toLocaleString('id-ID')} Litres
              </span>
            </div>
          )}
        </div>

        <div className="filter-wrapper" style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }} onClick={(e) => e.stopPropagation()}>
          {!isExpanded && (
            <div style={{ display: 'flex', gap: '8px', padding: '4px 10px', backgroundColor: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.03)', borderRadius: '12px', marginRight: '4px', flexShrink: 0 }}>
              <span style={{ fontSize: '10px', fontWeight: 'bold', color: meta.color }}>{totalRecords} <small style={{ fontWeight: 'normal', opacity: 0.7 }}>Recs</small></span>
              <div style={{ width: '1px', backgroundColor: activeTheme.border, height: '12px', alignSelf: 'center' }} />
              <span style={{ fontSize: '10px', fontWeight: 'bold', color: activeTheme.text }}>{totalVolume.toLocaleString('id-ID')} <small style={{ fontWeight: 'normal', opacity: 0.7 }}>L</small></span>
            </div>
          )}

          {isExpanded && (  
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', minWidth: 0 }}>
              {isVerified && (
                <div className="toggle-container" style={{ display: 'flex', alignItems: 'center', gap: '6px', marginRight: '4px' }}>
                  <label style={{ display: 'flex', alignItems: 'center', cursor: 'pointer', gap: '5px', fontSize: '11px', fontWeight: '600', color: activeTheme.text }}>
                    <input type="checkbox" checked={showInitialFlow} onChange={(e) => setShowInitialFlow(e.target.checked)} style={{ cursor: 'pointer', accentColor: '#10b981', width: '14px', height: '14px' }}/>
                    <span>Init Flow</span>
                  </label>
                </div>
              )}

              {(isAI || isVerified) && (
                <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                  <input 
                    type="date" 
                    value={selectedDate} 
                    onChange={(e) => setSelectedDate(e.target.value)} 
                    style={{ ...inputBaseStyle, paddingRight: selectedDate ? '24px' : '10px', width: '125px' }}
                  />
                  {selectedDate && (
                    <i 
                      className="fas fa-times" 
                      onClick={() => setSelectedDate("")} 
                      style={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)', fontSize: '10px', color: '#ef4444', cursor: 'pointer', zIndex: 2 }}
                    />
                  )}
                </div>
              )}

              {/* SEARCH BOX */}
              <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
                <i className="fas fa-search" style={{ position: 'absolute', left: '10px', top: '50%', transform: 'translateY(-50%)', fontSize: '11px', color: isDarkMode ? 'rgba(255,255,255,0.4)' : 'rgba(0,0,0,0.4)', pointerEvents: 'none' }}></i>
                <input type="text" placeholder="Search..." value={globalSearch} onChange={(e) => setGlobalSearch(e.target.value)} style={{ ...inputBaseStyle, paddingLeft: '28px', width: isAI ? '160px' : '140px' }}/>
                {globalSearch && <i className="fas fa-times-circle" onClick={() => setGlobalSearch("")} style={{ position: 'absolute', right: '8px', top: '50%', transform: 'translateY(-50%)', fontSize: '11px', color: activeTheme.subText, cursor: 'pointer', opacity: 0.7 }}/>}
              </div>
            </div>
          )}
          
          <i className={`fas ${!isExpanded ? 'fa-caret-down' : viewState === 'full' ? 'fa-compress-alt' : 'fa-expand-arrows-alt'}`} onClick={handleHeaderClick} style={{ color: '#ef4444', fontSize: '13px', transition: 'all 0.3s ease', cursor: 'pointer', padding: '4px', flexShrink: 0 }}></i>
        </div>
      </div>

      {/* TABLE DATA CONTAINMENT */}
      <div ref={tableRef} className="custom-scroll" style={{ overflow: 'auto', flex: 1, display: isExpanded ? 'block' : 'none', backgroundColor: isDarkMode ? '#111827' : '#ffffff' }}>
        <table style={{ width: 'max-content', minWidth: '100%', borderCollapse: 'collapse' }}>
          <thead style={{ position: 'sticky', top: 0, zIndex: 15, backgroundColor: activeTheme.tableHead, boxShadow: `0 1px 0px ${activeTheme.border}` }}>
            <tr style={{ fontSize: '11px', color: activeTheme.subText, backgroundColor: activeTheme.tableHead }}>
              <th style={tableHeaderThStyle} onClick={() => handleSort('no')}>NO <SortIcon config={sortConfig} col="no"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('location_id')}>Lokasi <SortIcon config={sortConfig} col="location_id"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('date')}>Date <SortIcon config={sortConfig} col="date"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('unit')}>JENIS UNIT <SortIcon config={sortConfig} col="unit"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('no_unit_sap')}>Unit Group <SortIcon config={sortConfig} col="no_unit_sap"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('tech_id')}>Unit <SortIcon config={sortConfig} col="tech_id"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('kode_unit')}>Kode Unit <SortIcon config={sortConfig} col="kode_unit"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('jam_isi')}>Jam Pengisian <SortIcon config={sortConfig} col="jam_isi"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('last_hm')}>HM Pengisian Sebelumnya <SortIcon config={sortConfig} col="last_hm"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('hm_km_unit')}>HM Pengisian Sekarang <SortIcon config={sortConfig} col="hm_km_unit"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('hm_selisih')}>HM/KM Selisih <SortIcon config={sortConfig} col="hm_selisih"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('flow_meter_value')}>Flowmeter Reading <SortIcon config={sortConfig} col="flow_meter_value"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('consumed_qty')}>Jumlah Pengisian <SortIcon config={sortConfig} col="consumed_qty"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('ltr_jam')}>LTR/Jam <SortIcon config={sortConfig} col="ltr_jam"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('flow_meter_id')}>FM <SortIcon config={sortConfig} col="flow_meter_id"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('budget_ltr_jam')}>Budget LTR/Jam <SortIcon config={sortConfig} col="budget_ltr_jam"/></th>
              <th style={tableHeaderThStyle} onClick={() => handleSort('diviasi')}>Diviasi <SortIcon config={sortConfig} col="diviasi"/></th>
              <th style={tableHeaderThStyle}>{isAnalytics ? 'Status' : 'Action'}</th>
            </tr>
          </thead>
          <tbody style={{ fontSize: '11px', textAlign: 'center' }}>
            {processedData.map((r, i) => (
              <tr key={r.id || i} style={{ borderBottom: `1px solid ${activeTheme.border}22` }}>
                <td style={tdStyle(activeTheme)}>{r.no || i + 1}</td>
                <td style={tdStyle(activeTheme)}>{r.location_id}</td>
                <td style={tdStyle(activeTheme)}>{r.date}</td>
                <td style={tdStyle(activeTheme)}>{r.unit}</td>
                <td style={tdStyle(activeTheme)}>{r.no_unit_sap}</td>
                <td style={tdStyle(activeTheme)}>{r.tech_id}</td>
                <td style={{ ...tdStyle(activeTheme), fontWeight: 'bold' }}>{r.kode_unit}</td>
                <td style={tdStyle(activeTheme)}>{r.jam_isi}</td>
                <td style={tdStyle(activeTheme)}>{r.last_hm?.toLocaleString('id-ID') || '0'}</td>
                <td style={tdStyle(activeTheme)}>{r.hm_km_unit?.toLocaleString('id-ID') || '0'}</td>
                <td style={tdStyle(activeTheme)}>
                  {((Number(r.hm_km_unit) || 0) - (Number(r.last_hm) || 0)).toLocaleString('id-ID')}
                </td>
                <td style={tdStyle(activeTheme)}>{r.flow_meter_value?.toLocaleString('id-ID') || '0'}</td>
                <td style={{ ...tdStyle(activeTheme), color: '#48e618', fontWeight: 'bold' }}>
                  {r.consumed_qty ? Math.abs(r.consumed_qty).toLocaleString('id-ID') : '-'}
                </td>
                <td style={tdStyle(activeTheme)}>
                  {(() => {
                    const selisih = (Number(r.hm_km_unit) || 0) - (Number(r.last_hm) || 0);
                    const consumed = Math.abs(Number(r.consumed_qty) || 0);
                    
                    // Validasi: Jika selisih adalah 0 atau tidak ada consumed, tampilkan '0'
                    if (selisih === 0 || !consumed) return '0';
                    
                    const ltrJam = consumed / selisih;
                    
                    // Menggunakan toFixed(2) agar desimal tidak terlalu panjang (misal: 2.33), lalu di-format ke id-ID
                    return ltrJam.toLocaleString('id-ID', { minimumFractionDigits: 0, maximumFractionDigits: 2 });
                  })()}
                </td>
                <td style={tdStyle(activeTheme)}>{r.flow_meter_id || '-'}</td>
                <td style={tdStyle(activeTheme)}>{r.budget_ltr_jam || '0'}</td>
                <td style={{ 
                  ...tdStyle(activeTheme), 
                  color: parseFloat(r.diviasi) < 0 ? '#ef4444' : activeTheme.text 
                }}>
                  {r.diviasi || '0'}
                </td>
                
                {/* AKSI / STATUS BADGE */}
                {isAnalytics ? (
                  <td style={tdStyle(activeTheme)}>
                    <ScreeningStatusBadge status={r.screening_status} />
                  </td>
                ) : (
                  <td
                    style={{ padding: '6px', position: 'relative', overflow: 'visible' }}
                    className="action-menu-container"
                    onClick={(e) => e.stopPropagation()}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                      <button
                        onClick={(e) => {
                          e.preventDefault();
                          e.stopPropagation();
                          if (setActiveMenuId) {
                            setActiveMenuId(activeMenuId === r.id ? null : r.id);
                          }
                        }}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: activeTheme.text,
                          cursor: 'pointer',
                          padding: '4px 8px',
                          zIndex: 101
                        }}
                      >
                        <i className={`fas ${activeMenuId === r.id ? 'fa-times' : 'fa-ellipsis-v'}`} />
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
                )}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>  
  );
};

export default FuelRecapTable;