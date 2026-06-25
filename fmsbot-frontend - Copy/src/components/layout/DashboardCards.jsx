import StatCard from "../cards/StatCard";

const DashboardCards = ({
  stats = { totalLiters: 0, uniqueUnits: 0, anomalies: 0, stations: 0 }, // <--- default value jika undefined
  activeTheme
}) => {
  return (
    <div
      style={{
        display: 'grid',
        gridTemplateColumns: 'repeat(4, 1fr)',
        gap: '10px',
        marginBottom: '12px',
        flexShrink: 0
      }}
    >
      <StatCard
        title="Total Fuel Liters" 
        value={`${(stats?.totalLiters || 0).toLocaleString()} L`} //* <--- optional chaining (?.)
        color="#06b6d4"
        activeTheme={activeTheme}
        subValue="Actual Consumption" 
        value1label="OFI"
        value1={`${(stats?.totalLitersOFI || 0).toLocaleString()} L`}
        value2label="TFO"
        value2={`${(stats?.totalLitersTFO || 0).toLocaleString()} L`}
      />

      <StatCard
        title="Active Units"
        value={stats.uniqueUnits}
        color="#10b981"
        activeTheme={activeTheme}
        subValue="Last 24 Hours"
      />

      <StatCard
        title="AI Anomaly Flags"
        value={stats.anomalies}
        color="#ef4444"
        activeTheme={activeTheme}
        subValue="Check Required"
      />

      <StatCard
        title="Gas Stations"
        value={stats.stations}
        color="#f59e0b"
        activeTheme={activeTheme}
        subValue="Operational"
      />
    </div>
  );
};

export default DashboardCards;