// --- 2. Filter Tabel Atas (LIVE MONITOR) ---
// Logika: Ambil data dari database (dbHistory) yang terbaru (10 data)
// Ini supaya saat REFRESH, data tidak hilang.
export const getFilteredLive = useMemo(() => {
    // Kita ambil 10 transaksi terakhir dari dbHistory sebagai basis Live Monitor
    return [...dbHistory]
      .sort((a, b) => b.id - a.id)
      .slice(0, 10);
  }, [dbHistory]);

export const getFilteredHistory = useMemo(() => {
    return dbHistory.filter(r => {
      const status = r.screening_status;
      
      // Munculkan jika butuh perhatian segera
      if (status === 'Mismatch' || status === 'Processing' || status === 'Pending' || status === 'Mismatched') {
        return true;
      }
      
      // Munculkan yang Verified hanya jika tanggalnya hari ini
      const screeningDate = r.date_screening || r.date; 
      return screeningDate === todayStr;
    });
  }, [dbHistory, todayStr]);
  
export const calculateStats = useMemo(() => {
    const totalLiters = dbHistory.reduce((acc, curr) => acc + Number(curr.consumed_qty || 0), 0);
    const uniqueUnits = new Set(dbHistory.map(r => r.no_unit_sap)).size;
    const anomalies = dbHistory.filter(r => r.screening_status === 'Mismatch' || r.screening_status?.includes('Error')).length;
    const stations = new Set(dbHistory.map(r => r.gas_station)).size;
    return { totalLiters, uniqueUnits, anomalies, stations };
  }, [dbHistory]);
