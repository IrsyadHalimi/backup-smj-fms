{/* PAN-VIEWER OVERLAY (Bingkai Minimalis Tengah) */}
{panImage && (
  <div style={{
    position: 'fixed', inset: 0, backgroundColor: 'rgba(0,0,0,0.95)', 
    zIndex: 200, display: 'flex', justifyContent: 'center', alignItems: 'center'
  }}>
    <div style={{
      width: '850px', height: '650px', backgroundColor: '#111', borderRadius: '15px',
      position: 'relative', overflow: 'hidden', border: '1px solid rgba(255,255,255,0.2)',
    }}>
      {/* Header */}
      <div style={{ position: 'absolute', top: 0, left: 0, right: 0, padding: '15px 20px', background: 'linear-gradient(to bottom, rgba(0,0,0,0.8), transparent)', zIndex: 10, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <span style={{ fontSize: '12px', color: '#38bdf8', fontWeight: 'bold' }}>DRAG PHOTO TO INSPECT WATERMARK</span>
        <button onClick={() => setPanImage(null)} style={{ background: '#ef4444', border: 'none', color: '#fff', width: '30px', height: '30px', borderRadius: '50%', cursor: 'pointer' }}>
          <i className="fas fa-times"></i>
        </button>
      </div>

      {/* DRAG AREA */}
      <div 
        ref={dragRef}
        onMouseDown={handleMouseDown}
        onMouseLeave={() => setIsDragging(false)}
        onMouseUp={() => setIsDragging(false)}
        onMouseMove={handleMouseMove}
        className="no-scrollbar"
        style={{ 
          width: '100%', height: '100%', overflow: 'hidden', // Hidden agar scrollbar hilang
          cursor: isDragging ? 'grabbing' : 'grab' 
        }}
      >
        <img 
          src={panImage} 
          draggable="false" // Agar drag bawaan browser tidak ganggu
          style={{ 
            width: '200%', // Perbesar agar bisa di-drag
            display: 'block',
            userSelect: 'none'
          }} 
        />
      </div>
    </div>
  </div>
)}