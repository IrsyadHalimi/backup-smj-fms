import React from 'react';

const FuturisticInlineLoader = ({ activeTheme, text = "SYNCING" }) => {
  const accentColor = activeTheme?.accent || '#c084fc';

  React.useEffect(() => {
    const styleId = "futuristic-inline-styles";
    if (!document.getElementById(styleId)) {
      const styleSheet = document.createElement("style");
      styleSheet.id = styleId;
      styleSheet.innerText = `
        @keyframes inline-glow {
          0%, 100% { opacity: 0.3; transform: scale(0.95); }
          50% { opacity: 1; transform: scale(1.05); filter: drop-shadow(0 0 4px ${accentColor}); }
        }
        @keyframes progress-slide {
          0% { left: -100%; }
          100% { left: 100%; }
        }
      `;
      document.head.appendChild(styleSheet);
    }
  }, [accentColor]);

  return (
    <div style={{ 
      display: 'flex', 
      alignItems: 'center', 
      gap: '8px', 
      backgroundColor: `${accentColor}15`, 
      padding: '4px 10px', 
      borderRadius: '20px',
      border: `1px solid ${accentColor}44`,
      marginRight: '10px',
      flexShrink: 0,
      position: 'relative',
      overflow: 'hidden'
    }}>
      {/* Mini Glowing Dot Indicator */}
      <div style={{
        width: '6px',
        height: '6px',
        borderRadius: '50%',
        backgroundColor: accentColor,
        animation: 'inline-glow 1.5s ease-in-out infinite'
      }} />
      
      {/* Monospace Futuristic Text */}
      <span style={{
        fontSize: '9px',
        fontWeight: '900',
        letterSpacing: '1px',
        color: activeTheme?.text || '#ffffff',
        fontFamily: 'monospace',
        userSelect: 'none'
      }}>
        {text}
      </span>

      {/* Ambient Moving Scanline di bawah container kecil ini */}
      <div style={{
        position: 'absolute',
        bottom: 0, left: 0, width: '100%', height: '1px',
        background: `linear-gradient(90deg, transparent, ${accentColor}, transparent)`,
        animation: 'progress-slide 2s linear infinite'
      }} />
    </div>
  );
};

export default FuturisticInlineLoader;