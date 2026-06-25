import React from 'react';
// 1. Import komponen FuelExplorer yang sudah Anda buat
// Catatan: Sesuaikan path "../../pages/FuelExplore/Dashboard" di bawah ini 
// dengan letak folder yang sebenarnya di proyek Anda.
import FuelExplorer from './pages/FuelExplore/FuelExplorer';

// =========================================================
// MAIN APP
// =========================================================
function App() {
  return (
    <>
      {/* 2. Panggil komponen di sini */}
      <FuelExplorer />
    </>
  );
}

export default App;