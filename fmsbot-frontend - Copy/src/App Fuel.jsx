// =========================================================
// CORE LIBRARIES
// =========================================================
import React, { useState, useEffect, useMemo } from 'react';

// =========================================================
// HOOKS
// =========================================================
import { useFuelSocket } from "./hooks/useFuelSocket";
import { useRealtimeFuelSync } from "./hooks/useRealtimeFuelSync";
import { useImageZoom } from "./hooks/useImageZoom";

// =========================================================
// UI COMPONENTS
// =========================================================
import HeaderSection from "./components/layout/HeaderSection";
import DashboardCards from "./components/layout/DashboardCards";
import MainTableGrid from "./components/layout/MainTableGrid";
import LeftMonitorPanel from "./layout/LeftMonitorPanel";
import RightAnalyticsPanel from "./layout/RightAnalyticsPanel";
import ValidationModal from "./components/modals/ValidationModal";
import ExportStudio from "./components/modals/ExportStudio";
import ImageZoomPreview from "./components/ui/ImageZoomPreview";
import ZoomConnector from "./components/overlay/ZoomConnector";

// =========================================================
// VALIDATION SERVICES
// =========================================================
import {
  handleOpenValidationHandler,
  handleSubmitValidationHandler,
  handleReScreeningHandler
} from "./services/validation/validationHandlers";

// =========================================================
// API SERVICES
// =========================================================
import { fetchHistoryApi } from "./services/api";

// =========================================================
// UTILITIES
// =========================================================
import { showToastHelper } from "./utils/toastHelper";

// =========================================================
// CONSTANTS & CONFIG
// =========================================================
import { themes } from "./helpers/styles/themeConfig";
import { BASE_URL, API_URL, WS_URL } from "./helpers/utils/apiConfig";

// =========================================================
// EXPORT STUDIO CONFIG
// =========================================================
import { AVAILABLE_FIELDS, DEFAULT_ACTIVE_FIELDS } from "./helpers/export/exportConstants";
import { exportToExcel } from "./helpers/export/exportExcelHelper";
import AIAnalyticsHub from "./pages/AIAnalyticsHub/AIAnalyticsHub";

