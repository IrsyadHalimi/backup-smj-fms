import React, { useState, useEffect } from 'react';
import { Rnd } from 'react-rnd';
import { FIELD_LABELS } from '../../helpers/export/exportConstants';

export const ExportStudio = ({
  isOpen, setIsOpen, isMaximized, setIsMaximized,
  viewMode, setViewMode, selectedFields, setSelectedFields,
  dbHistory, handleDownloadExcel, AVAILABLE_FIELDS, DEFAULT_ACTIVE_FIELDS, activeTheme
}) => {
  if (!isOpen) return null;

  // State untuk manajemen drag header kolom
  const [draggedHeaderIdx, setDraggedHeaderIdx] = useState(null);
  
  // State Dinamis untuk Mengatur Lebar Kolom (Resizing) - Default 145px per kolom
  const [columnWidths, setColumnWidths] = useState({});

  // Inisialisasi lebar kolom default saat komponen dimuat
  useEffect(() => {
    const initialWidths = {};
    AVAILABLE_FIELDS.forEach(field => {
      initialWidths[field] = 145; // lebar default dalam pixel
    });
    setColumnWidths(initialWidths);
  }, [AVAILABLE_FIELDS]);

  const activeModeStyle = { background: '#22d3ee', color: '#000', border: 'none', padding: '6px 16px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold', cursor: 'pointer' };
  const inactiveModeStyle = { background: 'rgba(255,255,255,0.05)', color: '#64748b', border: 'none', padding: '6px 16px', borderRadius: '4px', fontSize: '11px', cursor: 'pointer' };

  // Handler untuk mengubah urutan letak kolom (Drag & Drop)
  const executeReorder = (fromIdx, toIdx) => {
    const updated = [...selectedFields];
    const movedItem = updated.splice(fromIdx, 1)[0];
    updated.splice(toIdx, 0, movedItem);
    setSelectedFields(updated);
  };

  // Tombol Reset kembali ke tatanan bawaan pabrik / SAP standar
  const handleResetToDefault = () => {
    setSelectedFields([...DEFAULT_ACTIVE_FIELDS]);
    const resetWidths = {};
    AVAILABLE_FIELDS.forEach(field => {
      resetWidths[field] = 145;
    });
    setColumnWidths(resetWidths);
  };

  // Handler interaksi mouse untuk resize lebar kolom secara manual
  const handleResizeStart = (e, field) => {
    e.preventDefault();
    e.stopPropagation();
    
    const startX = e.clientX;
    const startWidth = columnWidths[field] || 145;

    const handleMouseMove = (moveEvent) => {
      const currentX = moveEvent.clientX;
      const newWidth = Math.max(90, startWidth + (currentX - startX)); 
      setColumnWidths(prev => ({
        ...prev,
        [field]: newWidth
      }));
    };

    const handleMouseUp = () => {
      document.removeEventListener('mousemove', handleMouseMove);
      document.removeEventListener('mouseup', handleMouseUp);
    };

    document.addEventListener('mousemove', handleMouseMove);
    document.addEventListener('mouseup', handleMouseUp);
  };

  // Format tanggal saat ini untuk Judul Preview Excel (DD.MM.YYYY)
  const getFormattedCurrentDate = (dateSource) => {
    const d = dateSource ? new Date(dateSource) : new Date();
    if (isNaN(d.getTime())) return dateSource;
    const day = String(d.getDate()).padStart(2, '0');
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const year = d.getFullYear();
    return `${day}.${month}.${year}`;
  };

  // Format jam ke format HH:MM:SS (Hanya digunakan sebagai fallback jika jam_isi benar-benar kosong)
  const getFormattedCurrentTime = (dateSource) => {
    const d = dateSource ? new Date(dateSource) : new Date();
    if (isNaN(d.getTime())) return "11:19:00";
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    return `${hours}:${minutes}:${seconds}`;
  };

  // =========================================================
  // LOGIKA AMAN: AMBIL SELURUH DATA YANG DIKIRIM OLEH APP.JSX
  // =========================================================
  const finalPreviewRows = dbHistory && Array.isArray(dbHistory.data) 
  ? dbHistory.data 
  : (Array.isArray(dbHistory) ? dbHistory : []);

  // Menghitung total akumulasi lebar seluruh tabel preview
  const totalTableWidth = selectedFields.reduce((acc, f) => acc + (columnWidths[f] || 145), 0) + 50;

  console.log("=== DEBUG EXPORT STUDIO ===");
  console.log("1. Data Mentah Masuk (dbHistory):", dbHistory);
  console.log("2. Data Siap Render (finalPreviewRows):", finalPreviewRows);
  console.log("3. Kolom Aktif (selectedFields):", selectedFields);

  return (
    <Rnd
      size={isMaximized ? { width: '100vw', height: '100vh' } : { width: 1200, height: 780 }}
      position={isMaximized ? { x: 0, y: 0 } : { x: 'calc(50vw - 600px)', y: 'calc(50vh - 390px)' }}
      disableDragging={isMaximized}
      dragHandleClassName="modal-drag-handle"
      style={{ zIndex: 9999, position: 'fixed' }}
    >
      <div style={studioContainerStyle(isMaximized, activeTheme)}>
        
        {/* HEADER TOP BAR */}
        <div className="modal-drag-handle" style={{ ...headerStyle, backgroundColor: activeTheme.card, borderBottom: `1px solid ${activeTheme.border}` }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '20px' }}>
            <h2 style={{ margin: 0, color: '#22d3ee', fontSize: '13px', fontWeight: '800', letterSpacing: '1px' }}>
              <i className="fas fa-file-export" style={{ marginRight: '8px' }}></i> EXPORT STUDIO (SAP ENGINE)
            </h2>
            <div style={{ display: 'flex', gap: '4px', background: '#000', padding: '3px', borderRadius: '6px' }}>
              <button onClick={() => setViewMode('spreadsheet')} style={viewMode === 'spreadsheet' ? activeModeStyle : inactiveModeStyle}>EXCEL VIEW</button>
              <button onClick={() => setViewMode('table')} style={viewMode === 'table' ? activeModeStyle : inactiveModeStyle}>NEON TABLE</button>
            </div>
          </div>
          
          <div style={{ display: 'flex', gap: '15px', alignItems: 'center' }}>
            <button onClick={handleResetToDefault} style={resetBtnStyle} title="Kembalikan Susunan Kolom Default SAP">
              <i className="fas fa-undo-alt"></i> RESET DEFAULT
            </button>
            <button onClick={() => setIsMaximized(!isMaximized)} style={iconBtnStyle}><i className={`fas ${isMaximized ? 'fa-compress' : 'fa-expand'}`}></i></button>
            <button onClick={() => setIsOpen(false)} style={{ ...iconBtnStyle, color: '#ef4444', fontSize: '20px' }}>&times;</button>
          </div>
        </div>

        {/* WORKSPACE AREA */}
        <div style={{ display: 'flex', flex: 1, overflow: 'hidden' }}>
          
          {/* SIDEBAR PANEL KIRI */}
          <aside style={{ ...sidebarStyle, backgroundColor: '#090f1e', borderRight: `1px solid ${activeTheme.border}` }}>
            <div style={labelStyle}>KOLOM AKTIF TER-URUT ({selectedFields.length})</div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '6px', overflowY: 'auto', flex: 1, paddingRight: '4px' }} className="no-scrollbar">
              {selectedFields.map((f, i) => (
                <div 
                  key={f} draggable 
                  onDragStart={(e) => e.dataTransfer.setData("draggedIdx", i)}
                  onDrop={(e) => {
                    const dragIdx = parseInt(e.dataTransfer.getData("draggedIdx"), 10);
                    executeReorder(dragIdx, i);
                  }}
                  onDragOver={(e) => e.preventDefault()}
                  style={{ ...fieldCardStyle, backgroundColor: activeTheme.card, border: `1px solid ${activeTheme.border}` }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#cbd5e1' }}>
                    <i className="fas fa-grip-vertical" style={{ color: '#475569' }}></i>
                    <span style={{ fontSize: '11px', fontWeight: '600' }}>{FIELD_LABELS[f] || f.toUpperCase()}</span>
                  </div>
                  <i className="fas fa-minus-circle" onClick={() => setSelectedFields(selectedFields.filter(x => x !== f))} style={{ cursor: 'pointer', color: '#ef4444', fontSize: '13px' }}></i>
                </div>
              ))}
            </div>

            {/* SEKTOR HIDDEN FIELDS */}
            <div style={{ borderTop: `1px solid ${activeTheme.border}`, paddingTop: '15px', marginTop: '15px' }}>
              <div style={{ ...labelStyle, color: '#94a3b8', fontSize: '10px' }}>HIDDEN FIELDS (KLIK UNTUK MENAMBAH)</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', overflowY: 'auto', maxHeight: '180px' }} className="no-scrollbar">
                {AVAILABLE_FIELDS.filter(x => !selectedFields.includes(x)).map(f => (
                  <button
                    key={f}
                    onClick={() => setSelectedFields([...selectedFields, f])}
                    style={hiddenFieldBadgeStyle}
                  >
                    + {FIELD_LABELS[f]}
                  </button>
                ))}
              </div>
            </div>
          </aside>

          {/* AREA PREVIEW KANAN */}
          <main style={{ flex: 1, backgroundColor: viewMode === 'spreadsheet' ? '#cbd5e1' : '#030712', padding: '20px', overflow: 'auto' }}>
            <div style={{ width: `${totalTableWidth}px`, transition: 'width 0.1s ease' }}>
              
              {viewMode === 'spreadsheet' ? (
                /* 1. EXCEL VIEW SCHEME */
                <div style={{ backgroundColor: '#fff', border: '1px solid #94a3b8', fontFamily: 'Arial, sans-serif', fontSize: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.15)' }}>
                  
                  <div style={{ background: '#107c41', color: '#fff', padding: '8px 14px', fontWeight: 'bold', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <i className="fas fa-file-excel" style={{ fontSize: '15px', color: '#fff' }}></i>
                    <span>FMS SAP Export_{getFormattedCurrentDate()} (.xlsx) - Preview ({finalPreviewRows.length} Rows)</span>
                  </div>

                  <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'fixed' }}>
                    <thead>
                      <tr style={{ background: '#f1f5f9' }}>
                        <th style={{ ...excelTh, width: '40px', textAlign: 'center' }}></th>
                        {selectedFields.map((f, index) => (
                          <th 
                            key={f} 
                            style={{ 
                              ...excelTh, 
                              width: `${columnWidths[f] || 145}px`, 
                              backgroundColor: draggedHeaderIdx === index ? '#bae6fd' : '#f1f5f9',
                              position: 'relative'
                            }}
                            draggable
                            onDragStart={() => setDraggedHeaderIdx(index)}
                            onDragOver={(e) => e.preventDefault()}
                            onDrop={() => {
                              if (draggedHeaderIdx !== null) {
                                executeReorder(draggedHeaderIdx, index);
                                setDraggedHeaderIdx(null);
                              }
                            }}
                          >
                            <span style={{ display: 'block', paddingRight: '10px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' }}>
                              {FIELD_LABELS[f] || f.toUpperCase()}
                            </span>
                            <div 
                              onMouseDown={(e) => handleResizeStart(e, f)}
                              style={columnResizerLineStyle}
                              title="Tarik untuk atur lebar"
                            />
                          </th>
                        ))}
                      </tr>
                    </thead>
                    <tbody>
                      {finalPreviewRows.length === 0 ? (
                        <tr>
                          <td colSpan={selectedFields.length + 1} style={{ ...excelTd, textAlign: 'center', padding: '20px', color: '#94a3b8' }}>
                            Tidak ada data terfilter yang cocok untuk ditampilkan.
                          </td>
                        </tr>
                      ) : (
                        finalPreviewRows.map((row, idx) => (
                          <tr key={idx}>
                            <td style={{ ...excelTd, background: '#f1f5f9', fontWeight: 'bold', textAlign: 'center', color: '#64748b' }}>{idx + 1}</td>
                            {selectedFields.map(f => {
                              let cellVal = row[f];
                              
                              // Sinkronisasi Parsing Logika Kolom Sesuai Aturan Pertambangan
                              if (f === 'gas_station') cellVal = row.gas_station || 'GW01';
                              else if (f === 'fluid_type') cellVal = row.fluid_type || 'FUEL-DIESEL';
                              else if (f === 'measuring_position') cellVal = row.measuring_position || 'FUEL';
                              else if (f === 'header_text') cellVal = row.header_text || 'FUEL_TRX';
                              else if (f === 'consumed_qty') {
                                const rawQty = Number(row.consumed_qty || row.qty_value || 0);
                                cellVal = rawQty > 0 ? `-${rawQty}` : String(rawQty);
                              }
                              else if (f === 'date') cellVal = getFormattedCurrentDate(row.date || row.created_at);
                              
                              // PERBAIKAN UTAMA: Ambil nilai langsung jam_isi dari backend terlebih dahulu
                              else if (f === 'jam_isi') cellVal = row.jam_isi || getFormattedCurrentTime(row.created_at || row.time);
                              
                              else if (f === 'hm_km') cellVal = row.hm_km_unit || row.hm_km || '-';
                              
                              // 💡 MUTLAK MENGGUNAKAN LOCATION_ID: pit_location_id diganti nilainya dengan properti location_id
                              else if (f === 'pit_location_id') cellVal = row.location_id || row.gas_station || '-';
                              
                              else if (f === 'flow_meter_value') cellVal = row.flow_meter_value || '-'; 
                              
                              return <td key={f} style={excelTd}>{cellVal !== undefined && cellVal !== null ? String(cellVal) : '-'}</td>;
                            })}
                          </tr>
                        ))
                      )}
                    </tbody>
                  </table>
                </div>
              ) : (
                /* 2. NEON TABLE SCHEME */
                <table style={{ width: '100%', borderCollapse: 'collapse', tableLayout: 'fixed' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid #22d3ee', backgroundColor: 'rgba(15,23,42,0.8)' }}>
                      {selectedFields.map((f, index) => (
                        <th 
                          key={f} 
                          style={{ 
                            width: `${columnWidths[f] || 145}px`, 
                            color: '#22d3ee', 
                            padding: '12px 8px', 
                            textAlign: 'left', 
                            fontSize: '11px', 
                            fontWeight: '800', 
                            backgroundColor: draggedHeaderIdx === index ? 'rgba(34,211,238,0.2)' : 'transparent', 
                            position: 'relative',
                            userSelect: 'none'
                          }}
                          className="neon-drag-header"
                          draggable
                          onDragStart={() => setDraggedHeaderIdx(index)}
                          onDragOver={(e) => e.preventDefault()}
                          onDrop={() => {
                            if (draggedHeaderIdx !== null) {
                              executeReorder(draggedHeaderIdx, index);
                              setDraggedHeaderIdx(null);
                            }
                          }}
                        >
                          <div style={{ display: 'flex', alignItems: 'center', gap: '4px', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', paddingRight: '8px' }}>
                            <i className="fas fa-arrows-alt-h" style={{ opacity: 0.4, fontSize: '9px' }}></i>
                            {FIELD_LABELS[f] ? FIELD_LABELS[f].toUpperCase() : f.toUpperCase()}
                          </div>
                          <div 
                            onMouseDown={(e) => handleResizeStart(e, f)}
                            style={columnResizerLineStyle}
                            title="Tarik untuk atur lebar"
                          />
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {finalPreviewRows.length === 0 ? (
                      <tr>
                        <td colSpan={selectedFields.length} style={{ padding: '24px', color: '#64748b', fontSize: '12px', textAlign:'center' }}>
                          Tidak ada data terfilter.
                        </td>
                      </tr>
                    ) : (
                      finalPreviewRows.map((row, idx) => (
                        <tr key={idx} style={{ borderBottom: '1px solid rgba(255,255,255,0.04)', background: idx % 2 === 0 ? 'rgba(30,41,59,0.15)' : 'transparent' }}>
                          {selectedFields.map(f => {
                            let cellVal = row[f];
                            if (f === 'gas_station') cellVal = row.gas_station || 'GW01';
                            else if (f === 'fluid_type') cellVal = row.fluid_type || 'FUEL-DIESEL';
                            else if (f === 'measuring_position') cellVal = row.measuring_position || 'FUEL';
                            else if (f === 'header_text') cellVal = row.header_text || 'FUEL_TRX';
                            else if (f === 'consumed_qty') {
                              const rawQty = Number(row.consumed_qty || row.qty_value || 0);
                              cellVal = rawQty > 0 ? `-${rawQty}` : String(rawQty);
                            }
                            else if (f === 'date') cellVal = getFormattedCurrentDate(row.date || row.created_at);
                            
                            // PERBAIKAN UTAMA: Ambil nilai langsung jam_isi dari backend terlebih dahulu
                            else if (f === 'jam_isi') cellVal = row.jam_isi || getFormattedCurrentTime(row.created_at || row.time);
                            
                            else if (f === 'hm_km') cellVal = row.hm_km_unit || row.hm_km || '-';
                            
                            // 💡 MUTLAK MENGGUNAKAN LOCATION_ID: pit_location_id diganti nilainya dengan properti location_id
                            else if (f === 'pit_location_id') cellVal = row.location_id || row.gas_station || '-';
                            
                            else if (f === 'flow_meter_value') cellVal = row.flow_meter_value || '-';
                            
                            return (
                              <td key={f} style={{ padding: '12px 8px', color: '#e2e8f0', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap', fontSize: '12px' }}>
                                {cellVal !== undefined && cellVal !== null ? String(cellVal) : '-'}
                              </td>
                            );
                          })}
                        </tr>
                      ))
                    )}
                  </tbody>
                </table>
              )}

            </div>
          </main>
        </div>

        {/* FOOTER BAR */}
        <footer style={{ ...footerStyle, backgroundColor: activeTheme.card, borderTop: `1px solid ${activeTheme.border}` }}>
           <button onClick={handleDownloadExcel} style={downloadBtnStyle}>
              <i className="fas fa-file-excel" style={{ marginRight: '8px' }}></i> GENERATE REPORT FOR SAP (.XLSX)
           </button>
        </footer>
      </div>
    </Rnd>
  );
};

const studioContainerStyle = (max, theme) => ({ width: '100%', height: '100%', backgroundColor: theme.bg, borderRadius: max ? '0' : '12px', display: 'flex', flexDirection: 'column', overflow: 'hidden', boxShadow: '0 20px 50px rgba(0,0,0,0.6)' });
const headerStyle = { padding: '12px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'move' };
const sidebarStyle = { width: '270px', padding: '15px', display: 'flex', flexDirection: 'column', overflow: 'hidden' };
const fieldCardStyle = { padding: '10px 12px', borderRadius: '6px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', userSelect: 'none', cursor: 'grab' };
const footerStyle = { padding: '12px 20px', display: 'flex', justifyContent: 'flex-end' };
const labelStyle = { fontSize: '10px', color: '#475569', marginBottom: '10px', fontWeight: 'bold', letterSpacing: '0.5px' };
const iconBtnStyle = { background: 'none', border: 'none', color: '#64748b', cursor: 'pointer', fontSize: '15px' };
const resetBtnStyle = { background: 'rgba(239, 68, 68, 0.1)', color: '#f87171', border: '1px solid rgba(239, 68, 68, 0.2)', padding: '5px 12px', borderRadius: '4px', fontSize: '11px', fontWeight: 'bold', cursor: 'pointer', display: 'flex', alignItems: 'center', gap: '6px' };
const downloadBtnStyle = { background: '#10b981', color: '#fff', padding: '10px 24px', borderRadius: '6px', border: 'none', fontWeight: 'bold', cursor: 'pointer', fontSize: '12px' };
const hiddenFieldBadgeStyle = { background: '#1e293b', color: '#94a3b8', border: '1px solid #334155', padding: '6px 10px', borderRadius: '4px', fontSize: '11px', fontWeight: '500', cursor: 'grab', transition: 'all 0.15s' };
const excelTh = { border: '1px solid #cbd5e1', padding: '8px 6px', background: '#f1f5f9', textAlign: 'left', fontWeight: 'bold', color: '#475569', fontSize: '11px', cursor: 'grab', userSelect: 'none' };
const excelTd = { border: '1px solid #e2e8f0', padding: '8px 6px', color: '#334155', textOverflow: 'ellipsis', overflow: 'hidden', whiteSpace: 'nowrap' };
const columnResizerLineStyle = { position: 'absolute', right: 0, top: 0, bottom: 0, width: '6px', cursor: 'col-resize', zIndex: 10, userSelect: 'none' };

export default ExportStudio;