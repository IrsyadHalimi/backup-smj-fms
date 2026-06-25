import React, { useState, useEffect } from 'react';
import AnalyticChart from '../../components/charts/AnalyticChart';
import DashboardCards from '../../components/layout/DashboardCards';
import HeaderSection from '../../components/layout/HeaderSection';
import MainTableGrid from '../../components/layout/MainTableGrid';
import LeftMonitorPanel from '../../layout/LeftMonitorPanel';
import RightAnalyticsPanel from '../../layout/RightAnalyticsPanel';
import { useFuelSocket } from '../../hooks/useFuelSocket';
import { useFuelData } from '../../hooks/useFuelData';
import { BASE_URL, API_URL, WS_URL } from '../../helpers/utils/apiConfig';


const DashboardAnalytics = () => {
  // --- 1. SETTING STATE & INTEGRASI APIS/SOCKETS ---
  const [isDarkMode, setIsDarkMode] = useState(true);
  const [isExportModalOpen, setIsExportModalOpen] = useState(false);
  const [selectedFields, setSelectedFields] = useState([]);
  const [isReadyToExport, setIsReadyToExport] = useState(true);
  
  const [transactions, setTransactions] = useState([]);
  const [leftPanelOpen, setLeftPanelOpen] = useState(true);
  const [rightPanelOpen, setRightPanelOpen] = useState(true);
  const [currentLayout, setCurrentLayout] = useState('NORMAL');

  const { lastJsonMessage, readyState } = useFuelSocket({ WS_URL });

  const {
    dbHistory,
    stats,
    isLoading,
    isNewDataIn,
    isConnected,
    notification
  } = useFuelData(BASE_URL, WS_URL, API_URL);

  // --- 2. CONFIG THEMES SYSTEM ---
  const themes = {
    dark: {
      bg: '#1e1e24',
      card: '#272730',
      border: '#3a3a45',
      text: '#ffffff',
      subText: '#aaaaaa',
      accent: '#3b82f6',
    },
    light: {
      bg: '#f8fafc',
      card: '#ffffff',
      border: '#e2e8f0',
      text: '#0f172a',
      subText: '#64748b',
      accent: '#0284c7',
    }
  };
  const activeTheme = isDarkMode ? themes.dark : themes.light;

  // --- 3. FETCH INITIAL DATA (REST API HTTP) ---
  useEffect(() => {
    const fetchInitialData = async () => {
      try {
        const response = await fetch(API_URL);
        if (response.ok) {
          const result = await response.json();
          if (result && Array.isArray(result.data)) {
            setTransactions(result.data);
          } else if (Array.isArray(result)) {
            setTransactions(result);
          } else {
            setTransactions([]);
          }
        }
      } catch (error) {
        console.error("Gagal memuat data:", error);
        setTransactions([]);
      }
    };
    fetchInitialData();
  }, []);


  // --- 5. DATA CALCULATIONS & CHART SCHEMAS ---
  // Menghitung akumulasi total liter bensin secara real-time dari state transaksi
  const calculatedTotalLiters = transactions.reduce((acc, curr) => acc + (Number(curr.consumed_qty) || 0), 0);
  const uniqueUnitsCount = new Set(transactions.map(t => t.no_unit_sap)).size;

  const fuelStats = {
    totalLiters: calculatedTotalLiters || 15420,
    totalLitersOFI: 9200,
    totalLitersTFO: 6220,
    uniqueUnits: uniqueUnitsCount || 14,
    anomalies: 3,
    stations: 5,
    colors: {
      uniqueUnits: '#10b981', // Sesuai permintaan penambahan warna ke sub-komponen
      totalLiters: '#3b82f6',
      anomalies: '#ef4444'
    }
  };

  const unitCategories = ['Unit DT01', 'Unit DT02', 'Unit DT03', 'Unit EX01'];
  const unitSeries = [{ name: 'Actual Fuel Consumed', data: [420, 380, 510, 640], color: '#3498db' }];
  
  const modelCategories = ['Scania P410', 'Volvo FMX', 'Komatsu PC200'];
  const modelSeries = [{ name: 'Avg Fuel Consumption', data: [35, 42, 55], color: '#e74c3c' }];
  
  const truckCategories = ['FT-01', 'FT-02', 'FT-03'];
  const truckSeries = [{ name: 'Fuel Dispensed', data: [5000, 7200, 4100], color: '#2ecc71' }];

  // --- 6. SUB-COMPONENT REUSABLE HANDLERS ---
  const handleGridDataFiltered = (type, filteredResult) => {
    console.log(`Data terfilter dari bagian ${type}:`, filteredResult);
  };
  const handleOpenValidation = (row) => alert(`Buka Validasi untuk ID: ${row.id}`);
  const handleReScreening = (row) => alert(`Menjalankan AI Re-Screening untuk ID: ${row.id}`);
  const formatIndoDate = (dateStr) => dateStr ? new Date(dateStr).toLocaleDateString('id-ID') : '-';

  return (
    <div style={{ 
      backgroundColor: activeTheme.bg, 
      color: activeTheme.text,
      padding: '20px', 
      minHeight: '100vh',
      transition: 'all 0.3s ease',
      boxSizing: 'border-box',
      overflow: 'auto'
    }}>
      
      {/* HEADER COMPONENT */}
      <HeaderSection 
        isDarkMode={isDarkMode}
        setIsDarkMode={setIsDarkMode}
        activeTheme={activeTheme}
        isReadyToExport={isReadyToExport}
        setIsExportModalOpen={setIsExportModalOpen}
        setSelectedFields={setSelectedFields}
      />

      {/* CONTROLLER SIDEBAR TOGGLES */}
      <div style={{ display: 'flex', gap: '10px', marginBottom: '15px', marginTop: '10px' }}>
        <button onClick={() => setLeftPanelOpen(!leftPanelOpen)} style={{ padding: '6px 12px', fontSize: '11px', cursor: 'pointer', backgroundColor: activeTheme.accent, color: '#fff', border: 'none', borderRadius: '4px' }}>
          {leftPanelOpen ? '❌ HIDE MONITOR' : '🖥️ SHOW MONITOR'}
        </button>
        <button onClick={() => setRightPanelOpen(!rightPanelOpen)} style={{ padding: '6px 12px', fontSize: '11px', cursor: 'pointer', backgroundColor: activeTheme.accent, color: '#fff', border: 'none', borderRadius: '4px' }}>
          {rightPanelOpen ? '❌ HIDE AI PANELS' : '🧠 SHOW AI PANELS'}
        </button>
      </div>

      {/* MAIN CONTENT SPLIT (3-COLUMN LAYOUT STRUCTURE) */}
      <div style={{ display: 'flex', gap: '15px', marginTop: '15px' }}>
        
        {/* PANEL KIRI: LOG MONITOR */}
        <LeftMonitorPanel 
          leftPanelOpen={leftPanelOpen}
          activeTheme={activeTheme}
          isConnected={isConnected}
          dbHistory={transactions}
          isNewDataIn={isNewDataIn}
        />

        {/* AREA PANEL TENGAH (UTAMA) */}
        <div style={{ flex: 1, minWidth: 0 }}>
          
          {/* DASHBOARD CARD STATS DENGAN PROP CONFIG WARNA */}
          <DashboardCards stats={fuelStats} activeTheme={isDarkMode ? 'dark' : 'light'} />
          
          
          {/* SECTION METRICS GRAPH ANALYTICS CHARTS */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginTop: '20px' }}>
            <div style={{ background: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.02)', padding: '15px', borderRadius: '8px' }}>
              <AnalyticChart type="column" title="Actual Fuel Consumption per Unit (Liters)" categories={unitCategories} series={unitSeries} />
            </div>

            <div style={{ background: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.02)', padding: '15px', borderRadius: '8px' }}>
              <AnalyticChart type="spline" title="Average Fuel Rate per Model (L/Hour)" categories={modelCategories} series={modelSeries} yAxisTitle="Liters / Hour" />
            </div>

            <div style={{ background: isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(0,0,0,0.02)', padding: '15px', borderRadius: '8px', gridColumn: 'span 2' }}>
              <AnalyticChart type="bar" title="Total Fuel Distributed by Fuel Truck" categories={truckCategories} series={truckSeries} yAxisTitle="Total Liters" />
            </div>
          </div>

          {/* SECTION MONITORING TRANSACTION DATA TABLE */}
          <div style={{ marginTop: '20px', height: '500px', display: 'flex', flexDirection: 'column' }}>
            <MainTableGrid 
              data={dbHistory} 
              isDarkMode={isDarkMode} 
              activeTheme={activeTheme}
              currentLayout={currentLayout}
              onChangeLayout={setCurrentLayout}
              onGridDataFiltered={handleGridDataFiltered}
              formatIndoDate={formatIndoDate}
              handleOpenValidation={handleOpenValidation}
              handleReScreening={handleReScreening}
            />
          </div>


        </div>
        
        {/* PANEL KANAN: GRAPH ANALYTICS SIMULATOR */}
        <RightAnalyticsPanel 
          rightPanelOpen={rightPanelOpen}
          activeTheme={activeTheme}
          isDarkMode={isDarkMode}
          dbHistory={transactions}
        />
      </div>
      
      {/* CONTROL DIALOG MODAL EXPORT EXCEL DATA BASE */}
      {isExportModalOpen && (
        <div style={{
          position: 'fixed', top: 0, left: 0, width: '100%', height: '100%', 
          backgroundColor: 'rgba(0,0,0,0.6)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 999
        }}>
          <div style={{ background: isDarkMode ? '#2d3748' : '#fff', padding: '25px', borderRadius: '8px', color: activeTheme.text, textAlign: 'center' }}>
            <h4>SAP Data Export Configured</h4>
            <p style={{ fontSize: '12px' }}>{selectedFields.length} fields selected for Excel output.</p>
            <button onClick={() => setIsExportModalOpen(false)} style={{ padding: '6px 12px', cursor: 'pointer', border: 'none', borderRadius: '4px', backgroundColor: activeTheme.accent, color: '#fff' }}>
              Close
            </button>
          </div>
        </div>
      )}

    </div>
  );
};

export default DashboardAnalytics;