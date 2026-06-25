import React from 'react';

const HeaderSection = ({
  isDarkMode,
  setIsDarkMode,
  activeTheme,
  isReadyToExport,
  setIsExportModalOpen,
  setSelectedFields
}) => {
  return (
/* =========================================================
    HEADER SECTION
========================================================= */
<div
  style={{
    display: 'flex',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: '15px',
    flexShrink: 0
  }}
>
  
  {/* =====================================================
      HEADER TITLE
  ===================================================== */}
  <div style={{ textAlign: 'left' }}>
    
    <h1
      style={{
        margin: 0,
        fontSize: '18px',
        fontWeight: '400',
        color: isDarkMode
          ? activeTheme.accent
          : activeTheme.text
      }}
    >
      Real-time Transaction Hub & AI Screening
    </h1>

    {/*<p
      style={{
        margin: 0,
        fontSize: '14px',
        color: activeTheme.subText
      }}
    >
      Real-time Transaction Hub & AI Screening
    </p>*/}

  </div>

  {/* =====================================================
      HEADER ACTION BUTTONS
  ===================================================== */}
  <div
    style={{
      display: 'flex',
      gap: '8px'
    }}
  >

    {/* =================================================
        DARK MODE TOGGLE
    ================================================= */}
    <button
      className="btn-h"
      onClick={() => setIsDarkMode(!isDarkMode)}
      style={{
        padding: '8px 12px',
        borderRadius: '5px',
        border: 'none',
        backgroundColor: activeTheme.accent,
        color: '#fff',
        fontSize: '10px',
        fontWeight: 'bold',
        cursor: 'pointer'
      }}
    >
      {isDarkMode ? 'STANDARD' : 'DARK'}
    </button>

    {/* =================================================
        EXPORT BUTTON
    ================================================= */}
    <button
      className="btn-h"
      disabled={!isReadyToExport}
      onClick={() => {

        // Set default fields for data export SAP
        setSelectedFields([
          'no_unit_sap',
          'gas_station',
          'date',
          'jam_isi',
          'hm_km_unit',
          'consumed_qty',
          'fluid_type',
          'measuring_position',
          'pit_location_id',
          'header_text',
          'flow_meter_value'
        ]);

        setIsExportModalOpen(true);
      }}
      style={{
        padding: '8px 16px',
        borderRadius: '5px',
        border: 'none',

        backgroundColor: isReadyToExport
          ? '#10b981'
          : '#1e293b',

        color: isReadyToExport
          ? '#fff'
          : '#64748b',

        fontSize: '10px',
        fontWeight: 'bold',

        cursor: isReadyToExport
          ? 'pointer'
          : 'not-allowed',

        opacity: isReadyToExport ? 1 : 0.6,

        boxShadow: isReadyToExport
          ? '0 0 15px rgba(16, 185, 129, 0.4)'
          : 'none',

        display: 'flex',
        alignItems: 'center',
        gap: '8px',

        transition: 'all 0.3s ease'
      }}
    >
      <i
        className={`fas ${
          isReadyToExport
            ? 'fa-file-excel'
            : 'fa-lock'
        }`}
      ></i>

      {isReadyToExport
        ? 'EXPORT SPREADSHEET'
        : 'VALIDATION INCOMPLETE'}
    </button>

  </div>
</div>
);
};
export default HeaderSection; 