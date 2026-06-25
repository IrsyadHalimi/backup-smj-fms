export const getTodayString = () => {
  return new Date().toISOString().split('T')[0];
};

export const calculateStats = (dbHistory) => {
  const totalLiters = dbHistory.reduce(
    (acc, curr) => acc + Number(curr.consumed_qty || 0),
    0
  );

  const uniqueUnits =
    new Set(dbHistory.map(r => r.no_unit_sap)).size;

  const anomalies = dbHistory.filter(
    r =>
      r.screening_status === 'Mismatch' ||
      r.screening_status?.includes('Error')
  ).length;

  const stations =
    new Set(dbHistory.map(r => r.gas_station)).size;

  return {
    totalLiters,
    uniqueUnits,
    anomalies,
    stations
  };
};