  const baseStyle = {
    fontSize: '10px',
    fontWeight: '700',
    padding: '4px 8px',
    borderRadius: '4px',
    display: 'inline-block',
    textAlign: 'center',
    minWidth: '60px'
  };

  const STATUS_CONFIG = {
    VERIFIED: {
      label: 'Matched',
      color: '#10b981',
      bg: 'rgba(16, 185, 129, 0.15)',
      border: 'rgba(16, 185, 129, 0.3)'
    },

    ERROR: {
      label: 'Error AI',
      color: '#ef4444',
      bg: 'rgba(239, 68, 68, 0.15)',
      border: 'rgba(239, 68, 68, 0.3)'
    }
  };

  const OCRStatusLabel = ({ status }) => {

    if (status === 'Processing' || status === 'Queue') {
      return (
        <div style={{ width: '100%' }}>
          <div style={{ ...baseStyle, color: '#06b6d4' }}>
            ⚡ Screening
          </div>

          <div
            style={{
              width: '100%',
              height: '4px',
              backgroundColor: '#334155',
              borderRadius: '2px',
              marginTop: '2px',
              overflow: 'hidden'
            }}
          >
            <div
              style={{
                width: '60%',
                height: '100%',
                backgroundColor: '#06b6d4',
                animation: 'blink 1s infinite'
              }}
            />
          </div>
        </div>
      );
    }

    if (status === 'Verified' || status === 'Matched') {
      const cfg = STATUS_CONFIG.VERIFIED;

      return (
        <span
          style={{
            ...baseStyle,
            backgroundColor: cfg.bg,
            color: cfg.color,
            border: `1px solid ${cfg.border}`
          }}
        >
          {cfg.label}
        </span>
      );
    }

    if (
      status === 'Mismatch' ||
      status === 'Mismatched' ||
      status === 'Failed' ||
      status?.includes('Error')
    ) {

      return (
        <span
          style={{
            ...baseStyle,
            backgroundColor: STATUS_CONFIG.ERROR.bg,
            color: STATUS_CONFIG.ERROR.color,
            border: `1px solid ${STATUS_CONFIG.ERROR.border}`
          }}
        >
          Error AI
        </span>
      );
    }

    return (
      <span style={{ color: '#94a3b8', fontSize: '10px' }}>
        -
      </span>
    );
  };

  export default OCRStatusLabel;