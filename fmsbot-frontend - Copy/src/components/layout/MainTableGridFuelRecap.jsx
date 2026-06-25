import React, { useMemo, useState } from 'react';
import FuelRecapTable from '../tables/FuelRecapTable';

const MainTableGridFuelRecap = ({
  dbHistory,
  data, 
  activeTheme,
  isDarkMode,
  currentLayout = 'NORMAL', 
  onChangeLayout, 
  getImageUrl,
  handleImageZoom,
  setZoomImage,
  formatIndoDate,
  handleOpenValidation,
  handleReScreening,
  activeMenuId,
  setActiveMenuId,
  menuButtonStyle,
  OCRStatusLabel,
  onGridDataFiltered,
  // Props untuk sinkronisasi penutupan elastis zoom foto
  zoomImage,
  sourceRect,
  zoomPosition
}) => {

  // KUNCI UTAMA: Buat state lokal khusus filter tanggal untuk AI Workbench saja
  const [aiWorkbenchDate, setAiWorkbenchDate] = useState("");

  const tableData = dbHistory || data || [];
  
  const safeGetImageUrl = typeof getImageUrl === 'function' ? getImageUrl : (path) => path || '';
  const safeHandleImageZoom = typeof handleImageZoom === 'function' ? handleImageZoom : () => {};

  const getGridStyles = () => {
    if (currentLayout === 'AI_FULL' || currentLayout === 'LIVE_FULL' || currentLayout === 'VERIFIED_FULL') {
      return {
        gridTemplateColumns: '1fr', 
        gridTemplateRows: '1fr',
        gap: '0px'
      };
    }
    // 💡 PERBAIKAN RESPONSIVITAS: Mengubah 'auto 1fr' menjadi '1fr 1fr' agar membagi tinggi layar secara adil (50% atas, 50% bawah)
    return {
      gridTemplateColumns: '1fr',
      gridTemplateRows: '1fr', 
      gap: '16px'
    };
  };

  // 💡 PERBAIKAN UTAMA: Mengunci referensi handler agar stabil menggunakan useMemo.
  // Ini menghentikan siklus tabrakan state ekspor di level Parent/App.jsx.
  const filterHandlers = useMemo(() => {
    return {
      QUEUE: (filteredResult) => {
        if (typeof onGridDataFiltered === 'function') onGridDataFiltered('QUEUE', filteredResult);
      },
      VERIFIED: (filteredResult) => {
        if (typeof onGridDataFiltered === 'function') onGridDataFiltered('VERIFIED', filteredResult);
      },
      AI_WORKBENCH: (filteredResult) => {
        if (typeof onGridDataFiltered === 'function') onGridDataFiltered('AI_WORKBENCH', filteredResult);
      }
    };
  }, [onGridDataFiltered]);

  return (
    <div
      style={{
        display: 'grid',
        ...getGridStyles(),
        flex: 1,
        height: '100%',     // 💡 KUNCI: Memaksa grid mengisi penuh 100% sisa ruang vertikal dari Parent
        minHeight: 0,        // 💡 KUNCI: Mencegah grid mengembang melampaui batas pembungkusnya di layar kecil
        padding: '10px 0',
        overflow: 'hidden',  // 💡 KUNCI: Mengunci layout agar tidak menimbulkan scrollbar browser luar
        boxSizing: 'border-box'
      }}
    >
      {/* =====================================================
          AI WORKBENCH TABLE (BOTTOM FULL WIDTH)
      ===================================================== */}
      <div style={{ 
        gridColumn: '1 / 3', 
        display: currentLayout === 'LIVE_FULL' || currentLayout === 'VERIFIED_FULL' ? 'none' : 'flex', 
        minHeight: 0, 
        overflow: 'hidden' 
      }}>
        <FuelRecapTable
          title="AI SCREENING WORKBENCH"
          tableType="AI_WORKBENCH"
          data={tableData}
          activeTheme={activeTheme}
          isDarkMode={isDarkMode}
          isExpanded={currentLayout !== 'LIVE_FULL' && currentLayout !== 'VERIFIED_FULL'}
          viewState={currentLayout === 'AI_FULL' ? 'full' : 'half'}
          onToggleState={() => {
            if (typeof onChangeLayout !== 'function') return;
            onChangeLayout(currentLayout === 'NORMAL' ? 'AI_FULL' : 'NORMAL'); 
          }}
          getImageUrl={safeGetImageUrl}
          handleImageZoom={safeHandleImageZoom}
          setZoomImage={setZoomImage}
          zoomImage={zoomImage} // 💡 Teruskan ke tabel AI workbench
          handleOpenValidation={handleOpenValidation}
          handleReScreening={handleReScreening}
          formatIndoDate={formatIndoDate}
          activeMenuId={activeMenuId}
          setActiveMenuId={setActiveMenuId}
          menuButtonStyle={menuButtonStyle}
          OCRStatusLabel={OCRStatusLabel}
          onDataFiltered={filterHandlers.AI_WORKBENCH}
          sourceRect={sourceRect}
          zoomPosition={zoomPosition}
          
          // PENYEMPURNAAN FILTER EKSKLUSIF: 
          // Inject state local ke prop 'filterDate' khusus untuk tabel AI ANALYTICS HUB
          filterDate={aiWorkbenchDate}
        />
      </div>
    </div>
  );
};

export default MainTableGridFuelRecap;