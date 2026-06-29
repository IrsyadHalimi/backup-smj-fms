import React, { useState, useEffect, useMemo } from 'react';
import "../../styles/css/FuelExplorer.css";
import HeaderSection from "../../components/layout/HeaderSection";
import Header from "../../components/layout/Header";
import MetricCard from "../../components/cards/MetricCard";
import DashboardCard from "../../components/common/DashboardCard";
import FuelTrendChart from "../../components/charts/FuelTrendChart";
import FuelModelChart from "../../components/charts/FuelModelChart";
import AnalysisPanel from "../../components/analysis/AnalysisPanel";
import TableToolbar from "../../components/tables/TableToolbar";
import PerformanceTable from "../../components/tables/PerformanceTable";

import { Droplets, Clock3, Gauge, Trophy, AlertCircle, PieChart } from "lucide-react";
import { useFuelExploreStore } from "../../store/fuelExploreStore";
import { useFuelExploreData } from "../../hooks/useFuelExploreData";
import { useFuelExploreUnits } from "../../hooks/useFuelExploreUnits";
import { useFuelTrend, useModelRates, useUnits } from "../../store/fuelExploreSelectors";
import { themes } from "../../helpers/styles/themeConfig";

export default function FuelExplorer() {
  
  const [isDarkMode, setIsDarkMode] = useState(true);
  const fuelTrend = useFuelTrend();
  const modelRates = useModelRates();
  const dashboard = useFuelExploreStore(state => state.dashboard);

  const [dates, setDates] = useState({
      start:"",
      end:""
  });
  const [startDate,setStartDate] = useState("");
  const [endDate,setEndDate] = useState("");

  const activeTheme = isDarkMode ? themes.dark : themes.standard;
  const [page, setPage] = useState(1);
  const [limit, setLimit] = useState(50);

  useFuelExploreUnits(
    page,
    limit,
    startDate,
    endDate
  );
  useFuelExploreData(
    startDate,
    endDate
  );
  
  useEffect(() => {
    console.log({
        startDate,
        endDate
    });
  }, [startDate, endDate]);
  const units = useUnits();

  if (!dashboard) return <div>Loading...</div>;

  const summary = dashboard.summary;

  const sourceInsight = {
    "summary": summary,
    "fueltrend": fuelTrend,
    "modelRates": modelRates,
  };


  // KONDISI WARNA IKON BERDASARKAN STATUS IS_DARK_MODE
  // Jika True: Menggunakan spektrum neon menyala (Mission Control Vibe)
  // Jika False: Menggunakan spektrum deep kontras (Corporate Clean Vibe)
  const colors = {
    fuel: isDarkMode ? "#38bdf8" : "#0f3d91",       // Sky Blue Neon vs Deep Navy
    hours: isDarkMode ? "#a78bfa" : "#5b21b6",      // Soft Purple vs Rich Indigo
    rate: isDarkMode ? "#fbbf24" : "#92400e",       // Amber Warning vs Dark Ochre
    best: isDarkMode ? "#34d399" : "#14532d",       // Emerald Neon vs Forest Green
    efficient: isDarkMode ? "#f43f5e" : "#7f1d1d",  // Rose Red Anomaly vs Burgundy
    unit: isDarkMode ? "#60a5fa" : "#1e3a8a"        // Electric Blue vs Classic Blue
  };

  return (
    <div className={`dashboard ${isDarkMode ? "theme-dark" : "theme-light"}`}>
      <Header 
        currentStart={startDate}
        currentEnd={endDate}
        onFilterChange={(start, end) => {
          setStartDate(start);
          setEndDate(end);
        }}
      />

      {/* Container utama pengunci struktur 3 Kolom x 3 Baris */}
      <div className="dashboard-grid-layout">
        
        {/* BARIS 1: Metric Cards dengan Warna Adaptif */}
        <div className="metrics-row-container">
          <MetricCard title="TOTAL FUEL CONSUMED" value={summary?.totalFuel} unit="Liter" change={summary?.fuelChange} icon={<Droplets />} iconColor={colors.fuel} />
          <MetricCard title="TOTAL WORKING HOUR" value={summary?.workingHour} unit="Hour" change={summary?.workingHourChange} icon={<Clock3 />} iconColor={colors.hours} />
          <MetricCard title="AVG FUEL RATE" value={summary?.avgFuelRate} unit="L/H" change={summary?.avgFuelRateChange} icon={<Gauge />} iconColor={colors.rate} />
          <MetricCard title="BEST PERFORMER" value={summary?.bestModel} unit="" change={0.1} icon={<Trophy />} iconColor={colors.best} />
          <MetricCard title="LEAST EFFICIENT" value={summary?.worstModel} unit="" change={-0.1} icon={<AlertCircle />} iconColor={colors.efficient} />
          <MetricCard title="TOTAL UNIT" value={summary?.totalUnit} unit="Unit" change={0.1} icon={<PieChart />} iconColor={colors.unit} />
        </div>

        {/* BARIS 2: Grafik Trend (Kolom 1) */}
        <div className="chart-trend-container">
          <DashboardCard title="FUEL RATE TREND">
            <FuelTrendChart data={fuelTrend} isDarkMode={isDarkMode} />
          </DashboardCard>
        </div>

        {/* BARIS 2: Grafik Model (Kolom 2) */}
        <div className="chart-model-container">
          <DashboardCard title="FUEL RATE BY UNIT">
            <FuelModelChart data={modelRates} isDarkMode={isDarkMode} />
          </DashboardCard>
        </div>

        {/* BARIS 3: Tabel Utama (Mengambil Kolom 1 & Kolom 2) */}
        <div className="table-row-container">
         
            <TableToolbar isDarkMode={isDarkMode} />
            <PerformanceTable 
              isDarkMode={isDarkMode} 
              page={page}
              setPage={setPage}
              limit={limit}
              setLimit={setLimit}
            />
        </div>

        {/* KOLOM KANAN (Baris 1 s/d Baris 3): AI Analysis Panel */}
        <div className="ai-panel-sidebar">
          <AnalysisPanel mainData={sourceInsight} isDarkMode={isDarkMode} />
        </div>

      </div>
    </div>
  );
}