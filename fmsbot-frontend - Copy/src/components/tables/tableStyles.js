export const thStyle = (theme) => ({
  padding: '10px 5px',
  border: `1px solid ${theme.border}44`,
  cursor: 'pointer',
  whiteSpace: 'nowrap'
});

export const tdStyle = (theme) => ({
  padding: '8px',
  borderRight: `1px solid ${theme.border}22`
});

export const statusBadge = (s) => ({
  fontSize: '9px',
  padding: '2px 6px',
  borderRadius: '4px',
  fontWeight: 'bold',
  backgroundColor:
    s === 'Verified'
      ? 'rgba(16,185,129,0.1)'
      : 'rgba(245,158,11,0.1)',

  color:
    s === 'Verified'
      ? '#10b981'
      : '#f59e0b',

  border: `1px solid ${
    s === 'Verified'
      ? '#10b98144'
      : '#f59e0b44'
  }`
});