import React from 'react';

export default function ScreeningStatusBadge({ status }) {

  const getConfig = () => {

    switch (status) {

      case 'Verified':
      case 'Downloaded':
        return {
          text: 'PASS',
          color: '#22c55e',
          bg: 'rgba(34,197,94,.15)',
          border: 'rgba(34,197,94,.3)'
        };

      case 'Flow Match':
      case 'HM Match':
        return {
          text: 'MATCH',
          color: '#06b6d4',
          bg: 'rgba(6,182,212,.15)',
          border: 'rgba(6,182,212,.3)'
        };

      case 'Mismatch':
        return {
          text: 'MISMATCH',
          color: '#f59e0b',
          bg: 'rgba(245,158,11,.15)',
          border: 'rgba(245,158,11,.3)'
        };

      case 'Failed':
      case 'Error AI':
        return {
          text: 'FAIL',
          color: '#ef4444',
          bg: 'rgba(239,68,68,.15)',
          border: 'rgba(239,68,68,.3)'
        };

      case 'Processing':
      case 'Screening':
        return {
          text: 'PROCESS',
          color: '#8b5cf6',
          bg: 'rgba(139,92,246,.15)',
          border: 'rgba(139,92,246,.3)'
        };

      default:
        return {
          text: status || '-',
          color: '#94a3b8',
          bg: 'rgba(148,163,184,.15)',
          border: 'rgba(148,163,184,.3)'
        };
    }
  };

  const cfg = getConfig();

  return (
    <span
      style={{
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        minWidth: '70px',
        padding: '4px 10px',
        borderRadius: '6px',
        fontSize: '10px',
        fontWeight: 700,
        color: cfg.color,
        backgroundColor: cfg.bg,
        border: `1px solid ${cfg.border}`
      }}
    >
      {cfg.text}
    </span>
  );
}