const RightAnalyticsPanel = ({
  rightPanelOpen,
  activeTheme,
  isDarkMode,
  dbHistory
}) => {
  return (
    <div
      className="panel-transition"
      style={{
        flexBasis: rightPanelOpen ? '240px' : '0px',
        minWidth: rightPanelOpen ? '240px' : '0px',
        maxWidth: rightPanelOpen ? '240px' : '0px',
        opacity: rightPanelOpen ? 1 : 0,
        overflow: 'hidden',
        backgroundColor: activeTheme.card,
        borderRadius: '8px',
        border: `1px solid ${activeTheme.border}`,
        padding: rightPanelOpen ? '15px' : '0px',
        display: 'flex',
        flexDirection: 'column'
      }}
    >
      <h4
        style={{
          margin: '0 0 15px 0',
          color: activeTheme.accent,
          fontSize: '13px'
        }}
      >
        AI ANALYTICS
      </h4>

        <div style={{ backgroundColor: isDarkMode ? 'rgba(0,0,0,0.2)' : '#fff', padding: '15px', borderRadius: '8px', textAlign: 'center', border: `1px solid ${activeTheme.border}` }}>
            <span style={{ fontSize: '28px', fontWeight: '700', color: activeTheme.accent }}>{dbHistory.length}</span>
            <p style={{ margin: 0, fontSize: '10px', color: activeTheme.subText }}>TOTAL RECORDS</p>
        </div>

        <div style={{ marginTop: '20px' }}>
            <div style={{ fontSize: '11px', color: activeTheme.subText, marginBottom: '10px' }}>Consumption Trend
            </div>
              <div style={{ display: 'flex', alignItems: 'flex-end', height: '100px', gap: '4px' }}>
                  {dbHistory.slice(0, 10).reverse().map((r, i) => (
                  <div key={i} style={{ flex: 1, backgroundColor: activeTheme.accent, height: `${Math.min((Number(r.consumed_qty)/1000)*100, 100)}%`, opacity: 0.6, borderRadius: '2px 2px 0 0' }} />
                  ))}
                  </div>
              </div>
          
    </div>
  );
};

export default RightAnalyticsPanel;