{/* =========================================================
    TABLE CONTAINER
    DINAMIS & RESPONSIF
========================================================= */}
<div
  style={{
    display: 'grid',

    gridTemplateColumns: '1fr 1fr',

    // gridTemplateRows menggunakan 'auto'
    // agar saat tabel minimize (44px),
    // baris langsung mengecil otomatis
    // dan memberikan sisa ruang ke row lainnya.

    gridTemplateRows: 'auto 1fr',

    gap: '16px',

    flex: 1,
    minHeight: 0,

    padding: '10px 0',

    overflow: 'hidden'
  }}
>

  {/* =====================================================
      LIVE QUEUE TABLE
      (LEFT TOP)
  ===================================================== */}
  <div
    style={{
      gridColumn: '1 / 2',
      display: 'flex',
      minHeight: 0
    }}
  >
    <FuelTable
      title="LIVE QUEUE"
      tableType="QUEUE"

      data={dbHistory}

      activeTheme={activeTheme}
      isDarkMode={isDarkMode}

      // Menggunakan state object:
      // expandedTable.live

      isExpanded={expandedTable.live}

      onToggle={() => handleToggle('live')}

      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}

      // Tambahkan ini untuk
      // fitur auto-hide zoom

      setZoomImage={setZoomImage}

      formatIndoDate={formatIndoDate}
    />
  </div>

  {/* =====================================================
      VERIFIED TABLE
      (RIGHT TOP)
  ===================================================== */}
  <div
    style={{
      gridColumn: '2 / 3',
      display: 'flex',
      minHeight: 0
    }}
  >
    <FuelTable
      title="READY TO EXPORT"
      tableType="VERIFIED"

      data={dbHistory}

      activeTheme={activeTheme}
      isDarkMode={isDarkMode}

      isExpanded={expandedTable.verified}

      onToggle={() => handleToggle('verified')}

      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}

      setZoomImage={setZoomImage}

      formatIndoDate={formatIndoDate}
    />
  </div>

  {/* =====================================================
      AI WORKBENCH TABLE
      (BOTTOM FULL WIDTH)
  ===================================================== */}
  <div
    style={{
      gridColumn: '1 / 3',
      display: 'flex',
      minHeight: 0
    }}
  >
    <FuelTable
      title="AI SCREENING WORKBENCH"
      tableType="AI_WORKBENCH"

      data={dbHistory}

      activeTheme={activeTheme}
      isDarkMode={isDarkMode}

      isExpanded={expandedTable.ai}

      onToggle={() => handleToggle('ai')}

      getImageUrl={getImageUrl}
      handleImageZoom={handleImageZoom}

      setZoomImage={setZoomImage}

      handleOpenValidation={handleOpenValidation}
      handleReScreening={handleReScreening}

      formatIndoDate={formatIndoDate}

      activeMenuId={activeMenuId}
      setActiveMenuId={setActiveMenuId}

      menuButtonStyle={menuButtonStyle}

      OCRStatusLabel={OCRStatusLabel}
    />
  </div>

</div>