// =========================================================
// MAIN APP
// =========================================================
function App() {
  // URL VIEW DETECTOR
  const urlParams = new URLSearchParams(window.location.search);
  const currentView = urlParams.get("view");
  const isAnalyticsView = currentView === "analytics";

  // MAIN DATA STATE
  const [dbHistory, setDbHistory] = useState([]);
  const [records, setRecords] = useState([]);
  const [selectedRecord, setSelectedRecord] = useState(null);

  // FILTERED TABLE DATA STATE (Untuk Sinkronisasi Grid Statis)
  const [filteredQueueData, setFilteredQueueData] = useState([]);
  const [filteredVerifiedData, setFilteredVerifiedData] = useState([]);
  const [filteredWorkbenchData, setFilteredWorkbenchData] = useState([]);

  // UI & LAYOUT STATE
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [isNewDataIn, setIsNewDataIn] = useState(false);
  const [leftPanelOpen, setLeftPanelOpen] = useState(true);
  const [rightPanelOpen, setRightPanelOpen] = useState(true);
  const [currentLayout, setCurrentLayout] = useState('NORMAL');

  // DISPATCHED LOGS VIEW STATE
  const [isDispatchedFull, setIsDispatchedFull] = useState(false);

  // FILTER STATE (Pusat Kendali Operasional)
  const [searchTerm, setSearchTerm] = useState('');
  const [filterDate, setFilterDate] = useState(''); // Menyimpan format standar "YYYY-MM-DD"
  const [showInitialFlow, setShowInitialFlow] = useState(false);

  // IMAGE ZOOM ENGINE
  const {
    zoomImage,
    setZoomImage,
    zoomPosition,
    sourceRect,
    getImageUrl,
    handleImageZoom,
    closeZoom
  } = useImageZoom(BASE_URL);

  // MODAL & MENU STATE
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [activeMenuId, setActiveMenuId] = useState(null);

  // EXPORT STATE
  const [selectedFields, setSelectedFields] = useState(DEFAULT_ACTIVE_FIELDS);
  const [viewMode, setViewMode] = useState('spreadsheet');
  const [isExportMaximized, setIsExportMaximized] = useState(false);

  // VALIDATION FORM STATE
  const [finalHM, setFinalHM] = useState('');
  const [finalFlow, setFinalFlow] = useState('');

  // NOTIFICATION STATE
  const [notification, setNotification] = useState({ show: false, message: '', icon: 'info-circle' });

  // ACTIVE THEME
  const activeTheme = isDarkMode ? themes.dark : themes.standard;

  // WEBSOCKET CONNECTION
  const { lastJsonMessage, readyState } = useFuelSocket({ WS_URL });
  const isConnected = readyState === 1;

  // REALTIME SYNCHRONIZATION WITH BACKEND POOL
  useRealtimeFuelSync({
    lastJsonMessage,
    BASE_URL,
    API_URL,
    setDbHistory,
    setRecords,
    setIsNewDataIn
  });

  // =========================================================
  // STATE LOADING & PROGRESS TUNING (LAZY STATUS ENGINE)
  // =========================================================
  const [isFirstLoad, setIsFirstLoad] = useState(true);
  const [loadingStatus, setLoadingStatus] = useState('IDLE'); // 'IDLE' | 'LOADING' | 'SLOW_NETWORK'

  // =========================================================================
  // CORE ENGINE: FETCH ALL DATA WITH SERVER-SIDE FILTER BYPASS LIMIT
  // =========================================================================
  const fetchAllData = async (targetDate = '', search = '') => {
    // 🌟 1. INIT STATE LAZY LOADING STATUS
    setLoadingStatus('LOADING');
    
    // 🌟 2. TIMER INTERUPSI JARINGAN TAMBANG (Mendeteksi RTO / Latency Tinggi)
    const networkTimer = setTimeout(() => {
      setLoadingStatus('SLOW_NETWORK');
    }, 3500); // Mengubah status menjadi SLOW_NETWORK jika server tidak merespon dalam 3.5 detik

    try {
      // 🕵️ LOG DEBUGGING BAWAAN DIPERTAHANKAN
      console.log(`=== [APP.JSX FETCH] MENARIK DATA POOL UTAMA (Tanggal: "${targetDate}", Cari: "${search}") ===`);
      
      const queryParams = {
        BASE_URL,
        API_URL,
        // 🚀 TUNING PERFORMA: Dioptimalkan ke 200 row agar query MySQL mengembalikan data kilat.
        // Data transaksi baru setelahnya akan ditangkap otomatis secara realtime oleh useRealtimeFuelSync via WebSocket.
        limit: 200,       
        page_size: 200,   
        date: targetDate || "",
        search: search || ""
      };

      const response = await fetchHistoryApi(queryParams);
      
      // 🕵️ LOG DEBUGGING BAWAAN DIPERTAHANKAN
      console.log("Response Mentah dari services/api:", response);

      let extractedData = [];
      if (response && response.results && Array.isArray(response.results)) {
        extractedData = response.results;
      } else if (response && response.data && Array.isArray(response.data)) {
        extractedData = response.data;
      } else if (Array.isArray(response)) {
        extractedData = response;
      }

      // 🕵️ LOG DEBUGGING BAWAAN DIPERTAHANKAN
      console.log(`Berhasil Mengeset Pool Data: ${extractedData.length} baris masuk ke state.`);
      setDbHistory(extractedData);
      setRecords(extractedData);
      
    } catch (err) {
      // 🕵️ LOG DEBUGGING BAWAAN DIPERTAHANKAN
      console.error("Failed fetch history:", err);
      
      // Penyelamatan cadangan notifikasi jika total loss network
      if (typeof showToast === 'function') {
        showToast("Koneksi ke backend terputus. Menggunakan local offline-cache.", "exclamation-triangle");
      }
    } finally {
      // 🌟 3. CLEAR TIMER & TERMINASI STATUS LOADING
      clearTimeout(networkTimer);
      setLoadingStatus('IDLE');
      if (typeof setIsFirstLoad === 'function') {
        setIsFirstLoad(false);
      }
    }
  };

  // Sinkronisasi Re-fetch Otomatis sewaktu operator mengubah filter atas
  useEffect(() => {
    fetchAllData(filterDate, searchTerm);
  }, [filterDate, searchTerm]);

  // =========================================================================
  // OPTIMIZED MEMO FILTER ENGINE
  // =========================================================================
  const filteredDispatchedData = useMemo(() => {
    if (!Array.isArray(dbHistory) || dbHistory.length === 0) return [];

    return dbHistory.filter(record => {
      const status = (
        record.screening_status ||
        record.validation_status ||
        record.status ||
        ""
      ).toLowerCase();

      const isValidStatus =
        status.includes('verif') || 
        status.includes('downlo') ||
        status.includes('success') ||
        status === 'success';

      if (!isValidStatus) return false;

      const searchLower = searchTerm.toLowerCase();
      const matchSearch =
        !searchTerm ||
        (record.location_id || record.pit_location_id || "").toLowerCase().includes(searchLower) ||
        (record.no_unit_sap || "").toLowerCase().includes(searchLower) ||
        (record.gas_station || "").toLowerCase().includes(searchLower);

      if (!matchSearch) return false;

      if (filterDate) {
        const recordDateSrc = record.date || record.created_at;
        if (!recordDateSrc) return false;

        let recordDateStr = "";
        if (typeof recordDateSrc === 'string' && recordDateSrc.includes('.')) {
          const parts = recordDateSrc.split('.');
          if (parts.length === 3) recordDateStr = `${parts[2]}-${parts[1]}-${parts[0]}`;
        } else {
          recordDateStr = recordDateSrc.split(' ')[0] || new Date(recordDateSrc).toISOString().split('T')[0];
        }

        if (recordDateStr !== filterDate) return false;
      }

      return true;
    });
  }, [dbHistory, searchTerm, filterDate]);

  // CLOSE ACTION MENU WHEN CLICK OUTSIDE
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (activeMenuId !== null && !event.target.closest('.action-menu-container')) {
        setActiveMenuId(null);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [activeMenuId]);

  // TOAST HELPER
  const showToast = (msg, icon = "info-circle") => {
    showToastHelper({ setNotification, msg, icon });
  };

  // VALIDATION HANDLERS
  const handleOpenValidation = (record) => {
    handleOpenValidationHandler({
      record,
      setSelectedRecord,
      setFinalHM,
      setFinalFlow,
      setIsModalOpen,
      setActiveMenuId,
      showToast
    });
  };

  const handleSubmitValidation = async () => {
    await handleSubmitValidationHandler({
      BASE_URL,
      selectedRecord,
      finalHM,
      finalFlow,
      setDbHistory,
      setRecords,
      setIsModalOpen,
      setSelectedRecord,
      setFinalHM,
      setFinalFlow,
      fetchAllData: () => fetchAllData(filterDate, searchTerm), // Mempertahankan filter tanggal aktif setelah submit
      showToast
    });
  };

  const handleReScreening = async (record) => {
    await handleReScreeningHandler({
      BASE_URL,
      API_URL,
      record,
      setRecords,
      setDbHistory,
      setActiveMenuId,
      showToast
    });
  };

  const handleGridDataFiltered = (tableType, incomingData) => {
    if (!tableType) return;
    switch (tableType) {
      case 'QUEUE': setFilteredQueueData(incomingData); break;
      case 'VERIFIED': setFilteredVerifiedData(incomingData); break;
      case 'AI_WORKBENCH': setFilteredWorkbenchData(incomingData); break;
      default: break;
    }
  };

  // DYNAMIC DASHBOARD STATS
  const stats = useMemo(() => {
    return {
      totalLiters: dbHistory.reduce((sum, row) => sum + (Number(row.consumed_qty) || 0), 0),
      uniqueUnits: new Set(dbHistory.map(row => row.no_unit_sap).filter(Boolean)).size,
      anomalies: dbHistory.filter(row => row.status === 'Anomaly' || row.is_anomaly || String(row.screening_status).toLowerCase() === 'mismatch').length,
      stations: new Set(dbHistory.map(row => row.gas_station).filter(Boolean)).size
    };
  }, [dbHistory]);

  const isReadyToExport = stats.anomalies === 0;

  // EXPORT STUDIO DATA POOL OPTIMIZATION
  const exportStudioDataPool = useMemo(() => {
    const isFilterActive = searchTerm.trim() !== '' || filterDate !== '';
    if (isFilterActive) return filteredVerifiedData; 
    if (Array.isArray(filteredVerifiedData) && filteredVerifiedData.length > 0) return filteredVerifiedData;
    if (Array.isArray(filteredDispatchedData) && filteredDispatchedData.length > 0) return filteredDispatchedData;
    
    return dbHistory.filter(r => {
      const s = String(r.screening_status || r.validation_status || r.status || "").toUpperCase();
      return s.includes("VERIF") || s.includes("SUCCESS") || s.includes("DOWNLO");
    });
  }, [searchTerm, filterDate, filteredVerifiedData, filteredDispatchedData, dbHistory]);

  // EXPORT EXECUTION HANDLER
  const handleDownloadExcelExecution = () => {
    const baseDataToExport = exportStudioDataPool || [];

    const getFormattedTimeFallback = (row) => {
      if (row.jam_isi && typeof row.jam_isi === 'string' && row.jam_isi.trim() !== '') return row.jam_isi;
      if (row.time && typeof row.time === 'string' && row.time.trim() !== '') return row.time;
      const dateSource = row.created_at || row.date;
      const d = dateSource ? new Date(dateSource) : new Date();
      if (isNaN(d.getTime())) return "00:00:00"; 
      return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
    };

    const getFormattedDateFallback = (dateSource) => {
      if (typeof dateSource === 'string' && dateSource.includes('.')) return dateSource;
      const d = dateSource ? new Date(dateSource) : new Date();
      if (isNaN(d.getTime())) return dateSource || "";
      return `${String(d.getDate()).padStart(2, '0')}.${String(d.getMonth() + 1).padStart(2, '0')}.${d.getFullYear()}`;
    };

    const sanitizedDataForExcel = baseDataToExport.map(row => {
      const cleanRow = { ...row };
      cleanRow.jam_isi = getFormattedTimeFallback(row);
      cleanRow.date = getFormattedDateFallback(row.date || row.created_at);
      cleanRow.gas_station = row.gas_station || 'GW01';
      cleanRow.fluid_type = row.fluid_type || 'FUEL-DIESEL';
      cleanRow.measuring_position = row.measuring_position || 'FUEL';
      cleanRow.header_text = row.header_text || 'FUEL_TRX';
      cleanRow.hm_km = row.hm_km_unit || row.hm_km || '-';
      cleanRow.location_id = row.location_id || row.pit_location_id || row.gas_station || 'PIT-55';
      cleanRow.flow_meter_value = row.flow_meter_value || '-';

      if (row.consumed_qty || row.qty_value) {
        cleanRow.consumed_qty = String(Math.abs(Number(row.consumed_qty || row.qty_value || 0)));
      }
      return cleanRow;
    });

    const customFileName = `FMS_SAP_Export_${new Date().toISOString().split('T')[0].replace(/-/g, '')}`;

    exportToExcel({
      records: sanitizedDataForExcel, 
      selectedFields,
      fileName: customFileName, 
      showToast,
      setDbHistory,
      setIsExportModalOpen
    });
  };

  // AI ANALYTICS REDIRECT
  if (isAnalyticsView) {
    return (
      <AIAnalyticsHub
        dbHistory={dbHistory}
        activeTheme={activeTheme}
        isDarkMode={isDarkMode}
        getImageUrl={getImageUrl}
        handleImageZoom={handleImageZoom}
        zoomImage={zoomImage}
        sourceRect={sourceRect}
        zoomPosition={zoomPosition}
        closeZoom={closeZoom}
      />
    );
  }

  return (
    <div
      style={{
        backgroundColor: activeTheme.bg,
        color: activeTheme.text,
        height: '100vh',
        width: '100vw',
        overflow: 'hidden',
        display: 'flex',
        flexDirection: 'column',
        padding: '10px',
        boxSizing: 'border-box',
        position: 'relative' // Memastikan pembungkus mendasari posisi fixed indikator
      }}
    >
      {/* 🌟 LAZY LOADING PROGRESS INDICATOR FLOATING HUD BAR */}
      {loadingStatus !== 'IDLE' && (
        <div style={{
          position: 'fixed', top: 0, left: 0, right: 0, zIndex: 99999,
          height: '4px', backgroundColor: 'rgba(15, 23, 42, 0.3)', backdropFilter: 'blur(2px)'
        }}>
          <div style={{
            height: '100%',
            width: loadingStatus === 'SLOW_NETWORK' ? '92%' : '65%',
            backgroundColor: loadingStatus === 'SLOW_NETWORK' ? '#f43f5e' : '#0ea5e9',
            transition: 'width 3s cubic-bezier(0.1, 0.8, 0.2, 1), background-color 0.4s ease',
            boxShadow: loadingStatus === 'SLOW_NETWORK' ? '0 0 12px #f43f5e' : '0 0 12px #0ea5e9'
          }} />
          <div style={{
            position: 'fixed', top: '16px', left: '50%', transform: 'translateX(-50%)',
            backgroundColor: loadingStatus === 'SLOW_NETWORK' ? 'rgba(225, 29, 72, 0.95)' : 'rgba(15, 23, 42, 0.9)',
            border: loadingStatus === 'SLOW_NETWORK' ? '1px solid #f43f5e' : '1px solid rgba(255,255,255,0.1)',
            color: '#fff', padding: '7px 16px', borderRadius: '30px', fontSize: '11.5px', fontWeight: '700',
            boxShadow: '0 10px 25px rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', gap: '10px',
            backdropFilter: 'blur(8px)', letterSpacing: '0.5px', fontFamily: 'sans-serif'
          }}>
            <i className="fas fa-sync-alt fa-spin" style={{ color: loadingStatus === 'SLOW_NETWORK' ? '#fff' : '#38bdf8' }}></i>
            <span>
              {loadingStatus === 'SLOW_NETWORK' 
                ? 'KONEKSI SITE TAMBANG LAMBAT, MENUNGGU RESPONS SERVER...' 
                : 'SINKRONISASI DATABASE FMS SEDANG BERJALAN...'}
            </span>
          </div>
        </div>
      )}

      <HeaderSection
        isDarkMode={isDarkMode}
        setIsDarkMode={setIsDarkMode}
        activeTheme={activeTheme}
        isReadyToExport={isReadyToExport}
        setIsExportModalOpen={setIsExportModalOpen}
        setSelectedFields={() => {}}
      />

      <div
        style={{
          display: 'flex',
          flex: 1,
          gap: '10px',
          minHeight: 0,
          position: 'relative',
          overflow: 'hidden'
        }}
      >
        {leftPanelOpen && (
          <LeftMonitorPanel
            leftPanelOpen={leftPanelOpen}
            activeTheme={activeTheme}
            isConnected={isConnected}
            dbHistory={dbHistory}
            isNewDataIn={isNewDataIn}
            records={records}
          />
        )}

        <div
          style={{
            flex: 1,
            display: 'flex',
            flexDirection: 'column',
            gap: '12px',
            minHeight: 0,
            position: 'relative',
            overflow: 'hidden'
          }}
        >
          {/* SLIDER CONTROL BUTTONS */}
          <button
            onClick={() => setLeftPanelOpen(!leftPanelOpen)}
            style={{
              position: 'absolute', left: '-10px', top: '50%', zIndex: 20,
              width: '20px', height: '40px', borderRadius: '10px', border: 'none',
              backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff'
            }}
          >
            {leftPanelOpen ? '◀' : '▶'}
          </button>

          <button
            onClick={() => setRightPanelOpen(!rightPanelOpen)}
            style={{
              position: 'absolute', right: '-10px', top: '50%', zIndex: 20,
              width: '20px', height: '40px', borderRadius: '10px', border: 'none',
              backgroundColor: activeTheme.accent, cursor: 'pointer', color: '#fff'
            }}
          >
            {rightPanelOpen ? '▶' : '◀'}
          </button>

          <DashboardCards stats={stats} dbHistory={dbHistory} activeTheme={activeTheme} />

          <MainTableGrid
            data={dbHistory}
            dbHistory={dbHistory}
            activeTheme={activeTheme}
            isDarkMode={isDarkMode}
            currentLayout={currentLayout}
            onChangeLayout={(layout) => {
              setCurrentLayout(layout);
              setRightPanelOpen(layout === 'NORMAL');
            }}
            handleOpenValidation={handleOpenValidation}
            handleReScreening={handleReScreening}
            activeMenuId={activeMenuId}
            setActiveMenuId={setActiveMenuId}
            isNewDataIn={isNewDataIn}
            setZoomImage={setZoomImage}
            zoomImage={zoomImage}
            handleImageZoom={handleImageZoom}
            closeZoom={closeZoom}
            getImageUrl={getImageUrl}
            setPanImage={setZoomImage}
            isDispatchedFull={isDispatchedFull}
            setIsDispatchedFull={setIsDispatchedFull}
            filteredDispatchedData={filteredDispatchedData}
            searchTerm={searchTerm}
            setSearchTerm={setSearchTerm}
            filterDate={filterDate}
            setFilterDate={setFilterDate}
            showInitialFlow={showInitialFlow}
            setShowInitialFlow={setShowInitialFlow}
            setIsExportModalOpen={setIsExportModalOpen}
            onGridDataFiltered={handleGridDataFiltered}
          />
        </div>

        {rightPanelOpen && (
          <RightAnalyticsPanel
            rightPanelOpen={rightPanelOpen}
            dbHistory={dbHistory}
            activeTheme={activeTheme}
            isDarkMode={isDarkMode}
          />
        )}
      </div>

      {/* TOAST NOTIFICATION */}
      {notification.show && (
        <div
          style={{
            position: 'fixed', top: '30px', right: '30px',
            backgroundColor: 'rgba(20,20,25,0.85)', backdropFilter: 'blur(10px)',
            border: '1px solid rgba(255,255,255,0.1)', padding: '12px 20px',
            borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '12px',
            color: '#fff', fontSize: '14px', fontWeight: '500',
            boxShadow: '0 10px 30px rgba(0,0,0,0.5)', zIndex: 9999
          }}
        >
          <i className={`fas fa-${notification.icon}`} style={{ color: '#c084fc' }}></i>
          <span>{notification.message}</span>
        </div>
      )}

      {/* OVERLAY VISUAL COMPONENTS */}
      {!isModalOpen && (
        <ZoomConnector zoomImage={zoomImage} sourceRect={sourceRect} zoomPosition={zoomPosition} activeTheme={activeTheme} />
      )}

      <ImageZoomPreview zoomImage={zoomImage} closeZoom={closeZoom} sourceRect={sourceRect} zoomPosition={zoomPosition} />

      <ValidationModal
        isOpen={isModalOpen} record={selectedRecord} onClose={() => setIsModalOpen(false)}
        activeTheme={activeTheme} finalHM={finalHM} setFinalHM={setFinalHM}
        finalFlow={finalFlow} setFinalFlow={setFinalFlow} onSubmit={handleSubmitValidation}
        getImageUrl={getImageUrl} setPanImage={setZoomImage}
      />

      <ExportStudio
        isOpen={isExportModalOpen} setIsOpen={setIsExportModalOpen} isMaximized={isExportMaximized}
        setIsMaximized={setIsExportMaximized} viewMode={viewMode} setViewMode={setViewMode}
        selectedFields={selectedFields} setSelectedFields={setSelectedFields}
        dbHistory={exportStudioDataPool} handleDownloadExcel={handleDownloadExcelExecution}
        AVAILABLE_FIELDS={AVAILABLE_FIELDS} DEFAULT_ACTIVE_FIELDS={DEFAULT_ACTIVE_FIELDS} activeTheme={activeTheme}
      />
    </div>
  );
}

export default App;