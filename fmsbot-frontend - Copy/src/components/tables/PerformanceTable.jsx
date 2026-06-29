import { useMemo } from "react";
import { useFuelExploreStore } from "../../store/fuelExploreStore";
// Impor selector pagination yang sudah Anda buat
import { usePagination } from "../../store/fuelExploreSelectors"; 
import Sparkline from "../charts/Sparkline";
import PerformanceBadge from "./PerformanceBadge";
import "../../styles/css/PerformanceBadge.css";

export default function PerformanceTable({ page, setPage, limit, setLimit }) {
  const units = useFuelExploreStore((state) => state.units);
  const search = useFuelExploreStore((state) => state.search);
  
  // 1. GUNAKAN DATA PAGINATION DARI BACKEND DI SINI
  const pagination = usePagination(); 
  
  // Ambil total data dari metadata backend Anda (misal: pagination.total, pagination.total_records, dll.)
  // Sesuaikan properti ".total" di bawah dengan struktur json response dari Django Anda
  const totalEntries = pagination?.total || units.length; 

  // Filter client-side hanya untuk search bar saja
  const filteredRows = useMemo(() => {
    return units.filter((item) =>
      item.model.toLowerCase().includes(search.toLowerCase())
    );
  }, [units, search]);

  // 2. Total halaman sekarang akan dinamis (misal: 150 data / 50 = 3 halaman)
  const totalPages = Math.ceil(totalEntries / limit) || 1;

  const handlePageChange = (newPage) => {
    if (newPage >= 1 && newPage <= totalPages) {
      setPage(newPage); // State naik ke FuelExplorer -> Memicu useFuelExploreUnits berjalan ulang
    }
  };

  return (
    <div className="performance-table-container">
      <div className="performance-table">
        <table style={{ width: "100%" }}>
          <thead>
            <tr>
              <th>No</th>
              <th>Unit</th>
              <th>Fuel</th>
              <th>Hour</th>
              <th>Rate</th>
              <th>VS AVG</th>
              <th>Trend</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {filteredRows.map((row, index) => {
              // Menghitung nomor urut kontinu berdasarkan halaman aktif saat ini
              const globalIndex = (page - 1) * limit + index + 1;
              
              return (
                <tr key={row.id}>
                  <td>{globalIndex}</td>
                  <td>{row.model}</td>
                  <td>{row.fuel.toLocaleString()}</td>
                  <td>{row.hour}</td>
                  <td>{row.fuelRate}</td>
                  <td>
                    <span className={row.vsAverage > 0 ? "up" : "down"}>
                      {row.vsAverage}%
                    </span>
                  </td>
                  <td>
                    <Sparkline data={row.trend} />
                  </td>
                  <td>
                    <PerformanceBadge value={row.performance} />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* FOOTER CONTROLS */}
      <div className="pagination-controls">
        <div className="pagination-info">
          Showing {(page - 1) * limit + 1} to {Math.min(page * limit, totalEntries)} of {totalEntries} entries
        </div>
        
        <div className="pagination-buttons">
          <button onClick={() => handlePageChange(1)} disabled={page === 1}>{"<<"}</button>
          <button onClick={() => handlePageChange(page - 1)} disabled={page === 1}>Previous</button>
          
          <span>Page <strong>{page}</strong> of {totalPages}</span>

          <button onClick={() => handlePageChange(page + 1)} disabled={page === totalPages}>Next</button>
          <button onClick={() => handlePageChange(totalPages)} disabled={page === totalPages}>{">>"}</button>

          <select value={limit} onChange={(e) => { setLimit(Number(e.target.value)); setPage(1); }}>
            {[10, 25, 50, 100].map(size => <option key={size} value={size}>Show {size}</option>)}
          </select>
        </div>
      </div>
    </div>
  );
}