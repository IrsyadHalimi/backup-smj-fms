import "../../styles/css/FuelExplorer.css";

import Header
from "../../components/layout/Header";

import MetricCard
from "../../components/cards/MetricCard";

import DashboardCard
from "../../components/common/DashboardCard";

import FuelTrendChart
from "../../components/charts/FuelTrendChart";

import FuelModelChart
from "../../components/charts/FuelModelChart";

import AnalysisPanel
from "../../components/analysis/AnalysisPanel";

import TableToolbar
from "../../components/tables/TableToolbar";

import PerformanceTable
from "../../components/tables/PerformanceTable";

import {
  Droplets,
  Clock3,
  Gauge,
  Trophy,
  AlertCircle,
  PieChart
}
from "lucide-react";

import {
  useFuelExploreStore
}
from "../../store/fuelExploreStore";

import {
  useFuelExploreData
}
from "../../hooks/useFuelExploreData";

import {
  useFuelExploreUnits
}
from "../../hooks/useFuelExploreUnits";

import {
  useDashboardSummary,
  useFuelTrend,
  useModelRates,
  useUnits
} from "../../store/fuelExploreSelectors";


export default function FuelExplorer() {

  useFuelExploreData();

  const fuelTrend =
    useFuelTrend();

  const modelRates =
    useModelRates();

  const dashboard =
    useFuelExploreStore(
      state => state.dashboard
    );

  useFuelExploreUnits(
    1,
    50
  );
  
  const units = 
    useUnits();

  if (!dashboard)
    return <div>Loading...</div>;

  const summary =
    dashboard.summary;

  return (
    <div className="dashboard">

      <Header />

      <div className="summary-grid">

        <MetricCard
          title="TOTAL FUEL CONSUMED"
          value={summary?.totalFuel}
          unit="Liter"
          change={summary?.fuelChange}
          icon={<Droplets />}
          iconColor="#0f3d91"
        />

        <MetricCard
          title="TOTAL WORKING HOUR"
          value={summary?.workingHour}
          unit="Hour"
          change={summary?.workingHourChange}
          icon={<Clock3 />}
          iconColor="#5b21b6"
        />

        <MetricCard
          title="AVG FUEL RATE"
          value={summary?.avgFuelRate}
          unit="L/H"
          change={summary?.avgFuelRateChange}
          icon={<Gauge />}
          iconColor="#92400e"
        />

        <MetricCard
          title="BEST PERFORMER"
          value={summary?.bestModel}
          unit=""
          change={0.1}
          icon={<Trophy />}
          iconColor="#14532d"
        />

        <MetricCard
          title="LEAST EFFICIENT"
          value={summary?.worstModel}
          unit=""
          change={-0.1}
          icon={<AlertCircle />}
          iconColor="#7f1d1d"
        />

        <MetricCard
          title="TOTAL UNIT"
          value={summary?.totalUnit}
          unit="Unit"
          change={0.1}
          icon={<PieChart />}
          iconColor="#1e3a8a"
        />

      </div>

      <div className="middle-grid">

        <DashboardCard
          title="FUEL RATE TREND"
        >
          <FuelTrendChart
            data={fuelTrend}
          />
        </DashboardCard>

        <DashboardCard
          title="FUEL RATE BY UNIT"
        >
          <FuelModelChart
            data={modelRates}
          />
        </DashboardCard>

        <AnalysisPanel />

      </div>

      <div className="bottom-grid">

        <DashboardCard
          title="UNIT PERFORMANCE MATRIX"
        >

          <TableToolbar />

          <PerformanceTable
            data={units}
          />

        </DashboardCard>

      </div>

    </div>
  );
}