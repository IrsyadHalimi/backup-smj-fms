{/* Elegant Mini Toast */}
{notification.show && (
  <div style={{
    position: 'fixed',
    top: '30px',
    right: '30px',
    backgroundColor: 'rgba(20, 20, 25, 0.85)',
    backdropFilter: 'blur(10px)',
    border: '1px solid rgba(255, 255, 255, 0.1)',
    padding: '12px 20px',
    borderRadius: '12px',
    display: 'flex',
    alignItems: 'center',
    gap: '12px',
    color: '#fff',
    fontSize: '14px',
    fontWeight: '500',
    boxShadow: '0 10px 30px rgba(0,0,0,0.5)',
    zIndex: 9999,
    animation: 'slideInRight 0.4s cubic-bezier(0.18, 0.89, 0.32, 1.28)'
  }}>
    <i className={`fas fa-${notification.icon || 'info-circle'}`} style={{ color: '#c084fc' }}></i>
    <span>{notification.message}</span>
  </div>
)}