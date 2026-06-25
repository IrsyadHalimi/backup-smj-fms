const LeftMonitorPanel = ({
  leftPanelOpen,
  activeTheme,
  isConnected,
  dbHistory=[],
  isNewDataIn
}) => {
  return (
    <div
      className="panel-transition"
      style={{
        flexBasis: leftPanelOpen ? '240px' : '0px',
        minWidth: leftPanelOpen ? '240px' : '0px',
        maxWidth: leftPanelOpen ? '240px' : '0px',
        opacity: leftPanelOpen ? 1 : 0,
        overflow: 'hidden',
        backgroundColor: activeTheme.card,
        borderRadius: '8px',
        border: `1px solid ${activeTheme.border}`,
        padding: leftPanelOpen ? '15px' : '0px',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      <h4
        style={{
          margin: '0 0 10px 0',
          color: activeTheme.accent,
          fontSize: '13px'
        }}
      >
        SYSTEM MONITOR
      </h4>

      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          fontSize: '11px',
          color: isConnected
            ? '#10b981'
            : '#ef4444',
          marginBottom: '10px'
        }}
      >
        <div
          style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: isConnected
              ? '#10b981'
              : '#ef4444'
          }}
        />

        {isConnected
          ? 'CONNECTED'
          : 'DISCONNECTED'}
      </div>

      <div
        style={{
          backgroundColor: '#000',
          borderRadius: '6px',
          padding: '10px',
          fontFamily: 'monospace',
          fontSize: '11px',
          color: '#10b981',
          flex: 1,
          overflowY: 'auto'
        }}
      >
        {`> Socket: Active`}
        <br />

        {`> Archive: ${dbHistory.length}`}
        <br />

        {isNewDataIn && (
          <span style={{ color: '#fff' }}>
            {`> RECV_DATA_SYNC...`}
          </span>
        )}
      </div>
    </div>
  );
};

export default LeftMonitorPanel;